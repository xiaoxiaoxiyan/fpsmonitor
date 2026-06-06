package com.fpsmonitor.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
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
import com.fpsmonitor.core.CpuCoreInfo
import com.fpsmonitor.core.FpsData
import com.fpsmonitor.core.FpsMonitor
import com.fpsmonitor.core.HardwareData
import com.fpsmonitor.core.HardwareMonitor
import com.fpsmonitor.core.SettingsManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

/**
 * FpsOverlayService - Vertical floating overlay with White color scheme.
 * - Collapsed: Dark pill on right side
 * - Expanded: Full vertical card with all content
 */
class FpsOverlayService : Service() {

    companion object {
        private const val CHANNEL_ID = "fps_overlay"
        private const val NOTIFICATION_ID = 1
        const val FPS_GOOD = 50
        const val FPS_WARNING = 30
        private const val MAX_HISTORY = 30
    }

    // ── Color palette (Pure White theme) ──
    private val COLOR_BG_PANEL = Color.parseColor("#FFFFFF")
    private val COLOR_BG_CARD = Color.parseColor("#F5F5F5")
    private val COLOR_TEXT_PRIMARY = Color.parseColor("#1A1A1A")
    private val COLOR_TEXT_SECONDARY = Color.parseColor("#888888")
    private val COLOR_BORDER = Color.parseColor("#E0E0E0")
    private val COLOR_BG_COLLAPSED = Color.parseColor("#1A1A1A")
    private val COLOR_TEXT_COLLAPSED = Color.parseColor("#FFFFFF")
    private val COLOR_BUTTON_BG = Color.parseColor("#1A1A1A")
    private val COLOR_BUTTON_TEXT = Color.parseColor("#FFFFFF")
    private val CORNER_RADIUS_CARD = 12f
    private val CORNER_RADIUS_COLLAPSED = 24f

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var isExpanded = false
    private var toggleInProgress = false

    private val fpsMonitor = FpsMonitor()
    private val hardwareMonitor = HardwareMonitor()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // ── Collapsed views ──
    private var collapsedLayout: LinearLayout? = null
    private var collapsedFpsText: TextView? = null

    // ── Expanded views ──
    private var expandedLayout: LinearLayout? = null
    private var expandedScrollView: ScrollView? = null

    // Top FPS big text
    private var bigFpsText: TextView? = null

    // Stats card (avg/min/max/jank)
    private var avgFpsText: TextView? = null
    private var minFpsText: TextView? = null
    private var maxFpsText: TextView? = null
    private var jankCountText: TextView? = null

    // Charts
    private var fpsChartView: FpsLineChartView? = null
    private var frameTimeChartView: FrameTimeChartView? = null
    private var cpuFreqChartView: CpuFreqChartView? = null

    // Device info card
    private var deviceInfoContainer: LinearLayout? = null
    private var deviceInfoPopulated = false

    // Temperature card
    private var tempCpuText: TextView? = null
    private var tempGpuText: TextView? = null
    private var tempBatText: TextView? = null
    private var tempTrendChartView: TempTrendChartView? = null

    // CPU control card
    private var cpuCoreContainer: LinearLayout? = null
    private var cpuCoreViews: MutableList<LinearLayout> = mutableListOf()

    // ── History buffers ──
    private val cpuFreqHistory = mutableListOf<List<Long>>()
    private val frameTimeHistory = mutableListOf<Float>()
    private val cpuTempHistory = mutableListOf<Float>()

