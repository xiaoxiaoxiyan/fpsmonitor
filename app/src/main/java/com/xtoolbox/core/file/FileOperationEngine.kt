package com.xtoolbox.core.file

import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.libsu.ShellUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val permissions: String,
    val lastModified: Long
)

object FileOperationEngine {
    fun listFiles(path: String): List<FileItem> {
        val shell = Shell.getShell()
        val result = Shell.cmd("ls -la \"$path\"").exec()

        if (!result.isSuccess) return emptyList()

        val items = mutableListOf<FileItem>()
        for (line in result.out) {
            if (line.startsWith("total") || line.isBlank()) continue
            val parts = line.split(Regex("\\s+"), limit = 9)
            if (parts.size < 9) continue

            val perms = parts[0]
            val name = parts[8]
            if (name == "." || name == "..") continue

            val isDir = perms.startsWith("d") || perms.startsWith("l")
            val size = if (isDir) 0L else parts[4].toLongOrNull() ?: 0L
            val fullPath = if (path.endsWith("/")) "$path$name" else "$path/$name"

            items.add(
                FileItem(
                    name = name,
                    path = fullPath,
                    isDirectory = isDir,
                    size = size,
                    permissions = perms,
                    lastModified = 0L
                )
            )
        }

        return items.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    }

    fun copyFile(src: String, dest: String): Boolean {
        return Shell.cmd("cp -r \"$src\" \"$dest\"").exec().isSuccess
    }

    fun moveFile(src: String, dest: String): Boolean {
        return Shell.cmd("mv \"$src\" \"$dest\"").exec().isSuccess
    }

    fun deleteFile(path: String): Boolean {
        return Shell.cmd("rm -rf \"$path\"").exec().isSuccess
    }

    fun renameFile(oldPath: String, newName: String): Boolean {
        val parent = oldPath.substringBeforeLast("/")
        val newPath = "$parent/$newName"
        return Shell.cmd("mv \"$oldPath\" \"$newPath\"").exec().isSuccess
    }

    fun createDirectory(path: String): Boolean {
        return Shell.cmd("mkdir -p \"$path\"").exec().isSuccess
    }

    fun changePermissions(path: String, permissions: String): Boolean {
        if (!permissions.matches(Regex("[0-7]{3,4}"))) return false
        return Shell.cmd("chmod $permissions \"$path\"").exec().isSuccess
    }

    fun executeScript(path: String): Shell.Result {
        val validated = File(path)
        if (!validated.exists() || !validated.absolutePath == path) {
            return Shell.Result()
        }
        return Shell.cmd("sh \"$path\"").exec()
    }

    private fun sanitizePath(path: String): String {
        return path.replace(Regex("[;|&`$]"), "")
    }
}
