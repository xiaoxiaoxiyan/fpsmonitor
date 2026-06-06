package com.fpsmonitor.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.core.app.NotificationCompat
import com.fpsmonitor.MainActivity
import com.fpsmonitor.core.FpsData
import com.fpsmonitor.core.FpsMonitor
import com.fpsmonitor.core.HardwareData
import com.fpsmonitor.core.HardwareMonitor
import com.fpsmonitor.core.SettingsManager
import kotlinx.coroutines.*

/**
 * FpsOverlayService - Dynamic Island style floating FPS monitor.
 * - Collapsed: pill-shaped frosted glass pill (Dynamic Island style)
 * - Expanded: glass morphic panel with FPS chart, CPU control, frequency setting
 * - Auto-adaptive positioning and screen ratio
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

    // Collapsed
    private var collapsedLayout: LinearLayout? = null
    private var collapsedFpsText: TextView? = null

    // Expanded
    private var expandedLayout: LinearLayout? = null
    private var statusFpsText: TextView? = null
    private var statusCpuTempText: TextView? = null
    private var statusBatTempText: TextView? = null
    private var fpsChartView: FpsLineChartView? = null
    private var cpuFreqChartView: CpuFreqChartView? = null
    private var cpuCoreContainer: LinearLayout? = null
    private var cpuCoreViews: MutableList<LinearLayout> = mutableListOf()

    private val cpuFreqHistory = mutableListOf<List<Long>>()
    private val MAX_CPU_HISTORY = 30

    override fun onCreate() {
        super.onCreate()
        SettingsManager.init(this)
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
                CHANNEL_ID, "FPS Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "FPS monitoring overlay" }
            val mgr = getSystemService(NotificationManager::class.java)
            mgr.createNotificationChannel(channel)
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

    @SuppressLint("ClickableViewAccessibility")
    private fun createOverlayView() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }

        // --- Collapsed: Dynamic Island pill ---
        collapsedLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(28, 12, 28, 12)
            background = createGlassPillBackground(0.85f)
        }

        collapsedFpsText = TextView(this).apply {
            text = "60"
            textSize = 15f
            setTextColor(Color.WHITE)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setShadowLayer(2f, 0f, 1f, Color.argb(60, 0, 0, 0))
        }
        collapsedLayout!!.addView(collapsedFpsText)
        container.addView(collapsedLayout)

        // --- Expanded: Glass morphic panel ---
        expandedLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            setPadding(14, 10, 14, 10)
            background = createGlassPanelBackground()
        }

        // Top status bar
        val statusBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(4, 0, 4, 6)
        }

        statusFpsText = createStatusLabel("60", 16f, Color.rgb(76, 175, 80), 0)
        statusBar.addView(statusFpsText)
        statusBar.addView(createSepDots())

        statusCpuTempText = createStatusLabel("42°C", 12f, Color.DKGRAY, 0)
        statusBar.addView(statusCpuTempText)
        statusBar.addView(createSepDots())

        statusBatTempText = createStatusLabel("35°C", 12f, Color.DKGRAY, 0)
        statusBar.addView(statusBatTempText)
        expandedLayout!!.addView(statusBar)

        // FPS Chart
        fpsChartView = FpsLineChartView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 240
            ).apply { bottomMargin = 6 }
        }
        expandedLayout!!.addView(fpsChartView)

        // CPU core controls (scrollable row)
        val coreScroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 180
            ).apply { bottomMargin = 6 }
        }
        cpuCoreContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(4, 0, 4, 0)
        }
        coreScroll.addView(cpuCoreContainer)
        expandedLayout!!.addView(coreScroll)

        // CPU freq chart
        cpuFreqChartView = CpuFreqChartView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 160
            )
        }
        expandedLayout!!.addView(cpuFreqChartView)

        container.addView(expandedLayout)

        // Window params — center-top like Dynamic Island
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
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 120
        }

        container.setOnTouchListener(OverlayTouchListener())
        container.setOnClickListener { toggleExpanded() }

        overlayView = container
        windowManager.addView(overlayView, layoutParams)
    }

    // ── Glass morphic backgrounds ──

    /** Collapsed pill: dark frosted glass */
    private fun createGlassPillBackground(opacity: Float): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 48f
            setColor(Color.argb((255 * opacity).toInt(), 20, 20, 22))
            setStroke(1.dpToPx(), Color.argb(60, 255, 255, 255))
        }
    }

    /** Expanded panel: light glass morphic */
    private fun createGlassPanelBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 20f
            setColor(Color.argb(225, 245, 247, 250))
            setStroke(1.dpToPx(), Color.argb(40, 180, 180, 190))
        }
    }

    private fun createStatusLabel(text: String, size: Float, color: Int, padDp: Int): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            setPadding(padDp, 0, padDp, 0)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    private fun createSepDots(): TextView {
        return TextView(this).apply {
            text = " · "
            textSize = 12f
            setTextColor(Color.parseColor("#B0B0B0"))
        }
    }

    // ── Expand / Collapse ──

    private var toggleInProgress = false

    private fun toggleExpanded() {
        if (toggleInProgress) return
        toggleInProgress = true
        try {
            isExpanded = !isExpanded
            expandedLayout?.visibility = if (isExpanded) View.VISIBLE else View.GONE
            overlayView?.let { windowManager.updateViewLayout(it, layoutParams) }
        } catch (_: Exception) {
        } finally {
            toggleInProgress = false
        }
    }

    private fun removeOverlayView() {
        try { overlayView?.let { windowManager.removeView(it) } } catch (_: Exception) { }
        overlayView = null
    }

    // ── Monitoring ──

    private fun startMonitoring() {
        fpsMonitor.applySettings()
        fpsMonitor.start()
        hardwareMonitor.start()
        scope.launch {
            launch { fpsMonitor.fpsData.collect { updateFpsDisplay(it) } }
            launch {
                fpsMonitor.fpsChartData.collect {
                    fpsChartView?.updateData(it, SettingsManager.targetFps)
                }
            }
            launch { hardwareMonitor.hardwareData.collect { updateHardwareDisplay(it) } }
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
        collapsedFpsText?.apply { text = "$fps"; setTextColor(color) }
        statusFpsText?.apply { text = "$fps"; setTextColor(color) }
    }

    private fun updateHardwareDisplay(data: HardwareData) {
        if (!isExpanded) return

        val cpuTemp = data.cpuTemp
        statusCpuTempText?.apply {
            text = "${cpuTemp.toInt()}°C"
            setTextColor(when {
                cpuTemp < 40f -> Color.rgb(76, 175, 80)
                cpuTemp < 60f -> Color.rgb(255, 152, 0)
                else -> Color.rgb(244, 67, 54)
            })
        }
        statusBatTempText?.apply {
            val bt = data.batteryTemp
            text = "${bt.toInt()}°C"
            setTextColor(when {
                bt < 35f -> Color.rgb(76, 175, 80)
                bt < 45f -> Color.rgb(255, 152, 0)
                else -> Color.rgb(244, 67, 54)
            })
        }

        updateCpuCoreControls(data)

        if (data.cpuFreqs.isNotEmpty()) {
            cpuFreqHistory.add(data.cpuFreqs)
            if (cpuFreqHistory.size > MAX_CPU_HISTORY) cpuFreqHistory.removeAt(0)
            cpuFreqChartView?.updateData(cpuFreqHistory.toList(), data.cpuFreqs.size)
        }
    }

    // ── CPU core controls with governor + frequency ──

    private fun updateCpuCoreControls(data: HardwareData) {
        val container = cpuCoreContainer ?: return
        val cores = data.cpuCores

        for (view in cpuCoreViews) { container.removeView(view) }
        cpuCoreViews.clear()

        val list = if (cores.isNotEmpty()) cores
        else data.cpuFreqs.mapIndexed { i, f -> com.fpsmonitor.core.CpuCoreInfo(i, f, "", emptyList()) }

        for (core in list) {
            val row = createCoreRow(core)
            container.addView(row)
            cpuCoreViews.add(row)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun createCoreRow(core: com.fpsmonitor.core.CpuCoreInfo): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(6, 3, 6, 3)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#FAFAFA"))
                cornerRadius = 8f
                setStroke(1, Color.parseColor("#E0E0E0"))
            }
            gravity = Gravity.CENTER_VERTICAL
            (layoutParams as? LinearLayout.LayoutParams)?.setMargins(0, 0, 0, 3)
        }

        // Core ID + freq
        val infoText = TextView(this).apply {
            text = "C${core.coreId}"
            textSize = 11f
            setTextColor(Color.DKGRAY)
            setPadding(0, 0, 6, 0)
        }
        row.addView(infoText)

        val freqText = TextView(this).apply {
            text = "${core.freqMHz}MHz"
            textSize = 11f
            setTextColor(Color.parseColor("#555555"))
            setPadding(0, 0, 8, 0)
        }
        row.addView(freqText)

        // Governor button
        val govBtn = TextView(this).apply {
            text = if (core.governor.isNotEmpty()) core.governor else "--"
            textSize = 10f
            setPadding(6, 2, 6, 2)
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#78909C"))
                cornerRadius = 10f
            }
            if (core.availableGovernors.isNotEmpty()) {
                setOnClickListener { showGovernorPopup(core.coreId, core.availableGovernors, this) }
            }
        }
        row.addView(govBtn)

        // Frequency input
        val freqBtn = TextView(this).apply {
            text = "⚙"
            textSize = 12f
            setPadding(6, 2, 6, 2)
            setTextColor(Color.parseColor("#455A64"))
            setOnClickListener { showFreqInputPopup(core.coreId) }
        }
        row.addView(freqBtn)

        return row
    }

    // ── Governor popup (PopupWindow, safe in Service) ──

    private fun showGovernorPopup(coreId: Int, governors: List<String>, anchor: View) {
        try {
            val listView = ListView(this).apply {
                adapter = object : ArrayAdapter<String>(this@FpsOverlayService,
                    android.R.layout.simple_list_item_1, governors) {
                    override fun getView(pos: Int, v: View?, parent: ViewGroup): View {
                        val tv = super.getView(pos, v, parent) as TextView
                        tv.setPadding(24, 16, 24, 16)
                        tv.setTextColor(Color.DKGRAY)
                        return tv
                    }
                }
                setBackgroundColor(Color.WHITE)
            }

            val popup = PopupWindow(listView, 260, ViewGroup.LayoutParams.WRAP_CONTENT, true).apply {
                setBackgroundDrawable(GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = 12f
                    setStroke(1, Color.parseColor("#DDDDDD"))
                })
                elevation = 16f
            }

            listView.setOnItemClickListener { _, _, pos, _ ->
                val selected = governors[pos]
                popup.dismiss()
                scope.launch {
                    val ok = hardwareMonitor.setCpuGovernor(coreId, selected)
                    if (ok) {
                        anchor.post { (anchor as TextView).text = selected }
                    }
                }
            }

            popup.showAsDropDown(anchor, 0, -260)
        } catch (e: Exception) {
            // PopupWindow may fail on some devices; silently ignore
        }
    }

    // ── Frequency input popup ──

    private fun showFreqInputPopup(coreId: Int) {
        try {
            val editText = EditText(this).apply {
                hint = "输入MHz (如 1800)"
                setTextColor(Color.DKGRAY)
                setHintTextColor(Color.GRAY)
                setPadding(24, 16, 24, 16)
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
            }

            val popup = PopupWindow(editText, 300, ViewGroup.LayoutParams.WRAP_CONTENT, true).apply {
                setBackgroundDrawable(GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = 12f
                    setStroke(1, Color.parseColor("#DDDDDD"))
                })
                elevation = 16f
                isFocusable = true
            }

            editText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                    val mhz = editText.text.toString().toLongOrNull()
                    if (mhz != null && mhz > 0) {
                        popup.dismiss()
                        scope.launch {
                            hardwareMonitor.setCpuGovernor(coreId, "userspace")
                            delay(50)
                            val khz = mhz * 1000
                            hardwareMonitor.setCpuMaxFreq(coreId, khz)
                            hardwareMonitor.setCpuMinFreq(coreId, khz / 2)
                        }
                    }
                    true
                } else false
            }

            // Find the container view to show near
            val container = cpuCoreContainer
            if (container != null && container.childCount > 0) {
                popup.showAtLocation(container, Gravity.CENTER, 0, 0)
            }
        } catch (_: Exception) { }
    }

    // ── Utility ──

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    // ── Touch listener for dragging ──

    private inner class OverlayTouchListener : View.OnTouchListener {
        private var initialX = 0
        private var initialY = 0
        private var initialTouchX = 0f
        private var initialTouchY = 0f
        private var isDragging = false
        private var moved = false

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams?.x ?: 0
                    initialY = layoutParams?.y ?: 0
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    moved = false
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    if (Math.abs(dx) > 8f || Math.abs(dy) > 8f) {
                        isDragging = true
                        moved = true
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
                    if (!moved) {
                        view.performClick()
                    }
                    return true
                }
            }
            return false
        }
    }
}