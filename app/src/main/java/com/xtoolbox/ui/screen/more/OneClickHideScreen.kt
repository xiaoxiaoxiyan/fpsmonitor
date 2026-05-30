package com.xtoolbox.ui.screen.more

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
fun OneClickHideScreen() {
    var output by remember { mutableStateOf("") }
    var isRunning by remember { mutableStateOf(false) }
    var hideStatus by remember { mutableStateOf("未检测") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "一键隐藏",
            style = MiuixTheme.textStyles.headline.large,
            fontWeight = FontWeight.Bold,
            color = MiuixTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "自动安装隐藏模块，实现 Magisk/KernelSU 隐藏",
            style = MiuixTheme.textStyles.body.secondary,
            color = MiuixTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "隐藏模块状态",
                    style = MiuixTheme.textStyles.body.primary,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = hideStatus,
                    style = MiuixTheme.textStyles.body.secondary,
                    color = MiuixTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        scope.launch {
                            val shamiko = ShellExecutor.execAsync("ls /data/adb/modules/shamiko/")
                            val hma = ShellExecutor.execAsync("ls /data/adb/modules/hma/")
                            hideStatus = when {
                                shamiko.isSuccess -> "Shamiko 已安装"
                                hma.isSuccess -> "HMA 已安装"
                                else -> "未安装"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MiuixTheme.colorScheme.primary)
                ) {
                    Text("检测状态")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "一键隐藏",
                    style = MiuixTheme.textStyles.body.primary,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "自动下载并安装 Shamiko 隐藏模块",
                    style = MiuixTheme.textStyles.body.tertiary,
                    color = MiuixTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        isRunning = true
                        output = "开始一键隐藏...\n"
                        scope.launch {
                            val shamiko = ShellExecutor.execAsync("ls /data/adb/modules/shamiko/")
                            if (shamiko.isSuccess) {
                                output += "Shamiko 已安装，无需重复安装\n"
                                hideStatus = "Shamiko 已安装"
                            } else {
                                output += "正在下载 Shamiko 模块...\n"
                                val download = ShellExecutor.execAsync(
                                    "curl -L -o /sdcard/shamiko.zip 'https://github.com/SakionTeam/Shamiko/releases/latest/download/Shamiko.zip' 2>&1"
                                )
                                output += download.out.joinToString("\n") + "\n"
                                if (download.isSuccess) {
                                    output += "正在安装 Shamiko...\n"
                                    val install = ShellExecutor.execAsync(
                                        "magisk --install-module /sdcard/shamiko.zip 2>/dev/null || ksud module install /sdcard/shamiko.zip 2>/dev/null"
                                    )
                                    output += install.out.joinToString("\n") + "\n"
                                    output += if (install.isSuccess) "安装成功，重启后生效" else "安装失败，请手动安装"
                                    val cleanup = ShellExecutor.execAsync("rm -f /sdcard/shamiko.zip")
                                } else {
                                    output += "下载失败，请检查网络连接\n"
                                }
                            }
                            isRunning = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isRunning,
                    colors = ButtonDefaults.buttonColors(containerColor = MiuixTheme.colorScheme.primary)
                ) {
                    Text(if (isRunning) "执行中..." else "一键隐藏")
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
