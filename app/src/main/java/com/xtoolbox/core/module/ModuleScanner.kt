package com.xtoolbox.core.module

import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.libsu.ShellUtils
import java.io.File

object ModuleScanner {
    private const val MODULES_DIR = "/data/adb/modules"
    private const val MODULE_UPDATE_DIR = "/data/adb/modules_update"

    fun scanModules(): List<ModuleInfo> {
        val modules = mutableListOf<ModuleInfo>()
        val modulesDir = File(MODULES_DIR)

        if (!modulesDir.exists()) return modules

        val shell = Shell.getShell()
        val dirs = ShellUtils.fastCmd(shell, "ls $MODULES_DIR").trim().split("\n")

        for (dirName in dirs) {
            if (dirName.isBlank()) continue
            val modulePath = "$MODULES_DIR/$dirName"
            val propPath = "$modulePath/module.prop"

            val propExists = ShellUtils.fastCmd(shell, "test -f $propPath && echo 1 || echo 0").trim()
            if (propExists != "1") continue

            val propContent = ShellUtils.fastCmd(shell, "cat $propPath")
            val props = parseModuleProp(propContent)

            val disablePath = "$modulePath/disable"
            val isEnabled = ShellUtils.fastCmd(shell, "test -f $disablePath && echo 0 || echo 1").trim() == "1"

            val webUIPath = "$modulePath/webroot/index.html"
            val hasWebUI = ShellUtils.fastCmd(shell, "test -f $webUIPath && echo 1 || echo 0").trim() == "1"

            modules.add(
                ModuleInfo(
                    id = props["id"] ?: dirName,
                    name = props["name"] ?: dirName,
                    version = props["version"] ?: "未知",
                    author = props["author"] ?: "未知",
                    description = props["description"] ?: "",
                    path = modulePath,
                    isEnabled = isEnabled,
                    hasWebUI = hasWebUI,
                    webUIPath = if (hasWebUI) webUIPath else null
                )
            )
        }

        return modules
    }

    private fun parseModuleProp(content: String): Map<String, String> {
        val props = mutableMapOf<String, String>()
        for (line in content.lines()) {
            if (line.isBlank() || line.startsWith("#")) continue
            val parts = line.split("=", limit = 2)
            if (parts.size == 2) {
                props[parts[0].trim()] = parts[1].trim()
            }
        }
        return props
    }

    fun enableModule(modulePath: String) {
        Shell.cmd("rm -f $modulePath/disable").exec()
    }

    fun disableModule(modulePath: String) {
        Shell.cmd("touch $modulePath/disable").exec()
    }

    fun uninstallModule(modulePath: String) {
        Shell.cmd("touch $modulePath/remove").exec()
    }
}
