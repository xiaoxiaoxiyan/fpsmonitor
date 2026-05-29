package com.xtoolbox.core.terminal

import android.os.Handler
import android.os.Looper
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.io.SuFile
import com.topjohnwu.superuser.libsu.ShellUtils
import java.io.InputStream
import java.io.OutputStream

data class TerminalSession(
    val id: String,
    val name: String,
    var isActive: Boolean = true
)

class TerminalEngine(
    private val onOutput: (String) -> Unit,
    private val onExit: () -> Unit
) {
    private var shell: Shell? = null
    private val handler = Handler(Looper.getMainLooper())

    fun startSession(root: Boolean = true) {
        try {
            shell = if (root) {
                Shell.Builder.create()
                    .setFlags(Shell.FLAG_REDIRECT_STDERR)
                    .setTimeout(0)
                    .build()
            } else {
                Shell.Builder.create()
                    .setFlags(Shell.FLAG_REDIRECT_STDERR)
                    .setTimeout(0)
                    .build()
            }

            if (shell?.isRoot == true) {
                onOutput("\u001B[32m# \u001B[0m")
            } else {
                onOutput("\u001B[33m$ \u001B[0m")
            }
        } catch (e: Exception) {
            onOutput("\u001B[31mError: ${e.message}\u001B[0m\n")
        }
    }

    fun executeCommand(command: String) {
        val currentShell = shell ?: return

        onOutput("$command\n")

        val result = currentShell.newJob().add(command).to(ArrayList(), ArrayList()).enqueue()

        Thread {
            try {
                result.get()

                handler.post {
                    val stdout = result.getOut()
                    val stderr = result.getErr()

                    for (line in stdout) {
                        onOutput("$line\n")
                    }
                    for (line in stderr) {
                        onOutput("\u001B[31m$line\u001B[0m\n")
                    }

                    if (currentShell.isRoot) {
                        onOutput("\u001B[32m# \u001B[0m")
                    } else {
                        onOutput("\u001B[33m$ \u001B[0m")
                    }
                }
            } catch (e: Exception) {
                handler.post {
                    onOutput("\u001B[31mError: ${e.message}\u001B[0m\n")
                    if (currentShell.isRoot) {
                        onOutput("\u001B[32m# \u001B[0m")
                    } else {
                        onOutput("\u001B[33m$ \u001B[0m")
                    }
                }
            }
        }.start()
    }

    fun closeSession() {
        shell?.close()
        shell = null
        onExit()
    }
}