    // ── Lifecycle ──

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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Re-apply the overlay view position after screen rotation
        removeOverlayView()
        createOverlayView()
    }

    // ── Notification ──

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

    // ── Overlay creation ──

    @SuppressLint("ClickableViewAccessibility")
    private fun createOverlayView() {
        // Read user-configured colors (read once at service start)
        val userCollapsedBgColor = Color.parseColor(SettingsManager.collapsedBgColor)
        val userPanelBgColor = Color.parseColor(SettingsManager.panelBgColor)
        val userFpsTextColor = Color.parseColor(SettingsManager.fpsTextColor)

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }

        // --- Collapsed: Dark pill (48dp x 80dp) ---
        collapsedLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(8.dpToPx(), 0, 8.dpToPx(), 0)
            layoutParams = LinearLayout.LayoutParams(48.dpToPx(), 80.dpToPx())
            background = createCollapsedBackground(userCollapsedBgColor)
        }

        collapsedFpsText = TextView(this).apply {
            text = "60"
            textSize = 18f
            setTextColor(COLOR_TEXT_COLLAPSED)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        collapsedLayout!!.addView(collapsedFpsText)
        container.addView(collapsedLayout)

        // --- Expanded: White vertical card (260dp width) ---
        expandedLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            setPadding(12.dpToPx(), 12.dpToPx(), 12.dpToPx(), 12.dpToPx())
            background = createExpandedPanelBackground(userPanelBgColor)
            layoutParams = LinearLayout.LayoutParams(260.dpToPx(), ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        // Wrap content in ScrollView
        expandedScrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val contentContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        // 1. FPS big text (top center, 48sp)
        bigFpsText = TextView(this).apply {
            text = "60"
            textSize = 48f
            setTextColor(userFpsTextColor)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, 4.dpToPx(), 0, 12.dpToPx())
        }
        contentContainer.addView(bigFpsText)

        // 2. FPS stats card
        val statsCard = createCardLayout()
        statsCard.addView(createFpsStatsRow())
        setCardMargin(statsCard, 0, 0, 0, 12.dpToPx())
        contentContainer.addView(statsCard)

        // 3. FPS line chart
        val fpsChartCard = createCardLayout()
        fpsChartView = FpsLineChartView(this@FpsOverlayService).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 200.dpToPx()
            )
        }
        fpsChartCard.addView(fpsChartView)
        setCardMargin(fpsChartCard, 0, 0, 0, 12.dpToPx())
        contentContainer.addView(fpsChartCard)

        // 4. Frame time bar chart
        val frameTimeCard = createCardLayout()
        frameTimeChartView = FrameTimeChartView(this@FpsOverlayService).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 160.dpToPx()
            )
        }
        frameTimeCard.addView(frameTimeChartView)
        setCardMargin(frameTimeCard, 0, 0, 0, 12.dpToPx())
        contentContainer.addView(frameTimeCard)

        // 5. CPU frequency chart
        val cpuFreqCard = createCardLayout()
        cpuFreqChartView = CpuFreqChartView(this@FpsOverlayService).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 160.dpToPx()
            )
        }
        cpuFreqCard.addView(cpuFreqChartView)
        setCardMargin(cpuFreqCard, 0, 0, 0, 12.dpToPx())
        contentContainer.addView(cpuFreqCard)

        // 6. Device info card
        val deviceCard = createCardLayout()
        val deviceHeader = TextView(this@FpsOverlayService).apply {
            text = "设备信息"
            textSize = 14f
            setTextColor(COLOR_TEXT_PRIMARY)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(8.dpToPx(), 4.dpToPx(), 8.dpToPx(), 8.dpToPx())
        }
        deviceCard.addView(deviceHeader)
        deviceInfoContainer = LinearLayout(this@FpsOverlayService).apply {
            orientation = LinearLayout.VERTICAL
        }
        deviceCard.addView(deviceInfoContainer)
        setCardMargin(deviceCard, 0, 0, 0, 12.dpToPx())
        contentContainer.addView(deviceCard)

        // 7. Temperature card
        val tempCard = createCardLayout()
        val tempHeader = TextView(this@FpsOverlayService).apply {
            text = "温度"
            textSize = 14f
            setTextColor(COLOR_TEXT_PRIMARY)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(8.dpToPx(), 4.dpToPx(), 8.dpToPx(), 8.dpToPx())
        }
        tempCard.addView(tempHeader)

        // Temperature big display
        val tempTopRow = LinearLayout(this@FpsOverlayService).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(8.dpToPx(), 4.dpToPx(), 8.dpToPx(), 8.dpToPx())
            gravity = Gravity.CENTER_VERTICAL
        }
        tempCpuText = TextView(this@FpsOverlayService).apply {
            text = "0°C"
            textSize = 36f
            setTextColor(COLOR_TEXT_PRIMARY)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            )
            gravity = Gravity.CENTER
        }
        tempTopRow.addView(tempCpuText)

        val tempRightColumn = LinearLayout(this@FpsOverlayService).apply {
            orientation = LinearLayout.VERTICAL
        }
        tempGpuText = TextView(this@FpsOverlayService).apply {
            text = "GPU: --°C"
            textSize = 14f
            setTextColor(COLOR_TEXT_PRIMARY)
            setPadding(2.dpToPx(), 2.dpToPx(), 2.dpToPx(), 2.dpToPx())
        }
        tempBatText = TextView(this@FpsOverlayService).apply {
            text = "电池: --°C"
            textSize = 14f
            setTextColor(COLOR_TEXT_PRIMARY)
            setPadding(2.dpToPx(), 2.dpToPx(), 2.dpToPx(), 2.dpToPx())
        }
        tempRightColumn.addView(tempGpuText)
        tempRightColumn.addView(tempBatText)
        tempTopRow.addView(tempRightColumn)

        tempCard.addView(tempTopRow)

        // Temperature trend chart
        tempTrendChartView = TempTrendChartView(this@FpsOverlayService).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 160.dpToPx()
            )
        }
        tempCard.addView(tempTrendChartView)
        setCardMargin(tempCard, 0, 0, 0, 12.dpToPx())
        contentContainer.addView(tempCard)

        // 8. CPU control card
        val cpuControlCard = createCardLayout()
        val controlHeader = TextView(this@FpsOverlayService).apply {
            text = "CPU 控制"
            textSize = 14f
            setTextColor(COLOR_TEXT_PRIMARY)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(8.dpToPx(), 4.dpToPx(), 8.dpToPx(), 8.dpToPx())
        }
        cpuControlCard.addView(controlHeader)
        cpuCoreContainer = LinearLayout(this@FpsOverlayService).apply {
            orientation = LinearLayout.VERTICAL
        }
        cpuControlCard.addView(cpuCoreContainer)
        contentContainer.addView(cpuControlCard)

        // Add everything to scrollView and expandedLayout
        expandedScrollView!!.addView(contentContainer)
        expandedLayout!!.addView(expandedScrollView)
        container.addView(expandedLayout)

        // Window params - default position: right side, center vertical
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
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
            x = 0
        }

        container.setOnTouchListener(OverlayTouchListener())
        container.setOnClickListener { toggleExpanded() }

        overlayView = container
        windowManager.addView(overlayView, layoutParams)
    }

    private fun createCardLayout(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
            background = GradientDrawable().apply {
                setColor(COLOR_BG_CARD)
                cornerRadius = CORNER_RADIUS_CARD * resources.displayMetrics.density
                setStroke(1.dpToPx(), COLOR_BORDER)
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun setCardMargin(card: View, left: Int, top: Int, right: Int, bottom: Int) {
        (card.layoutParams as? ViewGroup.MarginLayoutParams)?.setMargins(left, top, right, bottom)
    }

    private fun createFpsStatsRow(): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        avgFpsText = createStatItem("平均", "0")
        minFpsText = createStatItem("最小", "0")
        maxFpsText = createStatItem("最大", "0")
        jankCountText = createStatItem("卡顿", "0")

        row.addView(avgFpsText!!.parent as LinearLayout)
        row.addView(minFpsText!!.parent as LinearLayout)
        row.addView(maxFpsText!!.parent as LinearLayout)
        row.addView(jankCountText!!.parent as LinearLayout)

        return row
    }

    private fun createStatItem(label: String, value: String): TextView {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            )
            setPadding(4.dpToPx(), 4.dpToPx(), 4.dpToPx(), 4.dpToPx())
        }

        val labelTv = TextView(this).apply {
            text = label
            textSize = 11f
            setTextColor(COLOR_TEXT_SECONDARY)
            gravity = Gravity.CENTER
        }
        container.addView(labelTv)

        val valueTv = TextView(this).apply {
            text = value
            textSize = 18f
            setTextColor(COLOR_TEXT_PRIMARY)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, 2.dpToPx(), 0, 0)
        }
        container.addView(valueTv)

        return valueTv
    }

    // ── Backgrounds ──

    private fun createCollapsedBackground(bgColor: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = CORNER_RADIUS_COLLAPSED * resources.displayMetrics.density
            setColor(bgColor)
        }
    }

    private fun createExpandedPanelBackground(bgColor: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 16f * resources.displayMetrics.density
            setColor(bgColor)
            setStroke(1.dpToPx(), COLOR_BORDER)
        }
    }

    // ── Expand / Collapse ──

    private fun toggleExpanded() {
        if (toggleInProgress) return
        toggleInProgress = true
        try {
            isExpanded = !isExpanded
            collapsedLayout?.visibility = if (isExpanded) View.GONE else View.VISIBLE
            expandedLayout?.visibility = if (isExpanded) View.VISIBLE else View.GONE
            overlayView?.let { windowManager.updateViewLayout(it, layoutParams) }
            if (isExpanded && !deviceInfoPopulated) {
                populateDeviceInfo()
            }
        } catch (_: Exception) {
        } finally {
            toggleInProgress = false
        }
    }

    private fun removeOverlayView() {
        try { overlayView?.let { windowManager.removeView(it) } } catch (_: Exception) { }
        overlayView = null
    }

    // ── Device info ──

    private fun populateDeviceInfo() {
        if (deviceInfoPopulated) return
        deviceInfoPopulated = true
        val container = deviceInfoContainer ?: return

        val lastData = hardwareMonitor.hardwareData.value
        val di = lastData.deviceInfo

        val metrics = resources.displayMetrics
        val resolution = "${metrics.widthPixels}x${metrics.heightPixels}"
        val refreshRate = windowManager.defaultDisplay?.refreshRate ?: 0f
        val coreCount = Runtime.getRuntime().availableProcessors()

        val items = listOf(
            "设备型号" to di.model,
            "制造商" to di.manufacturer,
            "CPU 型号" to di.cpuModel,
            "CPU 架构" to di.cpuArch,
            "核心数" to "$coreCount",
            "总内存" to di.totalRam,
            "分辨率" to resolution,
            "刷新率" to "${refreshRate.toInt()} Hz"
        )

        for (i in items.indices step 2) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(4.dpToPx(), 4.dpToPx(), 4.dpToPx(), 4.dpToPx())
            }
            row.addView(createDeviceInfoCell(items[i].first, items[i].second))
            if (i + 1 < items.size) {
                row.addView(createDeviceInfoCell(items[i + 1].first, items[i + 1].second))
            }
            container.addView(row)
        }
    }

    private fun createDeviceInfoCell(label: String, value: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            )
            setPadding(4.dpToPx(), 2.dpToPx(), 4.dpToPx(), 2.dpToPx())

            addView(TextView(this@FpsOverlayService).apply {
                text = label
                textSize = 12f
                setTextColor(COLOR_TEXT_SECONDARY)
            })

            addView(TextView(this@FpsOverlayService).apply {
                text = value.ifEmpty { "未知" }
                textSize = 13f
                setTextColor(COLOR_TEXT_PRIMARY)
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            })
        }
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
        collapsedFpsText?.apply { text = "$fps" }
        bigFpsText?.apply { text = "$fps" }

        avgFpsText?.text = "%.1f".format(data.avgFps)
        minFpsText?.text = "${data.minFps}"
        maxFpsText?.text = "${data.maxFps}"
        jankCountText?.text = "${data.jankCount}"

        // Track frame time history for chart
        if (data.frameTimeMs > 0f) {
            frameTimeHistory.add(data.frameTimeMs)
            if (frameTimeHistory.size > MAX_HISTORY) frameTimeHistory.removeAt(0)
            frameTimeChartView?.updateData(frameTimeHistory.toList())
        }
    }

    private fun updateHardwareDisplay(data: HardwareData) {
        if (!isExpanded) return

        // Populate device info once
        if (!deviceInfoPopulated) {
            populateDeviceInfo()
        }

        // Track CPU temp history
        cpuTempHistory.add(data.cpuTemp)
        if (cpuTempHistory.size > MAX_HISTORY) cpuTempHistory.removeAt(0)

        // Update temperature
        updateTemperatureDisplay(data)
        tempTrendChartView?.updateData(cpuTempHistory.toList())

        // Update CPU core controls
        updateCpuCoreControls(data)

        // Always update CPU freq chart
        if (data.cpuFreqs.isNotEmpty()) {
            cpuFreqHistory.add(data.cpuFreqs)
            if (cpuFreqHistory.size > MAX_HISTORY) cpuFreqHistory.removeAt(0)
            cpuFreqChartView?.updateData(cpuFreqHistory.toList(), data.cpuFreqs.size)
        }
    }

    private fun updateTemperatureDisplay(data: HardwareData) {
        // CPU temp
        val cpuTemp = data.cpuTemp
        tempCpuText?.apply {
            text = "${cpuTemp.toInt()}°C"
        }

        // GPU temp
        val gpuTemp = data.gpuTemp
        tempGpuText?.apply {
            text = if (gpuTemp > 0f) "GPU: ${gpuTemp.toInt()}°C" else "GPU: --"
        }

        // Battery temp
        val batTemp = data.batteryTemp
        tempBatText?.apply {
            text = "电池: ${batTemp.toInt()}°C"
        }
    }

    // ── CPU core controls ──

    private fun updateCpuCoreControls(data: HardwareData) {
        val container = cpuCoreContainer ?: return
        val cores = data.cpuCores

        for (view in cpuCoreViews) { container.removeView(view) }
        cpuCoreViews.clear()

        val list = if (cores.isNotEmpty()) cores
        else data.cpuFreqs.mapIndexed { i, f -> CpuCoreInfo(i, f, "", emptyList()) }

        for (core in list) {
            val row = createCoreRow(core)
            container.addView(row)
            cpuCoreViews.add(row)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun createCoreRow(core: CpuCoreInfo): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(6.dpToPx(), 6.dpToPx(), 6.dpToPx(), 6.dpToPx())
            gravity = Gravity.CENTER_VERTICAL
            (layoutParams as? LinearLayout.LayoutParams)?.setMargins(0, 0, 0, 4.dpToPx())
        }

        val coreIdText = TextView(this).apply {
            text = "C${core.coreId}"
            textSize = 12f
            setTextColor(COLOR_TEXT_PRIMARY)
            setPadding(0, 0, 6.dpToPx(), 0)
            layoutParams = LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            )
        }
        row.addView(coreIdText)

        val freqText = TextView(this).apply {
            text = "${core.freqMHz}MHz"
            textSize = 11f
            setTextColor(COLOR_TEXT_SECONDARY)
            setPadding(0, 0, 6.dpToPx(), 0)
            layoutParams = LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.5f
            )
        }
        row.addView(freqText)

        // Governor button
        val govBtn = TextView(this).apply {
            text = if (core.governor.isNotEmpty()) core.governor else "--"
            textSize = 10f
            setPadding(8.dpToPx(), 4.dpToPx(), 8.dpToPx(), 4.dpToPx())
            setTextColor(COLOR_BUTTON_TEXT)
            background = GradientDrawable().apply {
                setColor(COLOR_BUTTON_BG)
                cornerRadius = 6f * resources.displayMetrics.density
            }
            if (core.availableGovernors.isNotEmpty()) {
                setOnClickListener { showGovernorPopup(core.coreId, core.availableGovernors, this) }
            }
        }
        row.addView(govBtn)
        (govBtn.layoutParams as LinearLayout.LayoutParams).setMargins(0, 0, 4.dpToPx(), 0)

        // Custom frequency button
        val freqBtn = TextView(this).apply {
            text = "自定义"
            textSize = 10f
            setPadding(8.dpToPx(), 4.dpToPx(), 8.dpToPx(), 4.dpToPx())
            setTextColor(COLOR_BUTTON_TEXT)
            background = GradientDrawable().apply {
                setColor(COLOR_BUTTON_BG)
                cornerRadius = 6f * resources.displayMetrics.density
            }
            setOnClickListener { showFreqInputPopup(core.coreId) }
        }
        row.addView(freqBtn)

        return row
    }

    // ── Governor popup ──

    private fun showGovernorPopup(coreId: Int, governors: List<String>, anchor: View) {
        try {
            val listView = ListView(this).apply {
                adapter = object : ArrayAdapter<String>(this@FpsOverlayService,
                    android.R.layout.simple_list_item_1, governors) {
                    override fun getView(pos: Int, v: View?, parent: ViewGroup): View {
                        val tv = super.getView(pos, v, parent) as TextView
                        tv.setPadding(24, 16, 24, 16)
                        tv.setTextColor(COLOR_TEXT_PRIMARY)
                        return tv
                    }
                }
                setBackgroundColor(Color.WHITE)
            }

            val popup = PopupWindow(listView, 240.dpToPx(), ViewGroup.LayoutParams.WRAP_CONTENT, true).apply {
                setBackgroundDrawable(GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = 12f * resources.displayMetrics.density
                    setStroke(1.dpToPx(), COLOR_BORDER)
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

            popup.showAsDropDown(anchor, 0, -180.dpToPx())
        } catch (_: Exception) { }
    }

    // ── Frequency input popup ──

    private fun showFreqInputPopup(coreId: Int) {
        try {
            val editText = EditText(this).apply {
                hint = "输入 MHz (如 1800)"
                setTextColor(COLOR_TEXT_PRIMARY)
                setHintTextColor(COLOR_TEXT_SECONDARY)
                setPadding(24.dpToPx(), 16.dpToPx(), 24.dpToPx(), 16.dpToPx())
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
                background = GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = 8f * resources.displayMetrics.density
                    setStroke(1.dpToPx(), COLOR_BORDER)
                }
            }

            val popup = PopupWindow(editText, 240.dpToPx(), ViewGroup.LayoutParams.WRAP_CONTENT, true).apply {
                setBackgroundDrawable(GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = 12f * resources.displayMetrics.density
                    setStroke(1.dpToPx(), COLOR_BORDER)
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
                    if (!isDragging && (Math.abs(dx) > 10f || Math.abs(dy) > 10f)) {
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
                        return true
                    }
                    return false
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) {
                        view.performClick()
                    }
                    if (isDragging) {
                        return true
                    }
                    return false
                }
            }
            return false
        }
    }
}
