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
fun Ak3FlashScreen() {
    var selectedZip by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var isFlashing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedZip = it.path ?: it.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "AK3 刷写",
            style = MiuixTheme.textStyles.headline.large,
            fontWeight = FontWeight.Bold,
            color = MiuixTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "AnyKernel3 内核刷入工具，选择内核 ZIP 包后一键刷入",
            style = MiuixTheme.textStyles.body.secondary,
            color = MiuixTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "选择内核包",
                    style = MiuixTheme.textStyles.body.primary,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { filePicker.launch("application/zip") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isFlashing
                ) {
                    Text(if (selectedZip.isNotEmpty()) "已选择: ${selectedZip.substringAfterLast("/")}" else "选择 ZIP 文件")
                }

                if (selectedZip.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = selectedZip,
                        style = MiuixTheme.textStyles.body.tertiary,
                        color = MiuixTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (selectedZip.isNotEmpty()) {
                    isFlashing = true
                    output = "开始刷入...\n"
                    scope.launch {
                        val commands = arrayOf(
                            "mkdir -p /data/local/tmp/ak3",
                            "unzip -o '$selectedZip' -d /data/local/tmp/ak3",
                            "sh /data/local/tmp/ak3/flash.sh",
                            "rm -rf /data/local/tmp/ak3"
                        )
                        val result = ShellExecutor.execAsync(*commands)
                        output = result.out.joinToString("\n") +
                                if (result.err.isNotEmpty()) "\n错误:\n${result.err.joinToString("\n")}" else ""
                        isFlashing = false
                    }
                } else {
                    output = "请先选择内核 ZIP 文件"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isFlashing && selectedZip.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MiuixTheme.colorScheme.primary
            )
        ) {
            Text(if (isFlashing) "刷入中..." else "刷入内核")
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
