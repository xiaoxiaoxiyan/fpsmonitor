package com.fpsmonitor.core

import android.content.Context
import android.content.SharedPreferences

/**
 * SettingsManager - Persistent settings storage using SharedPreferences.
 * Settings are shared between the app UI and the overlay service.
 */
object SettingsManager {

    private const val PREFS_NAME = "fps_monitor_settings"
    private const val KEY_SAMPLE_INTERVAL = "sample_interval"
    private const val KEY_TARGET_FPS = "target_fps"
    private const val KEY_JANK_THRESHOLD = "jank_threshold"
    private const val KEY_SHOW_OVERLAY_ON_START = "show_overlay_on_start"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    private fun requirePrefs(): SharedPreferences {
        return prefs ?: throw IllegalStateException("SettingsManager not initialized. Call init() first.")
    }

    var sampleInterval: Long
        get() = requirePrefs().getLong(KEY_SAMPLE_INTERVAL, 250L)
        set(value) {
            requirePrefs().edit().putLong(KEY_SAMPLE_INTERVAL, value).apply()
        }

    var targetFps: Int
        get() = requirePrefs().getInt(KEY_TARGET_FPS, 60)
        set(value) {
            requirePrefs().edit().putInt(KEY_TARGET_FPS, value).apply()
        }

    var jankThreshold: Float
        get() = requirePrefs().getFloat(KEY_JANK_THRESHOLD, 1.5f)
        set(value) {
            requirePrefs().edit().putFloat(KEY_JANK_THRESHOLD, value).apply()
        }

    var showOverlayOnStart: Boolean
        get() = requirePrefs().getBoolean(KEY_SHOW_OVERLAY_ON_START, true)
        set(value) {
            requirePrefs().edit().putBoolean(KEY_SHOW_OVERLAY_ON_START, value).apply()
        }
}