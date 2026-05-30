package com.xtoolbox.ui.screen.more

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.xtoolbox.core.root.ShellExecutor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulerScreen() {
    var cpuInfo by remember { mutableStateOf("未读取") }
    var governor by remember { mutableStateOf("") }
    var governorExpanded by remember { mutableStateOf(false) }
    var minFreq by remember { mutableFloatStateOf(0f) }
    var maxFreq by remember { mutableFloatStateOf(0f) }
    var freqRange by remember { mutableStateOf(0f..0f) }
    var thermalEnabled by remember { mutableStateOf(true) }
    var touchBoostEnabled by remember { mutableStateOf(false) }
    var output by remember { mutableStateOf("") }
    val cpuUsageHistory = remember { mutableStateListOf<Float>() }
    val scope = rememberCoroutineScope()

    val governors = listOf("performance", "powersave", "ondemand", "interactive", "conservative", "schedutil")

    scope.launch {
        while (true) {
            val result = ShellExecutor.execAsync(
                "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq 2>/dev/null"
            )
            val freq = result.out.firstOrNull()?.trim()?.toLongOrNull() ?: 0L
            val maxF = ShellExecutor.execFast("cat /sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq 2>/dev/null").trim().toLongOrNull() ?: 1L
            val usage = if (maxF > 0) freq.toFloat() / maxF.toFloat() else 0f
            if (cpuUsageHistory.size > 60) cpuUsageHistory.removeAt(0)
            cpuUsageHistory.add(usage.coerceIn(0f, 1f))
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "调度中心",
            style = MiuixTheme.textStyles.headline.large,
            fontWeight = FontWeight.Bold,
            color = MiuixTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "CPU 性能与温控管理",
            style = MiuixTheme.textStyles.body.secondary,
            color = MiuixTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CPU 信息",
                    style = MiuixTheme.textStyles.body.primary,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = cpuInfo,
                    style = MiuixTheme.textStyles.body.secondary,
                    fontFamily = FontFamily.Monospace,
                    color = MiuixTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        scope.launch {
                            val curFreq = ShellExecutor.execFast("cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq 2>/dev/null").trim()
                            val maxFreqVal = ShellExecutor.execFast("cat /sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq 2>/dev/null").trim()
                            val minFreqVal = ShellExecutor.execFast("cat /sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq 2>/dev/null").trim()
                            val gov = ShellExecutor.execFast("cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor 2>/dev/null").trim()
                            val temp = ShellExecutor.execFast("cat /sys/class/thermal/thermal_zone0/temp 2>/dev/null").trim()
                            governor = gov
                            cpuInfo = "当前频率: ${curFreq} kHz\n" +
                                    "最大频率: ${maxFreqVal} kHz\n" +
                                    "最小频率: ${minFreqVal} kHz\n" +
                                    "调度器: $gov\n" +
                                    "温度: ${temp}°C"
                            val min = minFreqVal.toFloatOrNull() ?: 0f
                            val max = maxFreqVal.toFloatOrNull() ?: 0f
                            freqRange = min..max
                            minFreq = min
                            maxFreq = max
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MiuixTheme.colorScheme.primary)
                ) {
                    Text("读取 CPU 信息")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "调度器设置",
                    style = MiuixTheme.textStyles.body.primary,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = governorExpanded,
                    onExpandedChange = { governorExpanded = !governorExpanded }
                ) {
                    OutlinedTextField(
                        value = governor,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("调度器") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = governorExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = governorExpanded,
                        onDismissRequest = { governorExpanded = false }
                    ) {
                        governors.forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g) },
                                onClick = {
                                    governor = g
                                    governorExpanded = false
                                    scope.launch {
                                        ShellExecutor.execAsync(
                                            "echo '$g' > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor"
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "最小频率: ${minFreq.toLong()} kHz",
                    style = MiuixTheme.textStyles.body.secondary,
                    color = MiuixTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = minFreq,
                    onValueChange = {
                        minFreq = it
                        scope.launch {
                            ShellExecutor.execAsync(
                                "echo '${it.toLong()}' > /sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq"
                            )
                        }
                    },
                    valueRange = freqRange,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "最大频率: ${maxFreq.toLong()} kHz",
                    style = MiuixTheme.textStyles.body.secondary,
                    color = MiuixTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = maxFreq,
                    onValueChange = {
                        maxFreq = it
                        scope.launch {
                            ShellExecutor.execAsync(
                                "echo '${it.toLong()}' > /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq"
                            )
                        }
                    },
                    valueRange = freqRange,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "温控与优化",
                    style = MiuixTheme.textStyles.body.primary,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "温控开关",
                            style = MiuixTheme.textStyles.body.primary,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (thermalEnabled) "已启用" else "已关闭",
                            style = MiuixTheme.textStyles.body.tertiary,
                            color = MiuixTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = thermalEnabled,
                        onCheckedChange = {
                            thermalEnabled = it
                            scope.launch {
                                if (it) {
                                    ShellExecutor.execAsync("echo 1 > /sys/module/msm_thermal/parameters/enabled 2>/dev/null")
                                } else {
                                    ShellExecutor.execAsync("echo 0 > /sys/module/msm_thermal/parameters/enabled 2>/dev/null")
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "触控采样率提升",
                            style = MiuixTheme.textStyles.body.primary,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (touchBoostEnabled) "已启用" else "已关闭",
                            style = MiuixTheme.textStyles.body.tertiary,
                            color = MiuixTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = touchBoostEnabled,
                        onCheckedChange = {
                            touchBoostEnabled = it
                            scope.launch {
                                if (it) {
                                    ShellExecutor.execAsync("echo 1 > /sys/devices/virtual/sec/tsp/cmd && echo 'boost_enable,1' > /sys/devices/virtual/sec/tsp/cmd 2>/dev/null")
                                } else {
                                    ShellExecutor.execAsync("echo 'boost_enable,0' > /sys/devices/virtual/sec/tsp/cmd 2>/dev/null")
                                }
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CPU 使用率",
                    style = MiuixTheme.textStyles.body.primary,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (cpuUsageHistory.isNotEmpty()) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val padding = 30f

                        drawLine(
                            color = MiuixTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            start = Offset(padding, canvasHeight - padding),
                            end = Offset(canvasWidth, canvasHeight - padding),
                            strokeWidth = 1f
                        )
                        drawLine(
                            color = MiuixTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            start = Offset(padding, padding),
                            end = Offset(padding, canvasHeight - padding),
                            strokeWidth = 1f
                        )

                        for (i in 0..4) {
                            val y = padding + (canvasHeight - 2 * padding) * i / 4f
                            drawLine(
                                color = MiuixTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                                start = Offset(padding, y),
                                end = Offset(canvasWidth, y),
                                strokeWidth = 1f
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                "${100 - i * 25}%",
                                0f,
                                y + 4f,
                                android.graphics.Paint().apply {
                                    textSize = 20f
                                    color = android.graphics.Color.GRAY
                                }
                            )
                        }

                        if (cpuUsageHistory.size > 1) {
                            val path = Path()
                            val stepX = (canvasWidth - padding) / 60f
                            cpuUsageHistory.forEachIndexed { index, value ->
                                val x = padding + index * stepX
                                val y = canvasHeight - padding - value * (canvasHeight - 2 * padding)
                                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            drawPath(
                                path = path,
                                color = MiuixTheme.colorScheme.primary,
                                style = Stroke(width = 2f)
                            )
                        }
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
