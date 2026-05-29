package com.xtoolbox.core.script

import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

object ScriptRunner {
    fun executeScript(scriptPath: String): Shell.Result {
        return Shell.cmd("sh \"$scriptPath\"").exec()
    }

    suspend fun executeScriptAsync(scriptPath: String): Shell.Result = withContext(Dispatchers.IO) {
        Shell.cmd("sh \"$scriptPath\"").exec()
    }

    fun executeScriptWithOutput(scriptPath: String): Pair<Int, List<String>> {
        val result = Shell.cmd("sh \"$scriptPath\"").exec()
        return Pair(result.code, result.out)
    }
}
