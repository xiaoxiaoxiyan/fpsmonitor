package com.xtoolbox.ui.screen.more

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
fun KeyConfigScreen() {
    var keyboxStatus by remember { mutableStateOf("未检测") }
    var targetStatus by remember { mutableStateOf("未检测") }
    var targetContent by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                isLoading = true
                output = "正在导入 keybox.xml...\n"
                ShellExecutor.exec("mkdir -p /data/adb/tricky_store")
                val result = ShellExecutor.execAsync(
                    "cp '${it.path}' /data/adb/tricky_store/keybox.xml",
                    "chmod 644 /data/adb/tricky_store/keybox.xml"
                )
                output += if (result.isSuccess) "keybox.xml 已导入" else "导入失败"
                keyboxStatus = "已配置"
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "密钥配置",
            style = MiuixTheme.textStyles.headline.large,
            fontWeight = FontWeight.Bold,
            color = MiuixTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "keybox 配置管理，用于 TrickyStore 密钥注入",
            style = MiuixTheme.textStyles.body.secondary,
            color = MiuixTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "keybox.xml",
                    style = MiuixTheme.textStyles.body.primary,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "当前状态: $keyboxStatus",
                    style = MiuixTheme.textStyles.body.secondary,
                    color = MiuixTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val result = ShellExecutor.execAsync(
                                    "cat /data/adb/tricky_store/keybox.xml 2>/dev/null"
                                )
                                keyboxStatus = if (result.out.isNotEmpty()) "已配置" else "未配置"
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("检测")
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Button(
                        onClick = { filePicker.launch("*/*") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MiuixTheme.colorScheme.primary)
                    ) {
                        Text("导入")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "target.txt",
                    style = MiuixTheme.textStyles.body.primary,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "当前状态: $targetStatus",
                    style = MiuixTheme.textStyles.body.secondary,
                    color = MiuixTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetContent,
                    onValueChange = { targetContent = it },
                    label = { Text("target.txt 内容") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val result = ShellExecutor.execAsync(
                                    "cat /data/adb/tricky_store/target.txt 2>/dev/null"
                                )
                                targetStatus = if (result.out.isNotEmpty()) "已配置" else "未配置"
                                targetContent = result.out.joinToString("\n")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("读取")
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                output = "正在写入 target.txt...\n"
                                ShellExecutor.exec("mkdir -p /data/adb/tricky_store")
                                val result = ShellExecutor.execAsync(
                                    "echo '$targetContent' > /data/adb/tricky_store/target.txt",
                                    "chmod 644 /data/adb/tricky_store/target.txt"
                                )
                                output += if (result.isSuccess) "target.txt 已写入" else "写入失败"
                                targetStatus = "已配置"
                                isLoading = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MiuixTheme.colorScheme.primary)
                    ) {
                        Text("写入")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    output = "正在应用配置...\n"
                    ShellExecutor.exec("mkdir -p /data/adb/tricky_store")
                    ShellExecutor.exec("chmod 644 /data/adb/tricky_store/keybox.xml 2>/dev/null")
                    ShellExecutor.exec("chmod 644 /data/adb/tricky_store/target.txt 2>/dev/null")
                    output += "配置权限已修复\n"
                    val keybox = ShellExecutor.execAsync("cat /data/adb/tricky_store/keybox.xml 2>/dev/null")
                    val target = ShellExecutor.execAsync("cat /data/adb/tricky_store/target.txt 2>/dev/null")
                    output += "keybox.xml: ${if (keybox.out.isNotEmpty()) "存在" else "不存在"}\n"
                    output += "target.txt: ${if (target.out.isNotEmpty()) "存在" else "不存在"}\n"
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MiuixTheme.colorScheme.primary)
        ) {
            Text("应用配置")
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
