package com.xtoolbox.ui.screen.script

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xtoolbox.core.script.ScriptDownloader
import com.xtoolbox.core.script.ScriptInfo
import com.xtoolbox.core.script.ScriptRunner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScriptViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(ScriptUiState())
    val uiState: StateFlow<ScriptUiState> = _uiState.asStateFlow()

    init {
        loadScripts()
    }

    fun loadScripts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val scripts = ScriptDownloader.fetchScriptList(getApplication())
            _uiState.value = _uiState.value.copy(
                scripts = scripts,
                isLoading = false
            )
        }
    }

    fun downloadScript(script: ScriptInfo) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDownloading = true)
            val updated = ScriptDownloader.downloadScript(getApplication(), script)
            val currentScripts = _uiState.value.scripts.toMutableList()
            val index = currentScripts.indexOfFirst { it.name == script.name }
            if (index >= 0) {
                currentScripts[index] = updated
            }
            _uiState.value = _uiState.value.copy(
                scripts = currentScripts,
                isDownloading = false
            )
        }
    }

    fun downloadAllScripts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDownloading = true)
            val updated = ScriptDownloader.downloadAllScripts(
                getApplication(),
                _uiState.value.scripts
            )
            _uiState.value = _uiState.value.copy(
                scripts = updated,
                isDownloading = false
            )
        }
    }

    fun executeScript(script: ScriptInfo) {
        if (script.localPath == null || !script.isDownloaded) {
            downloadAndExecute(script)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                executingScript = script.name,
                executionOutput = emptyList()
            )

            val (code, output) = ScriptRunner.executeScriptWithOutput(script.localPath)
            _uiState.value = _uiState.value.copy(
                executingScript = null,
                executionOutput = output
            )
        }
    }

    private fun downloadAndExecute(script: ScriptInfo) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDownloading = true)
            val updated = ScriptDownloader.downloadScript(getApplication(), script)

            val currentScripts = _uiState.value.scripts.toMutableList()
            val index = currentScripts.indexOfFirst { it.name == script.name }
            if (index >= 0) {
                currentScripts[index] = updated
            }

            _uiState.value = _uiState.value.copy(
                scripts = currentScripts,
                isDownloading = false
            )

            if (updated.isDownloaded && updated.localPath != null) {
                executeScript(updated)
            }
        }
    }

    fun clearOutput() {
        _uiState.value = _uiState.value.copy(executionOutput = emptyList())
    }
}
