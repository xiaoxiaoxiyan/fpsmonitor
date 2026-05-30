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

    fun getRootMethodVersion(): String {
        val shell = Shell.getShell()
        if (!shell.isRoot) return ""

        val method = getRootMethod()
        return when (method) {
            RootMethod.KERNELSU, RootMethod.KERNELSU_NEXT, RootMethod.SUKISU_ULTRA -> {
                val version = ShellUtils.fastCmd(shell, "cat /proc/version 2>/dev/null | grep -io 'kernelsu[^ ]*' | head -1")
                if (version.isNotBlank()) version else ShellUtils.fastCmd(shell, "ksud -V 2>/dev/null || echo ''").trim()
            }
            RootMethod.MAGISK -> {
                val version = ShellUtils.fastCmd(shell, "magisk -v 2>/dev/null").trim()
                val versionCode = ShellUtils.fastCmd(shell, "magisk -V 2>/dev/null").trim()
                if (version.isNotBlank()) "$version ($versionCode)" else ""
            }
            RootMethod.APATCH -> {
                ShellUtils.fastCmd(shell, "cat /data/adb/apd/version 2>/dev/null || echo ''").trim()
            }
            RootMethod.NONE -> ""
        }
    }

    fun getSuCompatStatus(): String {
        val shell = Shell.getShell()
        if (!shell.isRoot) return "不可用"
        val result = ShellUtils.fastCmd(shell, "ksud -s 2>/dev/null || echo ''").trim()
        return if (result.isNotBlank()) result else "不可用"
    }

    fun getSuperUserCount(): Int {
        val shell = Shell.getShell()
        if (!shell.isRoot) return 0
        val method = getRootMethod()
        return when (method) {
            RootMethod.KERNELSU, RootMethod.KERNELSU_NEXT, RootMethod.SUKISU_ULTRA -> {
                val count = ShellUtils.fastCmd(shell, "ls /data/adb/ksu/modules 2>/dev/null | wc -l").trim()
                count.toIntOrNull() ?: 0
            }
            RootMethod.MAGISK -> {
                val count = ShellUtils.fastCmd(shell, "ls /data/adb/magisk/../*/su 2>/dev/null | wc -l").trim()
                count.toIntOrNull() ?: 0
            }
            else -> 0
        }
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

    fun getSusfsStatus(): String {
        val shell = Shell.getShell()
        if (!shell.isRoot) return "不可用"
        val result = ShellUtils.fastCmd(shell, "cat /proc/susfs 2>/dev/null | head -1 || echo ''").trim()
        return if (result.isNotBlank()) "已启用" else "未启用"
    }
}
