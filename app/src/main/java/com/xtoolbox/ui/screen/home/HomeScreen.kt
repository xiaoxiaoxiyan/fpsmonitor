package com.xtoolbox.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "XToolbox",
            style = MiuixTheme.textStyles.headline.large,
            fontWeight = FontWeight.Bold,
            color = MiuixTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "v1.0.0",
            style = MiuixTheme.textStyles.body.secondary,
            color = MiuixTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            StatusCard(uiState)

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isRooted) {
                AuthCard(uiState)

                Spacer(modifier = Modifier.height(16.dp))
            }

            QuickActionsCard(
                isRooted = uiState.isRooted,
                onReboot = { viewModel.reboot() },
                onSoftReboot = { viewModel.softReboot() },
                onRebootRecovery = { viewModel.rebootRecovery() },
                onRebootBootloader = { viewModel.rebootBootloader() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DeviceInfoCard(uiState)
        }
    }
}

@Composable
private fun StatusCard(state: HomeUiState) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (state.isRooted) Icons.Filled.CheckCircle else Icons.Filled.Close,
                    contentDescription = null,
                    tint = if (state.isRooted)
                        MiuixTheme.colorScheme.primary
                    else
                        MiuixTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (state.isRooted) "已 Root" else "未 Root",
                    style = MiuixTheme.textStyles.title.large,
                    fontWeight = FontWeight.Bold,
                    color = if (state.isRooted)
                        MiuixTheme.colorScheme.primary
                    else
                        MiuixTheme.colorScheme.error
                )
            }

            if (state.isRooted) {
                Spacer(modifier = Modifier.height(16.dp))
                InfoRow("Root 方式", state.rootMethod)
                if (state.rootMethodVersion.isNotBlank()) {
                    InfoRow("版本", state.rootMethodVersion)
                }
                InfoRow("工作模式", state.workMode)
                InfoRow("内核版本", state.kernelVersion)
                InfoRow("Android", "${state.androidVersion} (API ${state.apiLevel})")
                InfoRow("安全补丁", state.securityPatch)
                if (state.susfsStatus != "不可用") {
                    InfoRow("SuSFS", state.susfsStatus)
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "未检测到 Root 权限，部分功能不可用",
                    style = MiuixTheme.textStyles.body.secondary,
                    color = MiuixTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AuthCard(state: HomeUiState) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "授权信息",
                style = MiuixTheme.textStyles.title.medium,
                fontWeight = FontWeight.SemiBold,
                color = MiuixTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoRow("授权方式", state.rootMethod)
            InfoRow("工作状态", "正常")
            InfoRow("SuperUser 数量", "${state.superUserCount}")
            if (state.suCompatStatus != "不可用") {
                InfoRow("suCompat", state.suCompatStatus)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MiuixTheme.textStyles.body.secondary,
            color = MiuixTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MiuixTheme.textStyles.body.secondary,
            fontWeight = FontWeight.Medium,
            color = MiuixTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun QuickActionsCard(
    isRooted: Boolean,
    onReboot: () -> Unit,
    onSoftReboot: () -> Unit,
    onRebootRecovery: () -> Unit,
    onRebootBootloader: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "快捷操作",
                style = MiuixTheme.textStyles.title.medium,
                fontWeight = FontWeight.SemiBold,
                color = MiuixTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onReboot,
                    enabled = isRooted,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MiuixTheme.colorScheme.error
                    )
                ) {
                    Text("重启")
                }
                OutlinedButton(
                    onClick = onSoftReboot,
                    enabled = isRooted,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("软重启")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRebootRecovery,
                    enabled = isRooted,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Recovery")
                }
                OutlinedButton(
                    onClick = onRebootBootloader,
                    enabled = isRooted,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Bootloader")
                }
            }
        }
    }
}

@Composable
private fun DeviceInfoCard(state: HomeUiState) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "设备信息",
                style = MiuixTheme.textStyles.title.medium,
                fontWeight = FontWeight.SemiBold,
                color = MiuixTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoRow("设备", state.deviceModel)
            InfoRow("CPU", state.cpuInfo)
            InfoRow("RAM", state.ramSize)
        }
    }
}
