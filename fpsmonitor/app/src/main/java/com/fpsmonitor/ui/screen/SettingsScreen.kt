package com.fpsmonitor.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fpsmonitor.core.SettingsManager

@Composable
fun SettingsScreen() {
    var sampleInterval by remember { mutableStateOf(SettingsManager.sampleInterval.toString()) }
    var targetFps by remember { mutableStateOf(SettingsManager.targetFps.toString()) }
    var jankThreshold by remember { mutableStateOf(SettingsManager.jankThreshold.toString()) }
    var showOverlayOnStart by remember { mutableStateOf(SettingsManager.showOverlayOnStart) }
    var collapsedBgColor by remember { mutableStateOf(SettingsManager.collapsedBgColor) }
    var fpsTextColor by remember { mutableStateOf(SettingsManager.fpsTextColor) }
    var chartLineColor by remember { mutableStateOf(SettingsManager.chartLineColor) }
    var panelBgColor by remember { mutableStateOf(SettingsManager.panelBgColor) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "设置",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sample interval
        SettingsCard("采样间隔 (ms)") {
            OutlinedTextField(
                value = sampleInterval,
                onValueChange = {
                    sampleInterval = it
                    it.toLongOrNull()?.let { v -> SettingsManager.sampleInterval = v }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                ),
                singleLine = true
            )
            Text(
                text = "FPS 采样刷新间隔，默认 250ms",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Target FPS
        SettingsCard("目标帧率") {
            OutlinedTextField(
                value = targetFps,
                onValueChange = {
                    targetFps = it
                    it.toIntOrNull()?.let { v -> SettingsManager.targetFps = v }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                ),
                singleLine = true
            )
            Text(
                text = "用于掉帧检测基准和 FPS 上限，默认 60fps。120Hz 屏幕请设为 120",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Jank threshold
        SettingsCard("掉帧阈值 (倍数)") {
            OutlinedTextField(
                value = jankThreshold,
                onValueChange = {
                    jankThreshold = it
                    it.toFloatOrNull()?.let { v -> SettingsManager.jankThreshold = v }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                ),
                singleLine = true
            )
            Text(
                text = "帧耗时超过 目标帧时间 × 阈值 时标记为掉帧，默认 1.5x",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Auto start overlay
        SettingsCard("自动开启悬浮窗") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "启动时自动开启悬浮窗",
                    color = Color.White
                )
                Switch(
                    checked = showOverlayOnStart,
                    onCheckedChange = {
                        showOverlayOnStart = it
                        SettingsManager.showOverlayOnStart = it
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF4CAF50),
                        checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Appearance colors
        val presetColors = listOf(
            "#1A1A1A", "#FFFFFF", "#888888", "#E53935", "#FFA000", "#4CAF50", "#2196F3"
        )
        val colorLabels = mapOf(
            "collapsedBgColor" to "收起态背景色",
            "fpsTextColor" to "FPS 文字颜色",
            "chartLineColor" to "图表线颜色",
            "panelBgColor" to "面板背景色"
        )

        SettingsCard("外观配色") {
            ColorPickerRow(
                label = colorLabels["collapsedBgColor"]!!,
                currentColor = collapsedBgColor,
                presetColors = presetColors,
                onColorSelected = { color ->
                    collapsedBgColor = color
                    SettingsManager.collapsedBgColor = color
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            ColorPickerRow(
                label = colorLabels["fpsTextColor"]!!,
                currentColor = fpsTextColor,
                presetColors = presetColors,
                onColorSelected = { color ->
                    fpsTextColor = color
                    SettingsManager.fpsTextColor = color
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            ColorPickerRow(
                label = colorLabels["chartLineColor"]!!,
                currentColor = chartLineColor,
                presetColors = presetColors,
                onColorSelected = { color ->
                    chartLineColor = color
                    SettingsManager.chartLineColor = color
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            ColorPickerRow(
                label = colorLabels["panelBgColor"]!!,
                currentColor = panelBgColor,
                presetColors = presetColors,
                onColorSelected = { color ->
                    panelBgColor = color
                    SettingsManager.panelBgColor = color
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // About section
        SettingsCard("关于") {
            Text(
                text = "FPS Monitor v1.1",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "基于开源项目 Takt、TinyDancer、fpsviewer、Scene 开发\n使用 Choreographer.FrameCallback 进行帧率监测\n通过 Root 权限获取系统硬件数据",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF16213E)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun ColorPickerRow(
    label: String,
    currentColor: String,
    presetColors: List<String>,
    onColorSelected: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 13.sp,
                modifier = Modifier.width(90.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Current color preview dot
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(currentColor)))
                    .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            // Preset color dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                presetColors.forEach { colorHex ->
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(colorHex)))
                            .border(
                                width = if (currentColor == colorHex) 2.dp else 1.dp,
                                color = if (currentColor == colorHex) Color(0xFF4CAF50)
                                        else Color.White.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                            .clickable { onColorSelected(colorHex) }
                    )
                }
            }
        }
    }
}