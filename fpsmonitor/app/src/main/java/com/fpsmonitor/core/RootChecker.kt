package com.fpsmonitor.core

import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * RootChecker - Detects if the device has root access.
 * Based on the approach used by Scene (TOOLBOX-SCENE) and TakoStats.
 * Uses libsu to check for root shell access.
 */
object RootChecker {

    private var _hasRoot = false
    private var _rootMethod = "Unknown"

    val hasRoot: Boolean get() = _hasRoot
    val rootMethod: String get() = _rootMethod

    /**
     * Check root access by attempting to execute "su -c id".
     * Returns true if uid=0 (root), false otherwise.
     */
    suspend fun checkRoot(): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = Shell.cmd("id").exec()
            val output = result.out.joinToString("\n")
            _hasRoot = output.contains("uid=0")
            if (_hasRoot) {
                detectRootMethod()
            }
        } catch (e: Exception) {
            _hasRoot = false
        }
        _hasRoot
    }

    /**
     * Detect which root method is being used (Magisk, KernelSU, APatch).
     * References: Scene's root detection logic.
     */
    private fun detectRootMethod() {
        try {
            // Check Magisk
            val magiskResult = Shell.cmd("test -d /data/adb/magisk && echo MAGISK || echo NONE").exec()
            if (magiskResult.out.any { it.contains("MAGISK") }) {
                _rootMethod = "Magisk"
                return
            }

            // Check KernelSU
            val ksuResult = Shell.cmd("test -d /data/adb/ksu && echo KSU || echo NONE").exec()
            if (ksuResult.out.any { it.contains("KSU") }) {
                _rootMethod = "KernelSU"
                return
            }

            // Check APatch
            val apResult = Shell.cmd("test -d /data/adb/ap && echo APATCH || echo NONE").exec()
            if (apResult.out.any { it.contains("APATCH") }) {
                _rootMethod = "APatch"
                return
            }

            _rootMethod = "Generic Root"
        } catch (e: Exception) {
            _rootMethod = "Generic Root"
        }
    }

    /**
     * Grant SYSTEM_ALERT_WINDOW permission via root shell.
     * Reference: Scene's ADB mode and TakoStats' Shizuku approach,
     * adapted to use root directly.
     */
    suspend fun grantOverlayPermission(packageName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = Shell.cmd("appops set $packageName SYSTEM_ALERT_WINDOW allow").exec()
            result.isSuccess
        } catch (e: Exception) {
            false
        }
    }
}