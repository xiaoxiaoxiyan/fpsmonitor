package com.xtoolbox.ui.screen.script

import com.xtoolbox.core.script.ScriptInfo

data class ScriptUiState(
    val scripts: List<ScriptInfo> = emptyList(),
    val isLoading: Boolean = true,
    val isDownloading: Boolean = false,
    val executingScript: String? = null,
    val executionOutput: List<String> = emptyList(),
    val error: String? = null
)
