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
import com.fpsmonitor.core.CpuCoreInfo
import com.fpsmonitor.core.FpsData
import com.fpsmonitor.core.FpsMonitor
import com.fpsmonitor.core.HardwareData
import com.fpsmonitor.core.HardwareMonitor
import com.fpsmonitor.core.SettingsManager
import kotlinx.coroutines.*

/**
 * FpsOverlayService - Multi-tab floating overlay with Teal color scheme.
 * - Collapsed: Dynamic Island style teal pill
 * - Expanded: 4-tab panel (设备/图表/温度/控制)
 */
class FpsOverlayService : Service() {

    companion object {
        private const val CHANNEL_ID = "fps_overlay"
        private const val NOTIFICATION_ID = 1
        const val FPS_GOOD = 50
        const val FPS_WARNING = 30
        private const val MAX_HISTORY = 30
    }

    // ── Color palette (Teal theme) ──
    private val TEAL_600 = Color.parseColor("#00897B")
    private val CYAN_600 = Color.parseColor("#00ACC1")
    private val BG_LIGHT = Color.parseColor("#F0F4F8")
    private val TEXT_SECONDARY = Color.parseColor("#607D8B")
    private val TEXT_PRIMARY = Color.parseColor("#263238")
    private val COLOR_AMBER = Color.parseColor("#FFA000")
    private val COLOR_RED = Color.parseColor("#E53935")

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
    private var selectedTab = 1
    private var tabIndicators: MutableList<View> = mutableListOf()
    private var tabContentViews: MutableList<View> = mutableListOf()

    // Tab 0 — Device info
    private var deviceInfoContainer: LinearLayout? = null
    private var deviceInfoPopulated = false

    // Tab 1 — Charts
    private var fpsChartView: FpsLineChartView? = null
    private var frameTimeChartView: FrameTimeChartView? = null
    private var cpuFreqChartView: CpuFreqChartView? = null

    // Tab 2 — Temperature
    private var tempCpuText: TextView? = null
    private var tempGpuText: TextView? = null
    private var tempBatText: TextView? = null
    private var tempTrendChartView: TempTrendChartView? = null

