package com.xtoolbox.core.file

import net.lingala.zip4j.ZipFile
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream

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

    fun extractTarGz(tarGzPath: String, destDir: String): Boolean {
        return try {
            val dest = File(destDir)
            if (!dest.exists()) dest.mkdirs()

            FileInputStream(tarGzPath).use { fis ->
                BufferedInputStream(fis).use { bis ->
                    GzipCompressorInputStream(bis).use { gzis ->
                        TarArchiveInputStream(gzis).use { tis ->
                            var entry = tis.nextTarEntry
                            while (entry != null) {
                                val outputFile = File(dest, entry.name)
                                if (entry.isDirectory) {
                                    outputFile.mkdirs()
                                } else {
                                    outputFile.parentFile?.mkdirs()
                                    tis.transferTo(outputFile.outputStream())
                                }
                                entry = tis.nextTarEntry
                            }
                        }
                    }
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun extractTar(tarPath: String, destDir: String): Boolean {
        return try {
            val dest = File(destDir)
            if (!dest.exists()) dest.mkdirs()

            FileInputStream(tarPath).use { fis ->
                BufferedInputStream(fis).use { bis ->
                    TarArchiveInputStream(bis).use { tis ->
                        var entry = tis.nextTarEntry
                        while (entry != null) {
                            val outputFile = File(dest, entry.name)
                            if (entry.isDirectory) {
                                outputFile.mkdirs()
                            } else {
                                outputFile.parentFile?.mkdirs()
                                tis.transferTo(outputFile.outputStream())
                            }
                            entry = tis.nextTarEntry
                        }
                    }
                }
            }
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
                lower.endsWith(".tgz") || lower.endsWith(".tar")
    }
}
