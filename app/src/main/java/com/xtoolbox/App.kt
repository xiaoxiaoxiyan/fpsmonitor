package com.xtoolbox

import android.app.Application
import com.topjohnwu.superuser.Shell

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Shell.enableVerboseLogging = BuildConfig.DEBUG
        Shell.setDefaultBuilder(
            Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(30)
        )
        autoGrantStoragePermissions()
    }

    private fun autoGrantStoragePermissions() {
        Shell.getShell { shell ->
            if (shell.isRoot) {
                val packageName = applicationContext.packageName
                Shell.cmd(
                    "pm grant $packageName android.permission.READ_EXTERNAL_STORAGE",
                    "pm grant $packageName android.permission.WRITE_EXTERNAL_STORAGE",
                    "pm grant $packageName android.permission.MANAGE_EXTERNAL_STORAGE"
                ).submit()
            }
        }
    }
}
