package com.xtoolbox.ui.screen.more

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

data class PartitionInfo(
    val name: String,
    val path: String
)

@Composable
fun PartitionScreen() {
    var partitions by remember { mutableStateOf(listOf<PartitionInfo>()) }
    var output by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var flashTarget by remember { mutableStateOf<PartitionInfo?>(null) }
    var showFlashDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val target = flashTarget ?: return@let
            scope.launch {
                output = "正在刷写 ${target.name}...\n"
                val result = ShellExecutor.execAsync(
                    "dd if='${it.path}' of='${target.path}'",
                    "echo '刷写完成'"
                )
                output += result.out.joinToString("\n") +
                        if (result.err.isNotEmpty()) "\n错误:\n${result.err.joinToString("\n")}" else ""
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "分区刷写",
            style = MiuixTheme.textStyles.headline.large,
            fontWeight = FontWeight.Bold,
            color = MiuixTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "备份和刷入分区镜像，操作有风险请谨慎",
            style = MiuixTheme.textStyles.body.secondary,
            color = MiuixTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isLoading = true
                scope.launch {
                    val result = ShellExecutor.execAsync("ls -la /dev/block/by-name/")
                    partitions = result.out.mapNotNull { line ->
                        val parts = line.trim().split(Regex("\\s+"))
                        if (parts.size >= 4 && parts[0].startsWith("l")) {
                            PartitionInfo(parts.last(), parts[parts.size - 2])
                        } else null
                    }
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = MiuixTheme.colorScheme.primary)
        ) {
            Text(if (isLoading) "加载中..." else "读取分区列表")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (partitions.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(partitions, key = { it.name }) { partition ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = partition.name,
                                style = MiuixTheme.textStyles.body.primary,
                                fontWeight = FontWeight.Medium,
                                color = MiuixTheme.colorScheme.onSurface
                            )
                            Text(
                                text = partition.path,
                                style = MiuixTheme.textStyles.body.tertiary,
                                fontFamily = FontFamily.Monospace,
                                color = MiuixTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            output = "正在备份 ${partition.name}...\n"
                                            ShellExecutor.exec("mkdir -p /sdcard/backup")
                                            val result = ShellExecutor.execAsync(
                                                "dd if='${partition.path}' of='/sdcard/backup/${partition.name}.img'"
                                            )
                                            output += result.out.joinToString("\n") +
                                                    if (result.err.isNotEmpty()) "\n${result.err.joinToString("\n")}" else ""
                                            output += "\n备份已保存到 /sdcard/backup/${partition.name}.img"
                                        }
                                    }
                                ) {
                                    Text("备份")
                                }
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                Button(
                                    onClick = {
                                        flashTarget = partition
                                        showFlashDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MiuixTheme.colorScheme.error
                                    )
                                ) {
                                    Text("刷写")
                                }
                            }
                        }
                    }
                }
            }
        }

        if (output.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
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

    if (showFlashDialog && flashTarget != null) {
        AlertDialog(
            onDismissRequest = { showFlashDialog = false },
            title = { Text("确认刷写") },
            text = { Text("即将刷写分区 ${flashTarget!!.name}，此操作有风险，确定继续？") },
            confirmButton = {
                TextButton(onClick = {
                    showFlashDialog = false
                    filePicker.launch("application/octet-stream")
                }) {
                    Text("继续")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFlashDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}
