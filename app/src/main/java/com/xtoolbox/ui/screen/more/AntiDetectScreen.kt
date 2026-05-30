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
fun AntiDetectScreen() {
    var piStatus by remember { mutableStateOf("未检测") }
    var tsStatus by remember { mutableStateOf("未检测") }
    var output by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "过检测工具箱",
            style = MiuixTheme.textStyles.headline.large,
            fontWeight = FontWeight.Bold,
            color = MiuixTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "绕过各类检测，配置 Play Integrity 和 TrickyStore",
            style = MiuixTheme.textStyles.body.secondary,
            color = MiuixTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Play Integrity 状态",
                    style = MiuixTheme.textStyles.body.primary,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = piStatus,
                    style = MiuixTheme.textStyles.body.secondary,
                    color = MiuixTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = {
                        scope.launch {
                            isLoading = true
                            val result = ShellExecutor.execAsync(
                                "ls /data/adb/modules/playintegrityfix/"
                            )
                            piStatus = if (result.isSuccess) "已安装" else "未安装"
                            isLoading = false
                        }
                    }) {
                        Text("检测状态")
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                output = "正在安装 PlayIntegrityFix...\n"
                                val result = ShellExecutor.execAsync(
                                    "ls /data/adb/modules/playintegrityfix/"
                                )
                                if (result.isSuccess) {
                                    output += "PlayIntegrityFix 模块已存在\n"
                                } else {
                                    output += "请通过模块管理安装 PlayIntegrityFix 模块\n"
                                }
                                isLoading = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MiuixTheme.colorScheme.primary)
                    ) {
                        Text("安装 PIF")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "TrickyStore 状态",
                    style = MiuixTheme.textStyles.body.primary,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tsStatus,
                    style = MiuixTheme.textStyles.body.secondary,
                    color = MiuixTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = {
                        scope.launch {
                            isLoading = true
                            val result = ShellExecutor.execAsync(
                                "ls /data/adb/modules/tricky_store/"
                            )
                            tsStatus = if (result.isSuccess) "已安装" else "未安装"
                            isLoading = false
                        }
                    }) {
                        Text("检测状态")
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                output = "正在配置 TrickyStore...\n"
                                ShellExecutor.exec("mkdir -p /data/adb/tricky_store")
                                val keybox = ShellExecutor.execAsync("cat /data/adb/tricky_store/keybox.xml 2>/dev/null")
                                val target = ShellExecutor.execAsync("cat /data/adb/tricky_store/target.txt 2>/dev/null")
                                output += "keybox.xml: ${if (keybox.out.isNotEmpty()) "已配置" else "未配置"}\n"
                                output += "target.txt: ${if (target.out.isNotEmpty()) "已配置" else "未配置"}\n"
                                isLoading = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MiuixTheme.colorScheme.primary)
                    ) {
                        Text("检查配置")
                    }
                }
            }
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
