package com.fpsmonitor.service

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.fpsmonitor.MainActivity
import com.fpsmonitor.core.FpsData
import com.fpsmonitor.core.FpsMonitor
import com.fpsmonitor.core.HardwareData
import com.fpsmonitor.core.HardwareMonitor
import kotlinx.coroutines.*

/**
 * FpsOverlayService - Foreground service displaying a draggable FPS overlay.
 * Click to expand into a full white panel with FPS chart, CPU controls, and CPU freq chart.
 */
class FpsOverlayService : Service() {

    companion object {
        private const val CHANNEL_ID = "fps_overlay"
        private const val NOTIFICATION_ID = 1

        const val FPS_GOOD = 50
        const val FPS_WARNING = 30
    }

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var isExpanded = false

    private val fpsMonitor = FpsMonitor()
    private val hardwareMonitor = HardwareMonitor()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Collapsed view references
    private var collapsedLayout: LinearLayout? = null
    private var collapsedFpsText: TextView? = null

    // Expanded view references
    private var expandedLayout: LinearLayout? = null
    private var statusFpsText: TextView? = null
    private var statusCpuTempText: TextView? = null
    private var statusBatTempText: TextView? = null
    private var fpsChartView: FpsLineChartView? = null
    private var cpuFreqChartView: CpuFreqChartView? = null
    private var cpuCoreContainer: LinearLayout? = null
    private var cpuCoreViews: MutableList<LinearLayout> = mutableListOf()

