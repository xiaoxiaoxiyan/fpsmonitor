package com.fpsmonitor.service

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
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.fpsmonitor.MainActivity
import com.fpsmonitor.R
import com.fpsmonitor.core.FpsData
import com.fpsmonitor.core.FpsMonitor
import com.fpsmonitor.core.HardwareData
import com.fpsmonitor.core.HardwareMonitor
import kotlinx.coroutines.*

/**
 * FpsOverlayService - Foreground service displaying a draggable FPS overlay.
 *
 * Reference: TinyDancer's Service-based overlay approach.
 * TinyDancer uses a Service to manage the overlay lifecycle and
 * WindowManager to add/remove the floating view.
 *
 * Features:
 * - Draggable overlay (TinyDancer's onTouchListener drag pattern)
 * - Color-coded FPS (green >= 50, yellow 30-49, red < 30)
 * - Collapsible/expandable (tap to toggle)
 * - Shows hardware data when expanded (CPU/GPU/temp/battery)
 */
class FpsOverlayService : Service() {

    companion object {
        private const val CHANNEL_ID = "fps_overlay"
        private const val NOTIFICATION_ID = 1
        private const val TAG = "FpsOverlayService"

        // FPS color thresholds (TinyDancer pattern)
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

    // UI references
    private var fpsText: TextView? = null
    private var detailText: TextView? = null
    private var detailLayout: LinearLayout? = null

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

    /**
     * Create notification channel for foreground service.
     */
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

    /**
     * Build foreground service notification.
     */
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
     * Create the WindowManager overlay view.
     * Reference: TinyDancer's overlay View creation pattern.
     */
    private fun createOverlayView() {
        // Create main container
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 12)
            setBackgroundColor(Color.argb(180, 0, 0, 0))
        }

        // FPS text (large, always visible)
        fpsText = TextView(this).apply {
            text = "FPS: --"
            textSize = 18f
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, 4)
        }
        container.addView(fpsText)

        // Detail layout (hidden when collapsed)
        detailLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }

        detailText = TextView(this).apply {
            text = ""
            textSize = 11f
            setTextColor(Color.argb(200, 255, 255, 255))
        }
        detailLayout!!.addView(detailText)
        container.addView(detailLayout)

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

        // Make overlay draggable (TinyDancer pattern: onTouchListener)
        container.setOnTouchListener(OverlayTouchListener())

        // Tap to expand/collapse (TinyDancer pattern: click to toggle)
        container.setOnClickListener {
            isExpanded = !isExpanded
            detailLayout?.visibility = if (isExpanded) View.VISIBLE else View.GONE
            // Update window layout
            try {
                overlayView?.let { windowManager.updateViewLayout(it, layoutParams) }
            } catch (_: Exception) { }
        }

        overlayView = container
        windowManager.addView(overlayView, layoutParams)
    }

    /**
     * Remove overlay view from WindowManager.
     */
    private fun removeOverlayView() {
        try {
            overlayView?.let { windowManager.removeView(it) }
        } catch (_: Exception) { }
        overlayView = null
    }

    /**
     * Start FPS and hardware monitoring.
     */
    private fun startMonitoring() {
        fpsMonitor.start()
        hardwareMonitor.start()

        // Collect FPS data and update overlay
        scope.launch {
            launch {
                fpsMonitor.fpsData.collect { data -> updateFpsDisplay(data) }
            }
            launch {
                hardwareMonitor.hardwareData.collect { data -> updateHardwareDisplay(data) }
            }
        }
    }

    /**
     * Stop monitoring.
     */
    private fun stopMonitoring() {
        fpsMonitor.stop()
        hardwareMonitor.stop()
    }

    /**
     * Update FPS display with color coding.
     * Reference: TinyDancer's color coding (green/yellow/red).
     */
    private fun updateFpsDisplay(data: FpsData) {
        val fps = data.fps
        val color = when {
            fps >= FPS_GOOD -> Color.rgb(76, 175, 80)    // Green
            fps >= FPS_WARNING -> Color.rgb(255, 193, 7)  // Yellow
            else -> Color.rgb(244, 67, 54)                // Red
        }
        fpsText?.apply {
            text = "FPS: $fps"
            setTextColor(color)
        }
    }

    /**
     * Update hardware data display.
     */
    private fun updateHardwareDisplay(data: HardwareData) {
        if (!isExpanded) return

        val cpuFreq = if (data.cpuFreqs.isNotEmpty()) {
            data.cpuFreqs.take(4).joinToString(" ") { "${it}MHz" }
        } else "N/A"

        val text = buildString {
            append("CPU: ${data.cpuUsage.toInt()}%")
            if (cpuFreq != "N/A") append(" | $cpuFreq")
            append("\nGPU: ${data.gpuUsage.toInt()}%")
            append(" | Bat: ${data.batteryLevel}%")
            append("\nTemp: CPU ${data.cpuTemp.toInt()}°C")
            append(" | Bat ${data.batteryTemp.toInt()}°C")
        }

        detailText?.text = text
    }

    /**
     * Touch listener for dragging the overlay.
     * Reference: TinyDancer's drag implementation using onTouchListener.
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
                    // If not dragged, treat as click (handled by onClickListener)
                    return !isDragging
                }
            }
            return false
        }
    }
}