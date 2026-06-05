package com.fpsmonitor.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fpsmonitor.core.FpsMonitor
import com.fpsmonitor.core.FpsRecorder
import com.fpsmonitor.core.RecordingSession
import com.fpsmonitor.ui.chart.FpsChartView

@Composable
fun MonitorScreen(
    hasRoot: Boolean,
    rootStatus: String,
    isOverlayRunning: Boolean,
    onStartOverlay: () -> Unit,
    onStopOverlay: () -> Unit
) {
    val fpsMonitor = remember { FpsMonitor() }
    val fpsData by fpsMonitor.fpsData.collectAsState()
    var isLocalMonitoring by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var lastSession by remember { mutableStateOf<RecordingSession?>(null) }
    var showChart by remember { mutableStateOf(false) }
    val records by fpsMonitor.records.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Root status card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (hasRoot)
                    Color(0xFF1B5E20).copy(alpha = 0.3f)
                else
                    Color(0xFFB71C1C).copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (hasRoot) "\u2713" else "\u2717",
                    color = if (hasRoot) Color(0xFF4CAF50) else Color(0xFFF44336),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = rootStatus,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (hasRoot) "悬浮窗权限已自动授予" else "需要 Root 权限才能使用",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // FPS big number display
        Text(
            text = "FPS",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp
        )
        Text(
            text = if (isLocalMonitoring) "${fpsData.fps}" else "--",
            color = when {
                !isLocalMonitoring -> Color.White.copy(alpha = 0.4f)
                fpsData.fps >= 50 -> Color(0xFF4CAF50)
                fpsData.fps >= 30 -> Color(0xFFFFC107)
                else -> Color(0xFFF44336)
            },
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold
        )

        if (isLocalMonitoring) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("当前", "${fpsData.fps}")
                StatItem("平均", "${fpsData.avgFps.toInt()}")
                StatItem("最低", "${fpsData.minFps}")
                StatItem("最高", "${fpsData.maxFps}")
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("掉帧", "${fpsData.jankCount}")
                StatItem("帧耗时", "${fpsData.frameTimeMs.toInt()}ms")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (isLocalMonitoring) {
                        fpsMonitor.stop()
                        isLocalMonitoring = false
                    } else {
                        fpsMonitor.start()
                        isLocalMonitoring = true
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLocalMonitoring)
                        Color(0xFFF44336) else Color(0xFF4CAF50)
                )
            ) {
                Text(if (isLocalMonitoring) "停止监测" else "开始监测")
            }

            Button(
                onClick = {
                    if (isRecording) {
                        lastSession = fpsMonitor.stopRecording()
                        showChart = true
                        isRecording = false
                    } else {
                        fpsMonitor.startRecording()
                        isRecording = true
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = isLocalMonitoring,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording)
                        Color(0xFFFF9800) else Color(0xFF2196F3)
                )
            ) {
                Text(if (isRecording) "停止记录" else "开始记录")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Overlay control
        Button(
            onClick = {
                if (isOverlayRunning) onStopOverlay() else onStartOverlay()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = hasRoot,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isOverlayRunning)
                    Color(0xFFF44336) else Color(0xFF9C27B0)
            )
        ) {
            Text(if (isOverlayRunning) "关闭悬浮窗" else "开启悬浮窗")
        }

        // Chart display
        if (showChart && lastSession != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF16213E)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "最近记录",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem("平均", "${lastSession!!.avgFps.toInt()}")
                        StatItem("最低", "${lastSession!!.minFps}")
                        StatItem("最高", "${lastSession!!.maxFps}")
                        StatItem("掉帧", "${lastSession!!.totalJanks}")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    FpsChartView(records = lastSession!!.records)
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp
        )
    }
}