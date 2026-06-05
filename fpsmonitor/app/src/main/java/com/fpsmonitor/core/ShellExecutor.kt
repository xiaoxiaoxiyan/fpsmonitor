package com.fpsmonitor.core

import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ShellExecutor - Execute root shell commands via libsu.
 * Provides a clean API for reading system files and executing commands.
 * Reference: Scene's daemon mode shell execution, Takt's Choreographer approach.
 */
object ShellExecutor {

    /**
     * Execute a shell command with root and return the output as a string.
     */
    suspend fun execute(command: String): String = withContext(Dispatchers.IO) {
        try {
            val result = Shell.cmd(command).exec()
            result.out.joinToString("\n").trim()
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Read a file via root shell (cat).
     */
    suspend fun readFile(path: String): String {
        return execute("cat $path 2>/dev/null")
    }

    /**
     * Execute multiple commands and return results.
     */
    suspend fun executeAll(commands: List<String>): List<String> = withContext(Dispatchers.IO) {
        try {
            val cmd = commands.joinToString(" && ")
            val result = Shell.cmd(cmd).exec()
            result.out.map { it.trim() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Check if a file or directory exists (via root).
     */
    suspend fun exists(path: String): Boolean {
        val result = execute("test -e $path && echo YES || echo NO")
        return result.contains("YES")
    }
}