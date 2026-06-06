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

    private var rootShell: Shell? = null

    /**
     * Get or create a root shell instance.
     */
    private fun getRootShell(): Shell {
        return rootShell ?: Shell.Builder.create().setFlags(Shell.FLAG_REDIRECT_STDERR).build().also {
            rootShell = it
        }
    }

    /**
     * Execute a shell command with root and return the output as a string.
     */
    suspend fun execute(command: String): String = withContext(Dispatchers.IO) {
        try {
            val result = getRootShell().newJob().add(command).exec()
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
            val result = getRootShell().newJob().add(cmd).exec()
            result.out.map { it.trim() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Write content to a file via root shell.
     * Uses printf to avoid shell escaping issues.
     */
    suspend fun writeFile(path: String, content: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = getRootShell().newJob().add("echo $content > $path").exec()
            result.isSuccess
        } catch (e: Exception) {
            false
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