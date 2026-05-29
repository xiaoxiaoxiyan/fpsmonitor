package com.xtoolbox.core.root

import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.libsu.ShellUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ShellExecutor {
    fun exec(vararg commands: String): Shell.Result {
        return Shell.cmd(*commands).exec()
    }

    fun execFast(cmd: String): String {
        return ShellUtils.fastCmd(Shell.getShell(), cmd)
    }

    suspend fun execAsync(vararg commands: String): Shell.Result = withContext(Dispatchers.IO) {
        Shell.cmd(*commands).exec()
    }

    fun reboot() {
        Shell.cmd("reboot").submit()
    }

    fun softReboot() {
        Shell.cmd("setprop sys.powerctl reboot").submit()
    }

    fun rebootRecovery() {
        Shell.cmd("reboot recovery").submit()
    }

    fun rebootBootloader() {
        Shell.cmd("reboot bootloader").submit()
    }
}
