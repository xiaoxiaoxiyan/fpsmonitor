package com.xtoolbox.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.topjohnwu.superuser.Shell
import com.xtoolbox.core.root.RootChecker
import com.xtoolbox.core.root.RootMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDeviceInfo()
    }

    fun loadDeviceInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true)

            Shell.getShell()

            val isRooted = RootChecker.isRootAvailable()
            val rootMethod = RootChecker.getRootMethod()
            val workMode = if (isRooted) RootChecker.getWorkMode() else "未知"
            val kernelVersion = if (isRooted) RootChecker.getKernelVersion() else "未知"
            val securityPatch = if (isRooted) RootChecker.getSecurityPatch() else "未知"
            val cpuInfo = if (isRooted) RootChecker.getCpuInfo() else "未知"
            val ramSize = if (isRooted) RootChecker.getRamSize() else "未知"

            _uiState.value = HomeUiState(
                isRooted = isRooted,
                rootMethod = rootMethod.displayName,
                workMode = workMode,
                kernelVersion = kernelVersion,
                androidVersion = RootChecker.getAndroidVersion(),
                apiLevel = RootChecker.getApiLevel(),
                securityPatch = securityPatch.ifBlank { "未知" },
                deviceModel = RootChecker.getDeviceModel(),
                cpuInfo = cpuInfo,
                ramSize = ramSize,
                isLoading = false
            )
        }
    }

    fun reboot() {
        com.xtoolbox.core.root.ShellExecutor.reboot()
    }

    fun softReboot() {
        com.xtoolbox.core.root.ShellExecutor.softReboot()
    }

    fun rebootRecovery() {
        com.xtoolbox.core.root.ShellExecutor.rebootRecovery()
    }

    fun rebootBootloader() {
        com.xtoolbox.core.root.ShellExecutor.rebootBootloader()
    }
}
