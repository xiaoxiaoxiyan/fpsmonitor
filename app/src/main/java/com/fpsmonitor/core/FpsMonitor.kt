package com.fpsmonitor.core

import android.os.Build
import android.util.Log
import android.view.Choreographer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * FpsMonitor - Core FPS monitoring using Choreographer.FrameCallback.
 *
 * Reference implementations:
 * - Takt (https://github.com/wasabeef/takt): Choreographer-based FPS measurement,
 *   interval-based sampling (default 250ms), customizable display.
 * - TinyDancer (https://github.com/friendlyrobotnyc/TinyDancer): Choreographer
 *   frame callback, dropped frame percentage, color-coded display.
 * - fpsviewer (https://github.com/SilenceDut/fpsviewer): Choreographer.FrameCallback,
 *   async sampling, jank detection with threshold, frame time analysis.
 *
 * Design:
 * - Register a Choreographer.FrameCallback to count frames
 * - Every interval (default 250ms), calculate FPS = frameCount / elapsedSeconds
 * - Detect jank: frameTimeNanos diff > 16.67ms (60fps target) × threshold
 * - Emit FpsData via StateFlow for reactive UI updates
 */
class FpsMonitor {

    companion object {
        private const val TAG = "FpsMonitor"
        // 60fps = 16.67ms per frame in nanoseconds
        private const val FRAME_INTERVAL_NANOS = 16_666_666L
        // Default sampling interval (matches Takt's default)
        private const val DEFAULT_INTERVAL_MS = 250L
    }

    // Configuration
    var intervalMs: Long = DEFAULT_INTERVAL_MS
    var targetFps: Int = 60
    var jankThreshold: Float = 1.5f  // fpsviewer's default: 1.5x frame time = jank
    private val targetFrameNanos: Long get() = 1_000_000_000L / targetFps

    // State
    private var frameCount = 0
    private var lastFrameTimeNanos = 0L
    private var lastIntervalTime = 0L
    private var isRunning = false

    // Jank tracking (fpsviewer style)
    private var totalJankCount = 0
    private var fpsHistory = mutableListOf<Int>()
    private var minFps = Int.MAX_VALUE
    private var maxFps = 0

    // FPS data flow
    private val _fpsData = MutableStateFlow(FpsData())
    val fpsData: StateFlow<FpsData> = _fpsData.asStateFlow()

    // Recording
    private val _records = MutableStateFlow<List<FpsRecord>>(emptyList())
    val records: StateFlow<List<FpsRecord>> = _records.asStateFlow()
    private var isRecording = false

    // FPS history for chart display (recent 60 samples)
    private val _fpsHistory = MutableStateFlow<List<FpsRecord>>(emptyList())
    val fpsHistory: StateFlow<List<FpsRecord>> = _fpsHistory.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Choreographer frame callback (same pattern as Takt/TinyDancer/fpsviewer)
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isRunning) return

            // Count this frame
            frameCount++

            // Detect jank: frame time exceeds threshold (fpsviewer approach)
            if (lastFrameTimeNanos > 0) {
                val elapsedNanos = frameTimeNanos - lastFrameTimeNanos
                if (elapsedNanos > targetFrameNanos * jankThreshold) {
                    totalJankCount++
                }
            }
            lastFrameTimeNanos = frameTimeNanos

            // Register next frame callback (Takt/TinyDancer pattern)
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    /**
     * Start FPS monitoring.
     * Posts the Choreographer frame callback and starts the sampling timer.
     * Reference: Takt's stock() method, TinyDancer's show() method.
     */
    fun start() {
        if (isRunning) return
        isRunning = true
        frameCount = 0
        lastFrameTimeNanos = 0L
        lastIntervalTime = System.currentTimeMillis()
        totalJankCount = 0
        fpsHistory.clear()
        minFps = Int.MAX_VALUE
        maxFps = 0

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Choreographer.getInstance().postFrameCallback(frameCallback)
        }

        // Start sampling timer (Takt's interval-based approach)
        scope.launch {
            while (isActive && isRunning) {
                delay(intervalMs)
                calculateFps()
            }
        }
    }

    /**
     * Stop FPS monitoring.
     */
    fun stop() {
        isRunning = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Choreographer.getInstance().removeFrameCallback(frameCallback)
        }
        scope.cancel()
    }

    /**
     * Start recording FPS history.
     */
    fun startRecording() {
        isRecording = true
        _records.value = emptyList()
    }

    /**
     * Stop recording and return the session.
     */
    fun stopRecording(): RecordingSession {
        isRecording = false
        val records = _records.value
        return RecordingSession(
            records = records,
            avgFps = if (records.isNotEmpty()) records.map { it.fps }.average().toFloat() else 0f,
            minFps = if (records.isNotEmpty()) records.minOf { it.fps } else 0,
            maxFps = if (records.isNotEmpty()) records.maxOf { it.fps } else 0,
            totalJanks = totalJankCount
        )
    }

    /**
     * Calculate FPS from accumulated frame count.
     * This is the core sampling logic from Takt:
     *   FPS = frameCount / (elapsedSeconds)
     * Runs every intervalMs.
     */
    private fun calculateFps() {
        val now = System.currentTimeMillis()
        val elapsedMs = now - lastIntervalTime
        if (elapsedMs <= 0) return

        val fps = ((frameCount.toFloat() / elapsedMs) * 1000f).toInt()
        val frameTimeMs = if (fps > 0) 1000f / fps else 0f

        // Update min/max
        if (fps < minFps) minFps = fps
        if (fps > maxFps) maxFps = fps

        // Track FPS history for average calculation
        fpsHistory.add(fps)
        if (fpsHistory.size > 100) fpsHistory.removeAt(0)

        val avgFps = if (fpsHistory.isNotEmpty()) fpsHistory.average().toFloat() else 0f

        // Emit FPS data
        _fpsData.value = FpsData(
            fps = fps,
            avgFps = avgFps,
            minFps = if (minFps == Int.MAX_VALUE) 0 else minFps,
            maxFps = maxFps,
            jankCount = totalJankCount,
            frameTimeMs = frameTimeMs
        )

        // Record if recording is active
        if (isRecording) {
            _records.value = _records.value + FpsRecord(timestamp = now, fps = fps)
        }

        // Update FPS history for chart (keep last 60 samples)
        val history = _fpsHistory.value.toMutableList()
        history.add(FpsRecord(timestamp = now, fps = fps))
        if (history.size > 60) history.removeAt(0)
        _fpsHistory.value = history

        // Reset for next interval
        frameCount = 0
        lastIntervalTime = now
    }

    /**
     * Reset all statistics.
     */
    fun resetStats() {
        totalJankCount = 0
        fpsHistory.clear()
        minFps = Int.MAX_VALUE
        maxFps = 0
    }
}