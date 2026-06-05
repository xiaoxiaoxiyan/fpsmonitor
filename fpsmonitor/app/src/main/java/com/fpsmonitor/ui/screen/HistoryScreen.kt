package com.fpsmonitor.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fpsmonitor.core.FpsRecorder
import com.fpsmonitor.core.RecordingSession
import com.fpsmonitor.ui.chart.FpsChartView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var sessions by remember { mutableStateOf(FpsRecorder.getSessions()) }
    var selectedSession by remember { mutableStateOf<RecordingSession?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "历史记录",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无记录\n请在监测页面点击「开始记录」",
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sessions) { session ->
                    SessionCard(
                        session = session,
                        isSelected = selectedSession == session,
                        onSelect = {
                            selectedSession = if (selectedSession == session) null else session
                        },
                        onExport = {
                            scope.launch {
                                FpsRecorder.exportToCsv(context, session)
                            }
                        },
                        onDelete = {
                            FpsRecorder.deleteSession(session.id)
                            if (selectedSession == session) selectedSession = null
                            sessions = FpsRecorder.getSessions()
                        }
                    )
                }
            }

            // Show chart for selected session
            if (selectedSession != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF16213E)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "FPS 曲线",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem("平均", "${selectedSession!!.avgFps.toInt()}")
                            StatItem("最低", "${selectedSession!!.minFps}")
                            StatItem("最高", "${selectedSession!!.maxFps}")
                            StatItem("掉帧", "${selectedSession!!.totalJanks}")
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        FpsChartView(records = selectedSession!!.records)
                    }
                }
            }
        }
    }
}

@Composable
fun SessionCard(
    session: RecordingSession,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                Color(0xFF0F3460) else Color(0xFF16213E)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(Date(session.startTime)),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Avg: ${session.avgFps.toInt()} FPS",
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "Min: ${session.minFps}",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
                Text(
                    text = "Max: ${session.maxFps}",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
                Text(
                    text = "${session.records.size} 样本",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onExport) {
                    Text("导出 CSV", color = Color(0xFF2196F3))
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onDelete) {
                    Text("删除", color = Color(0xFFF44336))
                }
            }
        }
    }
}