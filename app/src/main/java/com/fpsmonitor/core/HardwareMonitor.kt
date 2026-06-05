package com.fpsmonitor.core

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * HardwareMonitor - Reads system hardware data via Root Shell.
 *
 * Reference: Scene's root mode (TOOLBOX-SCENE) hardware monitoring.
 * Scene reads /sys/class/ nodes for CPU freq, GPU load, battery, temperature.
 * TakoStats uses Shizuku for the same purpose; we use Root directly.
 *
 * Sysfs paths:
 * - CPU freq: /sys/devices/system/cpu/cpuX/cpufreq/scaling_cur_freq
 * - CPU usage: /proc/stat
 * - GPU load: /sys/class/kgsl/kgsl-3d0/gpu_busy_percentage (Qualcomm)
 * - Battery temp: /sys/class/power_supply/battery/temp
 * - Battery level: /sys/class/power_supply/battery/capacity
 * - CPU temp: /sys/class/thermal/thermal_zoneX/temp
 */
class HardwareMonitor {

    companion object {
        private const val UPDATE_INTERVAL_MS = 1000L
        private const val CPU_COUNT = 8 // Max CPU cores to check
    }

    private val _hardwareData = MutableStateFlow(HardwareData())
    val hardwareData: StateFlow<HardwareData> = _hardwareData.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isRunning = false

    // Previous CPU stats for usage calculation
    private var prevCpuTotal = 0L
    private var prevCpuIdle = 0L

    /**
     * Start hardware monitoring loop.
     */
    fun start() {
        if (isRunning) return
        isRunning = true
        scope.launch {
            while (isActive && isRunning) {
                updateHardwareData()
                delay(UPDATE_INTERVAL_MS)
            }
        }
    }

    /**
     * Stop hardware monitoring.
     */
    fun stop() {
        isRunning = false
        scope.cancel()
    }

    /**
     * Update all hardware data by reading sysfs nodes via Root Shell.
     * Reference: Scene's daemon mode reads /sys/class/ nodes periodically.
     */
    private suspend fun updateHardwareData() {
        val cpuFreqs = readCpuFrequencies()
        val cpuUsage = readCpuUsage()
        val gpuUsage = readGpuUsage()
        val batteryTemp = readBatteryTemp()
        val batteryLevel = readBatteryLevel()
        val cpuTemp = readCpuTemp()

        _hardwareData.value = HardwareData(
            cpuUsage = cpuUsage,
            cpuFreqs = cpuFreqs,
            gpuUsage = gpuUsage,
            batteryTemp = batteryTemp,
            batteryLevel = batteryLevel,
            cpuTemp = cpuTemp
        )
    }

    /**
     * Read CPU frequencies for all cores.
     * Path: /sys/devices/system/cpu/cpu{N}/cpufreq/scaling_cur_freq
     * Reference: Scene reads this for CPU frequency display.
     */
    private suspend fun readCpuFrequencies(): List<Long> {
        val freqs = mutableListOf<Long>()
        for (i in 0 until CPU_COUNT) {
            val path = "/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq"
            val value = ShellExecutor.readFile(path)
            if (value.isNotEmpty()) {
                try {
                    freqs.add(value.toLong() / 1000) // Convert KHz to MHz
                } catch (_: NumberFormatException) { }
            }
        }
        return freqs
    }

    /**
     * Calculate CPU usage from /proc/stat.
     * Reads total and idle ticks, computes delta from previous reading.
     * Reference: Standard Linux CPU usage calculation.
     */
    private suspend fun readCpuUsage(): Float {
        try {
            val stat = ShellExecutor.readFile("/proc/stat")
            val cpuLine = stat.lines().firstOrNull { it.startsWith("cpu ") } ?: return 0f
            val fields = cpuLine.split("\\s+".toRegex()).drop(1).map { it.toLongOrNull() ?: 0L }
            if (fields.size < 4) return 0f

            val total = fields.sum()
            val idle = fields[3] // idle field (4th field, index 3)

            val totalDiff = total - prevCpuTotal
            val idleDiff = idle - prevCpuIdle

            prevCpuTotal = total
            prevCpuIdle = idle

            if (totalDiff <= 0) return 0f
            return ((totalDiff - idleDiff).toFloat() / totalDiff * 100f)
        } catch (e: Exception) {
            return 0f
        }
    }

    /**
     * Read GPU usage from Qualcomm Adreno GPU.
     * Path: /sys/class/kgsl/kgsl-3d0/gpu_busy_percentage
     * Reference: Scene reads this for GPU load on Qualcomm devices.
     */
    private suspend fun readGpuUsage(): Float {
        try {
            val value = ShellExecutor.readFile("/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage")
            if (value.isNotEmpty()) {
                return value.trim().replace("%", "").toFloatOrNull() ?: 0f
            }
            // Alternative for non-Qualcomm devices
            val altValue = ShellExecutor.readFile("/sys/kernel/gpu/gpu_usage")
            if (altValue.isNotEmpty()) {
                return altValue.trim().toFloatOrNull() ?: 0f
            }
        } catch (_: Exception) { }
        return 0f
    }

    /**
     * Read battery temperature.
     * Path: /sys/class/power_supply/battery/temp
     * Value is in tenths of degrees Celsius.
     * Reference: Scene reads battery temp for thermal monitoring.
     */
    private suspend fun readBatteryTemp(): Float {
        try {
            val value = ShellExecutor.readFile("/sys/class/power_supply/battery/temp")
            if (value.isNotEmpty()) {
                return (value.toFloatOrNull() ?: 0f) / 10f // Convert to °C
            }
        } catch (_: Exception) { }
        return 0f
    }

    /**
     * Read battery level percentage.
     * Path: /sys/class/power_supply/battery/capacity
     */
    private suspend fun readBatteryLevel(): Int {
        try {
            val value = ShellExecutor.readFile("/sys/class/power_supply/battery/capacity")
            if (value.isNotEmpty()) {
                return value.trim().toIntOrNull() ?: 0
            }
        } catch (_: Exception) { }
        return 0
    }

    /**
     * Read CPU temperature from thermal zones.
     * Scans /sys/class/thermal/thermal_zoneX/temp for CPU-related zones.
     * Reference: Scene scans thermal zones for CPU temperature.
     */
    private suspend fun readCpuTemp(): Float {
        try {
            for (i in 0..20) {
                // Check type first to find CPU thermal zone
                val type = ShellExecutor.readFile("/sys/class/thermal/thermal_zone$i/type")
                if (type.contains("cpu", ignoreCase = true) ||
                    type.contains("tsens", ignoreCase = true) ||
                    type.contains("soc", ignoreCase = true)) {
                    val temp = ShellExecutor.readFile("/sys/class/thermal/thermal_zone$i/temp")
                    if (temp.isNotEmpty()) {
                        return (temp.trim().toFloatOrNull() ?: 0f) / 1000f // Convert m°C to °C
                    }
                }
            }
            // Fallback: try zone0
            val temp = ShellExecutor.readFile("/sys/class/thermal/thermal_zone0/temp")
            if (temp.isNotEmpty()) {
                return (temp.trim().toFloatOrNull() ?: 0f) / 1000f
            }
        } catch (_: Exception) { }
        return 0f
    }
}