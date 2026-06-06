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
    private const val KEY_COLLAPSED_BG_COLOR = "collapsed_bg_color"
    private const val KEY_FPS_TEXT_COLOR = "fps_text_color"
    private const val KEY_CHART_LINE_COLOR = "chart_line_color"
    private const val KEY_PANEL_BG_COLOR = "panel_bg_color"

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

    var collapsedBgColor: String
        get() = requirePrefs().getString(KEY_COLLAPSED_BG_COLOR, "#1A1A1A") ?: "#1A1A1A"
        set(value) {
            requirePrefs().edit().putString(KEY_COLLAPSED_BG_COLOR, value).apply()
        }

    var fpsTextColor: String
        get() = requirePrefs().getString(KEY_FPS_TEXT_COLOR, "#1A1A1A") ?: "#1A1A1A"
        set(value) {
            requirePrefs().edit().putString(KEY_FPS_TEXT_COLOR, value).apply()
        }

    var chartLineColor: String
        get() = requirePrefs().getString(KEY_CHART_LINE_COLOR, "#1A1A1A") ?: "#1A1A1A"
        set(value) {
            requirePrefs().edit().putString(KEY_CHART_LINE_COLOR, value).apply()
        }

    var panelBgColor: String
        get() = requirePrefs().getString(KEY_PANEL_BG_COLOR, "#FFFFFF") ?: "#FFFFFF"
        set(value) {
            requirePrefs().edit().putString(KEY_PANEL_BG_COLOR, value).apply()
        }
}