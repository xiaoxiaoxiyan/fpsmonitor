package com.fpsmonitor.core

/**
 * Data class representing FPS monitoring data.
 * Reference: Takt's listener callback data, fpsviewer's sampling data.
 */
data class FpsData(
    val fps: Int = 0,
    val avgFps: Float = 0f,
    val minFps: Int = Int.MAX_VALUE,
    val maxFps: Int = 0,
    val jankCount: Int = 0,
    val frameTimeMs: Float = 0f
)

/**
 * CPU core information including frequency and governor.
 */
data class CpuCoreInfo(
    val coreId: Int = 0,
    val freqMHz: Long = 0,
    val governor: String = "",
    val availableGovernors: List<String> = emptyList()
)

/**
 * Hardware monitoring data collected via root shell.
 * Reference: Scene's root mode hardware monitoring.
 */
data class HardwareData(
    val cpuUsage: Float = 0f,
    val cpuFreqs: List<Long> = emptyList(),
    val cpuCores: List<CpuCoreInfo> = emptyList(),
    val gpuUsage: Float = 0f,
    val batteryTemp: Float = 0f,
    val batteryLevel: Int = 0,
    val cpuTemp: Float = 0f,
    val gpuTemp: Float = 0f,
    val deviceInfo: DeviceInfo = DeviceInfo()
)

/**
 * Device information collected once at startup.
 */
data class DeviceInfo(
    val model: String = "",
    val manufacturer: String = "",
    val cpuModel: String = "",
    val cpuArch: String = "",
    val totalRam: String = "",
    val screenResolution: String = "",
    val screenRefreshRate: Float = 0f,
    val coreCount: Int = 0
)

/**
 * Recorded FPS history entry for chart display.
 * Reference: TakoStats Records, Scene frame rate recording.
 */
data class FpsRecord(
    val timestamp: Long = System.currentTimeMillis(),
    val fps: Int = 0
)

/**
 * A recording session containing FPS data history.
 */
data class RecordingSession(
    val id: Long = System.currentTimeMillis(),
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = System.currentTimeMillis(),
    val records: List<FpsRecord> = emptyList(),
    val avgFps: Float = 0f,
    val minFps: Int = 0,
    val maxFps: Int = 0,
    val totalJanks: Int = 0
)