    // CPU frequency history for chart
    private val cpuFreqHistory = mutableListOf<List<Long>>()
    private val MAX_CPU_HISTORY = 30

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)

        createOverlayView()
        startMonitoring()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopMonitoring()
        removeOverlayView()
        scope.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "FPS Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "FPS monitoring overlay notification"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FPS Monitor")
            .setContentText("FPS 监测运行中")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    /**
     * Create the overlay view with collapsed and expanded states.
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun createOverlayView() {
        // Main container
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        // --- Collapsed view (dark pill, always visible) ---
        collapsedLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(20, 10, 20, 10)
            setBackgroundColor(Color.argb(180, 0, 0, 0))
            // Use a GradientDrawable for rounded corners
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.argb(180, 0, 0, 0))
                cornerRadius = 24f
            }
        }

        collapsedFpsText = TextView(this).apply {
            text = "FPS: --"
            textSize = 16f
            setTextColor(Color.WHITE)
        }
        collapsedLayout!!.addView(collapsedFpsText)
        container.addView(collapsedLayout)

        // --- Expanded view (white panel, hidden by default) ---
        expandedLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            setPadding(16, 12, 16, 12)
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.argb(235, 255, 255, 255))
                cornerRadius = 16f
            }
        }

        // Top status bar: FPS + CPU temp + battery temp
        val statusBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, 8)
        }

        statusFpsText = TextView(this).apply {
            text = "FPS: --"
            textSize = 18f
            setTextColor(Color.DKGRAY)
            setPadding(0, 0, 16, 0)
        }
        statusBar.addView(statusFpsText)

        statusCpuTempText = TextView(this).apply {
            text = "CPU: --°C"
            textSize = 13f
            setTextColor(Color.DKGRAY)
            setPadding(0, 0, 12, 0)
        }
        statusBar.addView(statusCpuTempText)

        statusBatTempText = TextView(this).apply {
            text = "Bat: --°C"
            textSize = 13f
            setTextColor(Color.DKGRAY)
        }
        statusBar.addView(statusBatTempText)
        expandedLayout!!.addView(statusBar)

        // Middle section: FPS chart (left) + CPU core controls (right)
        val middleRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        // FPS chart
        fpsChartView = FpsLineChartView(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, 320).apply {
                weight = 1f
            }
        }
        middleRow.addView(fpsChartView)

        // CPU core controls (scrollable)
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                320
            )
        }
        cpuCoreContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(8, 0, 0, 0)
        }
        scrollView.addView(cpuCoreContainer)
        middleRow.addView(scrollView)
        expandedLayout!!.addView(middleRow)

        // Bottom: CPU frequency chart
        cpuFreqChartView = CpuFreqChartView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                200
            )
        }
        expandedLayout!!.addView(cpuFreqChartView)

        container.addView(expandedLayout)

        // Configure window params
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 200
        }

        container.setOnTouchListener(OverlayTouchListener())
        container.setOnClickListener {
            toggleExpanded()
        }

        overlayView = container
        windowManager.addView(overlayView, layoutParams)
    }

    private fun toggleExpanded() {
        isExpanded = !isExpanded
        expandedLayout?.visibility = if (isExpanded) View.VISIBLE else View.GONE
        try {
            overlayView?.let { windowManager.updateViewLayout(it, layoutParams) }
        } catch (_: Exception) { }
    }

    private fun removeOverlayView() {
        try {
            overlayView?.let { windowManager.removeView(it) }
        } catch (_: Exception) { }
        overlayView = null
    }

    private fun startMonitoring() {
        fpsMonitor.start()
        hardwareMonitor.start()

        scope.launch {
            launch {
                fpsMonitor.fpsData.collect { data -> updateFpsDisplay(data) }
            }
            launch {
                fpsMonitor.fpsChartData.collect { history ->
                    fpsChartView?.updateData(history)
                }
            }
            launch {
                hardwareMonitor.hardwareData.collect { data -> updateHardwareDisplay(data) }
            }
        }
    }

    private fun stopMonitoring() {
        fpsMonitor.stop()
        hardwareMonitor.stop()
    }

    private fun updateFpsDisplay(data: FpsData) {
        val fps = data.fps
        val color = when {
            fps >= FPS_GOOD -> Color.rgb(76, 175, 80)
            fps >= FPS_WARNING -> Color.rgb(255, 152, 0)
            else -> Color.rgb(244, 67, 54)
        }

        // Update collapsed view
        collapsedFpsText?.apply {
            text = "FPS: $fps"
            setTextColor(color)
        }

        // Update expanded status bar
        statusFpsText?.apply {
            text = "FPS: $fps"
            setTextColor(color)
        }
    }

    private fun updateHardwareDisplay(data: HardwareData) {
        if (!isExpanded) return

        // CPU temperature with color
        val cpuTemp = data.cpuTemp
        val cpuTempColor = when {
            cpuTemp < 40f -> Color.rgb(76, 175, 80)
            cpuTemp < 60f -> Color.rgb(255, 152, 0)
            else -> Color.rgb(244, 67, 54)
        }
        statusCpuTempText?.apply {
            text = "CPU: ${cpuTemp.toInt()}°C"
            setTextColor(cpuTempColor)
        }

        // Battery temperature
        val batTemp = data.batteryTemp
        val batTempColor = when {
            batTemp < 35f -> Color.rgb(76, 175, 80)
            batTemp < 45f -> Color.rgb(255, 152, 0)
            else -> Color.rgb(244, 67, 54)
        }
        statusBatTempText?.apply {
            text = "Bat: ${batTemp.toInt()}°C"
            setTextColor(batTempColor)
        }

        // Update CPU core controls
        updateCpuCoreControls(data)

        // Update CPU frequency chart
        if (data.cpuFreqs.isNotEmpty()) {
            cpuFreqHistory.add(data.cpuFreqs)
            if (cpuFreqHistory.size > MAX_CPU_HISTORY) {
                cpuFreqHistory.removeAt(0)
            }
            cpuFreqChartView?.updateData(cpuFreqHistory.toList(), data.cpuFreqs.size)
        }
    }

    private fun updateCpuCoreControls(data: HardwareData) {
        val container = cpuCoreContainer ?: return
        val cores = data.cpuCores

        // Remove old views
        for (view in cpuCoreViews) {
            container.removeView(view)
        }
        cpuCoreViews.clear()

        if (cores.isEmpty()) {
            // Use raw cpuFreqs if no governor data
            data.cpuFreqs.forEachIndexed { index, freq ->
                val row = createCoreRow(index, freq, "", emptyList())
                container.addView(row)
                cpuCoreViews.add(row)
            }
        } else {
            for (core in cores) {
                val row = createCoreRow(core.coreId, core.freqMHz, core.governor, core.availableGovernors)
                container.addView(row)
                cpuCoreViews.add(row)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun createCoreRow(
        coreId: Int,
        freqMHz: Long,
        governor: String,
        availableGovernors: List<String>
    ): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(8, 4, 8, 4)
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.parseColor("#F5F5F5"))
                cornerRadius = 6f
            }
            // Margin between rows
            (layoutParams as? LinearLayout.LayoutParams)?.setMargins(0, 0, 0, 4)
        }

        val freqText = TextView(this).apply {
            text = "核心$coreId: ${freqMHz}MHz"
            textSize = 12f
            setTextColor(Color.DKGRAY)
        }
        row.addView(freqText)

        val govText = TextView(this).apply {
            text = if (governor.isNotEmpty()) "调速器: $governor" else "调速器: 不支持"
            textSize = 11f
            setTextColor(Color.parseColor("#888888"))
            setPadding(0, 2, 0, 0)

            if (governor.isNotEmpty() && availableGovernors.isNotEmpty()) {
                setOnClickListener {
                    showGovernorDialog(coreId, governor, availableGovernors, this)
                }
                // Visual indicator that it's clickable
                setBackgroundColor(Color.parseColor("#E8E8E8"))
            }
        }
        row.addView(govText)

        return row
    }

    private fun showGovernorDialog(
        coreId: Int,
        currentGovernor: String,
        availableGovernors: List<String>,
        targetView: TextView
    ) {
        val items = availableGovernors.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("选择核心${coreId}调速器")
            .setItems(items) { _, which ->
                val selected = items[which]
                scope.launch {
                    val success = hardwareMonitor.setCpuGovernor(coreId, selected)
                    if (success) {
                        targetView.post {
                            targetView.text = "调速器: $selected"
                        }
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * Touch listener for dragging the overlay.
     */
    private inner class OverlayTouchListener : View.OnTouchListener {
        private var initialX = 0
        private var initialY = 0
        private var initialTouchX = 0f
        private var initialTouchY = 0f
        private var isDragging = false
        private val dragThreshold = 10f

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams?.x ?: 0
                    initialY = layoutParams?.y ?: 0
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    if (Math.abs(dx) > dragThreshold || Math.abs(dy) > dragThreshold) {
                        isDragging = true
                    }
                    if (isDragging) {
                        layoutParams?.apply {
                            x = (initialX + dx).toInt()
                            y = (initialY + dy).toInt()
                        }
                        try {
                            overlayView?.let { windowManager.updateViewLayout(it, layoutParams) }
                        } catch (_: Exception) { }
                    }
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    return !isDragging
                }
            }
            return false
        }
    }
}