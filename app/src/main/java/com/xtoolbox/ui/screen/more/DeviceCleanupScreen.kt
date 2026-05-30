package com.xtoolbox.ui.screen.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.xtoolbox.core.root.ShellExecutor

@Composable
fun DeviceCleanupScreen() {
    var ssaid by remember { mutableStateOf("未知") }
    var deviceId by remember { mutableStateOf("未知") }
    var androidId by remember { mutableStateOf("未知") }
    var output by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "设备清理",
            style = MiuixTheme.textStyles.headline.large,
            fontWeight = FontWeight.Bold,
            color = MiuixTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "修改设备标识，清理 ID 信息",
            style = MiuixTheme.textStyles.body.secondary,
            color = MiuixTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SSAID",
                    style = MiuixTheme.textStyles.body.primary,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurface
                )
                Text(
                    text = ssaid,
                    style = MiuixTheme.textStyles.body.secondary,
                    fontFamily = FontFamily.Monospace,
                    color = MiuixTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = {
                        scope.launch {
                            val result = ShellExecutor.execAsync("settings get secure android_id")
                            ssaid = result.out.firstOrNull()?.trim() ?: "未知"
                        }
                    }) {
                        Text("读取")
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                output = "正在随机化 SSAID...\n"
                                val result = ShellExecutor.execAsync(
                                    "settings put secure android_id $(openssl rand -hex 8)"
                                )
                                output += if (result.isSuccess) "SSAID 已更新" else "更新失败"
                                val read = ShellExecutor.execAsync("settings get secure android_id")
                                ssaid = read.out.firstOrNull()?.trim() ?: "未知"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MiuixTheme.colorScheme.primary)
                    ) {
                        Text("随机化")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Device ID",
                    style = MiuixTheme.textStyles.body.primary,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurface
                )
                Text(
                    text = deviceId,
                    style = MiuixTheme.textStyles.body.secondary,
                    fontFamily = FontFamily.Monospace,
                    color = MiuixTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = {
                        scope.launch {
                            val result = ShellExecutor.execAsync(
                                "service call iphonesubinfo 1 s16 com.android.shell 2>/dev/null || echo unknown"
                            )
                            deviceId = result.out.firstOrNull()?.trim() ?: "未知"
                        }
                    }) {
                        Text("读取")
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                output = "正在随机化 Device ID...\n"
                                val result = ShellExecutor.execAsync(
                                    "settings put global device_id $(openssl rand -hex 16)"
                                )
                                output += if (result.isSuccess) "Device ID 已更新" else "更新失败"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MiuixTheme.colorScheme.primary)
                    ) {
                        Text("随机化")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Android ID",
                    style = MiuixTheme.textStyles.body.primary,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurface
                )
                Text(
                    text = androidId,
                    style = MiuixTheme.textStyles.body.secondary,
                    fontFamily = FontFamily.Monospace,
                    color = MiuixTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = {
                        scope.launch {
                            val result = ShellExecutor.execAsync("settings get secure android_id")
                            androidId = result.out.firstOrNull()?.trim() ?: "未知"
                        }
                    }) {
                        Text("读取")
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                output = "正在随机化 Android ID...\n"
                                val result = ShellExecutor.execAsync(
                                    "content insert --uri content://settings/secure --bind name:s:android_id --bind value:s:$(openssl rand -hex 8)"
                                )
                                output += if (result.isSuccess) "Android ID 已更新" else "更新失败"
                                val read = ShellExecutor.execAsync("settings get secure android_id")
                                androidId = read.out.firstOrNull()?.trim() ?: "未知"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MiuixTheme.colorScheme.primary)
                    ) {
                        Text("随机化")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    output = "正在全部重置...\n"
                    ShellExecutor.execAsync(
                        "settings put secure android_id $(openssl rand -hex 8)",
                        "settings put global device_id $(openssl rand -hex 16)"
                    )
                    output += "所有 ID 已重置\n"
                    val readSsaid = ShellExecutor.execAsync("settings get secure android_id")
                    ssaid = readSsaid.out.firstOrNull()?.trim() ?: "未知"
                    androidId = ssaid
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MiuixTheme.colorScheme.error)
        ) {
            Text("全部重置")
        }

        if (output.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = output,
                    modifier = Modifier.padding(16.dp),
                    style = MiuixTheme.textStyles.body.secondary,
                    fontFamily = FontFamily.Monospace,
                    color = MiuixTheme.colorScheme.onSurface
                )
            }
        }
    }
}
