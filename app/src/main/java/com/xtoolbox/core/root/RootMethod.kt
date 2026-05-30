package com.xtoolbox.core.root

enum class RootMethod(val displayName: String) {
    KERNELSU("KernelSU"),
    KERNELSU_NEXT("KernelSU-Next"),
    SUKISU_ULTRA("SukiSU-Ultra"),
    APATCH("APatch"),
    MAGISK("Magisk"),
    NONE("无");

    companion object {
        fun detect(): RootMethod {
            val shell = com.topjohnwu.superuser.Shell.getShell()
            if (!shell.isRoot) return NONE

            val ksud = com.topjohnwu.superuser.libsu.ShellUtils.fastCmd(shell, "test -e /data/adb/ksud && echo 1 || echo 0")
            if (ksud.trim() == "1") {
                val ksuNext = com.topjohnwu.superuser.libsu.ShellUtils.fastCmd(shell, "test -d /data/adb/ksu-next && echo 1 || echo 0")
                if (ksuNext.trim() == "1") return KERNELSU_NEXT

                val sukiSU = com.topjohnwu.superuser.libsu.ShellUtils.fastCmd(shell, "test -f /data/adb/sukisu && echo 1 || echo 0")
                if (sukiSU.trim() == "1") return SUKISU_ULTRA

                return KERNELSU
            }

            val apd = com.topjohnwu.superuser.libsu.ShellUtils.fastCmd(shell, "test -e /data/adb/apd && echo 1 || echo 0")
            if (apd.trim() == "1") return APATCH

            val magisk = com.topjohnwu.superuser.libsu.ShellUtils.fastCmd(shell, "test -e /data/adb/magisk && echo 1 || echo 0")
            if (magisk.trim() == "1") return MAGISK

            return NONE
        }
    }
}
