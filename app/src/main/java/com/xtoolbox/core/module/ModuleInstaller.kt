package com.xtoolbox.core.module

import android.content.Context
import com.topjohnwu.superuser.Shell
import java.io.File

object ModuleInstaller {
    fun installModule(zipPath: String): Boolean {
        val result = Shell.cmd("magisk --install-module \"$zipPath\"").exec()
        if (result.isSuccess) return true

        return try {
            val tmpDir = "/data/local/tmp/module_install"
            Shell.cmd("rm -rf $tmpDir", "mkdir -p $tmpDir").exec()

            Shell.cmd("unzip -o \"$zipPath\" -d $tmpDir").exec()

            val moduleProp = Shell.cmd("cat $tmpDir/module.prop").exec().out.joinToString("\n")
            if (moduleProp.isBlank()) {
                Shell.cmd("rm -rf $tmpDir").exec()
                return false
            }

            val moduleId = moduleProp.lines()
                .firstOrNull { it.startsWith("id=") }
                ?.substringAfter("id=")?.trim() ?: return false

            val modulesDir = "/data/adb/modules"
            Shell.cmd(
                "mkdir -p $modulesDir/$moduleId",
                "cp -r $tmpDir/* $modulesDir/$moduleId/",
                "chmod -R 755 $modulesDir/$moduleId",
                "rm -rf $tmpDir"
            ).exec().isSuccess
        } catch (_: Exception) {
            false
        }
    }
}
