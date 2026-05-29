package com.xtoolbox.ui.screen.home

import androidx.compose.ui.graphics.Color

data class HomeUiState(
    val isRooted: Boolean = false,
    val rootMethod: String = "无",
    val workMode: String = "未知",
    val kernelVersion: String = "未知",
    val androidVersion: String = "未知",
    val apiLevel: Int = 0,
    val securityPatch: String = "未知",
    val deviceModel: String = "未知",
    val cpuInfo: String = "未知",
    val ramSize: String = "未知",
    val isLoading: Boolean = true
)
