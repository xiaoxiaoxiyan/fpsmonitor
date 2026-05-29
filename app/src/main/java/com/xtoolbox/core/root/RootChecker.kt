package com.xtoolbox.core.root

import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.libsu.ShellUtils

object RootChecker {
    fun isRootAvailable(): Boolean {
        return Shell.getShell().isRoot
    }

    fun getRootMethod(): RootMethod {
        return RootMethod.detect()
    }

    fun getWorkMode(): String {
        val shell = Shell.getShell()
        if (!shell.isRoot) return "未知"

        val ksuVersion = ShellUtils.fastCmd(shell, "cat /proc/version 2>/dev/null | grep -i 'kernelsu' || echo ''")
        if (ksuVersion.isNotEmpty()) {
            val isGki = ShellUtils.fastCmd(shell, "getprop ro.boot.vbmeta.device_state 2>/dev/null")
            return if (isGki.isNotEmpty()) "GKI" else "LKM"
        }

        val kernelVersion = ShellUtils.fastCmd(shell, "uname -r")
        val hasGki = kernelVersion.contains("android", ignoreCase = true) ||
                kernelVersion.contains("generic", ignoreCase = true)

        return if (hasGki) "GKI" else "LKM"
    }

    fun getKernelVersion(): String {
        return ShellUtils.fastCmd(Shell.getShell(), "uname -r")
    }

    fun getAndroidVersion(): String {
        return android.os.Build.VERSION.RELEASE
    }

    fun getApiLevel(): Int {
        return android.os.Build.VERSION.SDK_INT
    }

    fun getSecurityPatch(): String {
        return ShellUtils.fastCmd(Shell.getShell(), "getprop ro.build.version.security_patch")
    }

    fun getDeviceModel(): String {
        return "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
    }

    fun getCpuInfo(): String {
        val shell = Shell.getShell()
        val cpuName = ShellUtils.fastCmd(shell, "cat /proc/cpuinfo 2>/dev/null | grep 'Hardware' | head -1 | sed 's/Hardware\\s*:\\s*//'")
        return if (cpuName.isNotBlank()) cpuName.trim() else android.os.Build.SOC_MODEL ?: "Unknown"
    }

    fun getRamSize(): String {
        val shell = Shell.getShell()
        val memTotal = ShellUtils.fastCmd(shell, "cat /proc/meminfo | grep MemTotal | awk '{print \$2}'")
        val memMb = memTotal.trim().toLongOrNull()?.div(1024) ?: 0
        return if (memMb >= 1024) "${memMb / 1024}GB" else "${memMb}MB"
    }
}
