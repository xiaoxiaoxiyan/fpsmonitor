package com.xtoolbox.core.file

import net.lingala.zip4j.ZipFile
import java.io.File

object ArchiveHelper {
    fun compressToZip(sourcePath: String, destZipPath: String, password: String? = null): Boolean {
        return try {
            val source = File(sourcePath)
            val zipFile = ZipFile(destZipPath, password?.toCharArray())
            if (source.isDirectory) {
                zipFile.addFolder(source)
            } else {
                zipFile.addFile(source)
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun extractZip(zipPath: String, destDir: String, password: String? = null): Boolean {
        return try {
            val zipFile = ZipFile(zipPath, password?.toCharArray())
            zipFile.extractAll(destDir)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun isZipFile(path: String): Boolean {
        return path.lowercase().endsWith(".zip")
    }

    fun isArchiveFile(path: String): Boolean {
        val lower = path.lowercase()
        return lower.endsWith(".zip") || lower.endsWith(".tar.gz") ||
                lower.endsWith(".tgz") || lower.endsWith(".tar") ||
                lower.endsWith(".gz") || lower.endsWith(".rar")
    }
}
