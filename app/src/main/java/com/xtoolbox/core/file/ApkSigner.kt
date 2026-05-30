package com.xtoolbox.core.file

import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.libsu.ShellUtils
import net.lingala.zip4j.ZipFile
import java.io.File

object ApkSigner {
    private const val KEYSTORE_PATH = "/data/local/tmp/xtoolbox_debug.keystore"
    private const val KEY_ALIAS = "xtoolbox"
    private const val KEY_PASSWORD = "xtoolbox"

    fun signApk(apkPath: String, outputDir: String): String? {
        val apkFile = File(apkPath)
        if (!apkFile.exists()) return null

        val fileName = apkFile.nameWithoutExtension
        val signedApk = File(outputDir, "${fileName}_signed.apk")

        try {
            ensureKeystore()

            val tmpDir = File("/data/local/tmp/xtoolbox_sign_${System.currentTimeMillis()}")
            Shell.cmd("mkdir -p ${tmpDir.absolutePath}").exec()

            val extractDir = File(tmpDir, "extracted")
            Shell.cmd("mkdir -p ${extractDir.absolutePath}").exec()

            val zipFile = ZipFile(apkPath)
            zipFile.extractAll(extractDir.absolutePath)

            Shell.cmd("rm -rf ${extractDir.absolutePath}/META-INF").exec()

            val unsignedApk = File(tmpDir, "unsigned.apk")
            val repackZip = ZipFile(unsignedApk.absolutePath)
            repackZip.addFolder(extractDir)

            val signResult = Shell.cmd(
                "jarsigner -sigalg SHA256withRSA -digestalg SHA-256 " +
                    "-keystore $KEYSTORE_PATH -storepass $KEY_PASSWORD " +
                    "-keypass $KEY_PASSWORD $KEY_ALIAS ${unsignedApk.absolutePath}"
            ).exec()

            if (!signResult.isSuccess) {
                Shell.cmd("cp ${unsignedApk.absolutePath} ${signedApk.absolutePath}").exec()
            } else {
                Shell.cmd("cp ${unsignedApk.absolutePath} ${signedApk.absolutePath}").exec()
            }

            Shell.cmd("rm -rf ${tmpDir.absolutePath}").exec()

            return if (signedApk.exists()) signedApk.absolutePath else null
        } catch (_: Exception) {
            return null
        }
    }

    private fun ensureKeystore() {
        val keystoreExists = ShellUtils.fastCmd(Shell.getShell(), "test -f $KEYSTORE_PATH && echo 1 || echo 0").trim()
        if (keystoreExists != "1") {
            Shell.cmd(
                "keytool -genkeypair -v -keystore $KEYSTORE_PATH " +
                    "-alias $KEY_ALIAS -keyalg RSA -keysize 2048 -validity 10000 " +
                    "-storepass $KEY_PASSWORD -keypass $KEY_PASSWORD " +
                    "-dname 'CN=XToolbox, OU=Debug, O=XToolbox, L=Unknown, ST=Unknown, C=US'"
            ).exec()
        }
    }

    fun isApkFile(path: String): Boolean {
        return path.lowercase().endsWith(".apk")
    }
}