    // Tab 3 — Control
    private var refreshRateText: TextView? = null
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
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }

        // --- Collapsed: Teal Dynamic Island pill ---
        collapsedLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(28, 12, 28, 12)
            background = createCollapsedPillBackground()
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

        // --- Expanded: White panel with tabs ---
        expandedLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            setPadding(14, 10, 14, 10)
            background = createExpandedPanelBackground()
        }

        // Tab bar
        expandedLayout!!.addView(createTabBar())

        // Tab content frame
        val contentFrame = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        contentFrame.addView(createDeviceInfoTab())
        contentFrame.addView(createChartsTab())
        contentFrame.addView(createTemperatureTab())
        contentFrame.addView(createControlTab())

        // Apply initial tab selection
        selectTab(selectedTab)

        expandedLayout!!.addView(contentFrame)
        container.addView(expandedLayout)

        // Window params
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

    // ── Tab bar ──

    private fun createTabBar(): LinearLayout {
        val tabBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 6)
        }

        val tabNames = listOf("设备", "图表", "温度", "控制")
        tabIndicators.clear()

        for (i in tabNames.indices) {
            val tabItem = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
                )
            }

            val tabLabel = TextView(this).apply {
                text = tabNames[i]
                textSize = 13f
                setTextColor(TEXT_SECONDARY)
                gravity = Gravity.CENTER
                setPadding(8, 4, 8, 4)
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }

            val indicator = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 3
                )
                setBackgroundColor(if (i == selectedTab) TEAL_600 else Color.TRANSPARENT)
            }

            tabItem.addView(tabLabel)
            tabItem.addView(indicator)
            tabIndicators.add(indicator)

            val idx = i
            tabItem.setOnClickListener { selectTab(idx) }

            tabBar.addView(tabItem)
        }

        return tabBar
    }

    private fun selectTab(index: Int) {
        if (index < 0 || index >= tabContentViews.size) return
        selectedTab = index

        for (i in tabContentViews.indices) {
            tabContentViews[i].visibility = if (i == index) View.VISIBLE else View.GONE
        }

        for (i in tabIndicators.indices) {
            tabIndicators[i].setBackgroundColor(if (i == index) TEAL_600 else Color.TRANSPARENT)
        }

        // Update tab label colors
        val tabBar = tabIndicators.firstOrNull()?.parent?.parent as? LinearLayout
        tabBar?.let { bar ->
            for (i in 0 until bar.childCount) {
                val item = bar.getChildAt(i) as? LinearLayout ?: continue
                val label = item.getChildAt(0) as? TextView ?: continue
                label.setTextColor(if (i == index) TEAL_600 else TEXT_SECONDARY)
            }
        }
    }

    // ── Tab 0: Device info ──

    private fun createDeviceInfoTab(): View {
        deviceInfoContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            setPadding(4, 4, 4, 4)
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 12f
                setStroke(1.dpToPx(), Color.parseColor("#E0E8ED"))
            }
        }

        // Header
        val header = TextView(this).apply {
            text = "设备信息"
            textSize = 14f
            setTextColor(TEAL_600)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(12, 10, 12, 8)
            background = GradientDrawable().apply {
                setColor(Color.TRANSPARENT)
            }
        }
        deviceInfoContainer!!.addView(header)

        // Separator
        deviceInfoContainer!!.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1
            )
            setBackgroundColor(Color.parseColor("#E0E8ED"))
        })

        tabContentViews.add(deviceInfoContainer!!)
        return deviceInfoContainer!!
    }

    private fun populateDeviceInfo() {
        if (deviceInfoPopulated) return
        deviceInfoPopulated = true
        val container = deviceInfoContainer ?: return

        val lastData = hardwareMonitor.hardwareData.value
        val di = lastData.deviceInfo

        // Supplement with Android API data
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
            "屏幕分辨率" to resolution,
            "屏幕刷新率" to "${refreshRate.toInt()} Hz"
        )

        for ((label, value) in items) {
            val row = createDeviceInfoRow(label, value)
            container.addView(row)
        }
    }

    private fun createDeviceInfoRow(label: String, value: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(12, 6, 12, 6)
            gravity = Gravity.CENTER_VERTICAL

            addView(TextView(this@FpsOverlayService).apply {
                text = label
                textSize = 12f
                setTextColor(TEXT_SECONDARY)
                layoutParams = LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.4f
                )
            })

            addView(TextView(this@FpsOverlayService).apply {
                text = value.ifEmpty { "未知" }
                textSize = 12f
                setTextColor(TEXT_PRIMARY)
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.6f
                )
            })
        }
    }

    // ── Tab 1: Charts ──

    private fun createChartsTab(): View {
        val scrollView = ScrollView(this).apply {
            visibility = View.GONE
        }

        val chartContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(4, 4, 4, 4)
        }

        fpsChartView = FpsLineChartView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 250.dpToPx()
            ).apply { bottomMargin = 6.dpToPx() }
        }
        chartContainer.addView(fpsChartView)

        frameTimeChartView = FrameTimeChartView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 200.dpToPx()
            ).apply { bottomMargin = 6.dpToPx() }
        }
        chartContainer.addView(frameTimeChartView)

        cpuFreqChartView = CpuFreqChartView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 160.dpToPx()
            )
        }
        chartContainer.addView(cpuFreqChartView)

        scrollView.addView(chartContainer)
        tabContentViews.add(scrollView)
        return scrollView
    }

    // ── Tab 2: Temperature ──

    private fun createTemperatureTab(): View {
        val tempLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            setPadding(4, 4, 4, 4)
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 12f
                setStroke(1.dpToPx(), Color.parseColor("#E0E8ED"))
            }
        }

        // CPU temp big display
        tempCpuText = TextView(this).apply {
            text = "0°C"
            textSize = 40f
            setTextColor(TEAL_600)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(12, 8, 12, 4)
        }
        tempLayout.addView(tempCpuText)

        val cpuLabel = TextView(this).apply {
            text = "CPU 温度"
            textSize = 12f
            setTextColor(TEXT_SECONDARY)
            gravity = Gravity.CENTER
            setPadding(12, 0, 12, 8)
        }
        tempLayout.addView(cpuLabel)

        // Separator
        tempLayout.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1
            )
            setBackgroundColor(Color.parseColor("#E0E8ED"))
        })

        // GPU temp
        val gpuRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(12, 6, 12, 2)
            gravity = Gravity.CENTER_VERTICAL
        }
        gpuRow.addView(TextView(this).apply {
            text = "GPU: "
            textSize = 12f
            setTextColor(TEXT_SECONDARY)
        })
        tempGpuText = TextView(this).apply {
            text = "--"
            textSize = 18f
            setTextColor(TEAL_600)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        gpuRow.addView(tempGpuText)
        tempLayout.addView(gpuRow)

        // Battery temp
        val batRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(12, 2, 12, 6)
            gravity = Gravity.CENTER_VERTICAL
        }
        batRow.addView(TextView(this).apply {
            text = "电池: "
            textSize = 12f
            setTextColor(TEXT_SECONDARY)
        })
        tempBatText = TextView(this).apply {
            text = "--"
            textSize = 18f
            setTextColor(TEAL_600)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        batRow.addView(tempBatText)
        tempLayout.addView(batRow)

        // Temp trend chart
        tempTrendChartView = TempTrendChartView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 160.dpToPx()
            ).apply { topMargin = 4.dpToPx() }
        }
        tempLayout.addView(tempTrendChartView)

        tabContentViews.add(tempLayout)
        return tempLayout
    }

    // ── Tab 3: Control ──

    private fun createControlTab(): View {
        val controlLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            setPadding(4, 4, 4, 4)
        }

        // Refresh rate display
        refreshRateText = TextView(this).apply {
            text = "60 Hz"
            textSize = 24f
            setTextColor(TEAL_600)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(12, 8, 12, 8)
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 12f
                setStroke(1.dpToPx(), Color.parseColor("#E0E8ED"))
            }
        }
        controlLayout.addView(refreshRateText)

        // CPU core controls (scrollable)
        val coreScroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            ).apply { topMargin = 6.dpToPx() }
        }
        cpuCoreContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(4, 0, 4, 0)
        }
        coreScroll.addView(cpuCoreContainer)
        controlLayout.addView(coreScroll)

        tabContentViews.add(controlLayout)
        return controlLayout
    }

    // ── Backgrounds ──

    private fun createCollapsedPillBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 48f
            setColor(Color.argb(200, 0, 137, 123))
            setStroke(1.dpToPx(), Color.argb(60, 255, 255, 255))
        }
    }

    private fun createExpandedPanelBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 20f
            setColor(Color.WHITE)
            setStroke(1.dpToPx(), Color.parseColor("#D0D8E0"))
        }
    }

    // ── Expand / Collapse ──

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
            fps >= FPS_GOOD -> TEAL_600
            fps >= FPS_WARNING -> COLOR_AMBER
            else -> COLOR_RED
        }
        collapsedFpsText?.apply { text = "$fps"; setTextColor(Color.WHITE) }

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
        if (selectedTab == 0 && !deviceInfoPopulated) {
            populateDeviceInfo()
        }

        // Track CPU temp history
        cpuTempHistory.add(data.cpuTemp)
        if (cpuTempHistory.size > MAX_HISTORY) cpuTempHistory.removeAt(0)

        when (selectedTab) {
            2 -> {
                updateTemperatureDisplay(data)
                tempTrendChartView?.updateData(cpuTempHistory.toList())
            }
            3 -> updateCpuCoreControls(data)
        }

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
            setTextColor(getTempColor(cpuTemp))
        }

        // GPU temp
        val gpuTemp = data.gpuTemp
        tempGpuText?.apply {
            text = if (gpuTemp > 0f) "${gpuTemp.toInt()}°C" else "--"
            setTextColor(getTempColor(gpuTemp))
        }

        // Battery temp
        val batTemp = data.batteryTemp
        tempBatText?.apply {
            text = "${batTemp.toInt()}°C"
            setTextColor(getTempColor(batTemp))
        }

        // Refresh rate
        val rr = windowManager.defaultDisplay?.refreshRate ?: 0f
        refreshRateText?.text = "${rr.toInt()} Hz"
    }

    private fun getTempColor(temp: Float): Int = when {
        temp <= 0f -> TEAL_600
        temp < 40f -> TEAL_600
        temp <= 60f -> COLOR_AMBER
        else -> COLOR_RED
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
            setPadding(6, 3, 6, 3)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#FAFAFA"))
                cornerRadius = 8f
                setStroke(1, Color.parseColor("#E0E0E0"))
            }
            gravity = Gravity.CENTER_VERTICAL
            (layoutParams as? LinearLayout.LayoutParams)?.setMargins(0, 0, 0, 3)
        }

        val infoText = TextView(this).apply {
            text = "C${core.coreId}"
            textSize = 11f
            setTextColor(TEXT_PRIMARY)
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

        // Governor button (Teal style)
        val govBtn = TextView(this).apply {
            text = if (core.governor.isNotEmpty()) core.governor else "--"
            textSize = 10f
            setPadding(8, 3, 8, 3)
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                setColor(TEAL_600)
                cornerRadius = 10f
            }
            if (core.availableGovernors.isNotEmpty()) {
                setOnClickListener { showGovernorPopup(core.coreId, core.availableGovernors, this) }
            }
        }
        row.addView(govBtn)

        // Custom frequency button (gear)
        val freqBtn = TextView(this).apply {
            text = "\u2699"
            textSize = 14f
            setPadding(8, 3, 8, 3)
            setTextColor(TEXT_SECONDARY)
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
                        tv.setTextColor(TEXT_PRIMARY)
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
        } catch (_: Exception) { }
    }

    // ── Frequency input popup ──

    private fun showFreqInputPopup(coreId: Int) {
        try {
            val editText = EditText(this).apply {
                hint = "输入MHz (如 1800)"
                setTextColor(TEXT_PRIMARY)
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