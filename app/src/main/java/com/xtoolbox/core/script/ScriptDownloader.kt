package com.xtoolbox.core.script

import android.content.Context
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.libsu.ShellUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.URLDecoder

object ScriptDownloader {
    private const val SCRIPT_BASE_URL = "http://sgheejejee54545181851616646461515166hxhdhehejjdfhh.qehap.asia/脚本/"
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private fun getScriptDir(context: Context): File {
        val dir = File(context.filesDir, "scripts")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    suspend fun fetchScriptList(context: Context): List<ScriptInfo> = withContext(Dispatchers.IO) {
        val scriptDir = getScriptDir(context)
        val localFiles = scriptDir.listFiles()?.associateBy { it.name } ?: emptyMap()

        try {
            val request = Request.Builder().url(SCRIPT_BASE_URL).build()
            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: return@withContext emptyList()

            val hrefRegex = Regex("""href="([^"]+)"""")
            val matches = hrefRegex.findAll(html)

            val scripts = mutableListOf<ScriptInfo>()
            for (match in matches) {
                val encodedName = match.groupValues[1]
                if (encodedName.endsWith("/") || encodedName == "../" || encodedName == ".." || encodedName == "?") continue

                val decodedName = try {
                    URLDecoder.decode(encodedName, "UTF-8")
                } catch (_: Exception) {
                    encodedName
                }

                if (decodedName.contains("..") || decodedName.contains("/") || decodedName.contains("\\")) continue

                val localFile = localFiles[decodedName]
                val isDownloaded = localFile != null && localFile.exists() && localFile.length() > 0

                scripts.add(
                    ScriptInfo(
                        name = decodedName,
                        url = SCRIPT_BASE_URL + encodedName,
                        localPath = localFile?.absolutePath,
                        size = localFile?.length() ?: 0,
                        lastModified = if (localFile != null) {
                            java.text.SimpleDateFormat("MM-dd", java.util.Locale.getDefault())
                                .format(java.util.Date(localFile.lastModified()))
                        } else "",
                        isDownloaded = isDownloaded
                    )
                )
            }
            scripts
        } catch (_: Exception) {
            localFiles.map { file ->
                ScriptInfo(
                    name = file.name,
                    url = SCRIPT_BASE_URL + file.name,
                    localPath = file.absolutePath,
                    size = file.length(),
                    lastModified = java.text.SimpleDateFormat("MM-dd", java.util.Locale.getDefault())
                        .format(java.util.Date(file.lastModified())),
                    isDownloaded = true
                )
            }
        }
    }

    suspend fun downloadScript(context: Context, script: ScriptInfo): ScriptInfo = withContext(Dispatchers.IO) {
        val scriptDir = getScriptDir(context)
        val targetFile = File(scriptDir, script.name)

        try {
            val request = Request.Builder().url(script.url).build()
            val response = client.newCall(request).execute()
            val body = response.body ?: return@withContext script

            targetFile.outputStream().use { output ->
                body.byteStream().use { input ->
                    input.copyTo(output)
                }
            }

            Shell.cmd("chmod 755 ${targetFile.absolutePath}").exec()

            script.copy(
                localPath = targetFile.absolutePath,
                size = targetFile.length(),
                lastModified = java.text.SimpleDateFormat("MM-dd", java.util.Locale.getDefault())
                    .format(java.util.Date(targetFile.lastModified())),
                isDownloaded = true
            )
        } catch (_: Exception) {
            script
        }
    }

    suspend fun downloadAllScripts(context: Context, scripts: List<ScriptInfo>): List<ScriptInfo> = withContext(Dispatchers.IO) {
        scripts.map { script ->
            downloadScript(context, script)
        }
    }
}
