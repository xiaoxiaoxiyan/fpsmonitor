package com.xtoolbox.ui.screen.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xtoolbox.core.module.ModuleInfo
import com.xtoolbox.core.module.ModuleScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ModuleUiState(
    val modules: List<ModuleInfo> = emptyList(),
    val isLoading: Boolean = true
)

class ModuleViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ModuleUiState())
    val uiState: StateFlow<ModuleUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val modules = ModuleScanner.scanModules()
            _uiState.value = ModuleUiState(modules = modules, isLoading = false)
        }
    }

    fun toggleModule(module: ModuleInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            if (module.isEnabled) {
                ModuleScanner.disableModule(module.path)
            } else {
                ModuleScanner.enableModule(module.path)
            }
            refresh()
        }
    }

    fun uninstallModule(module: ModuleInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            ModuleScanner.uninstallModule(module.path)
            refresh()
        }
    }
}
