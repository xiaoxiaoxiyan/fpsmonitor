package com.fpsmonitor.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.fpsmonitor.core.FpsRecord

/**
 * FpsChartView - Canvas-based FPS line chart.
 *
 * Reference: fpsviewer's chart visualization approach.
 * Uses Compose Canvas to draw a simple FPS over time line chart.
 * No third-party chart library needed.
 */
@Composable
fun FpsChartView(
    records: List<FpsRecord>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF4CAF50),
    targetFps: Int = 60
) {
    if (records.isEmpty()) return

    val maxFps = maxOf(records.maxOf { it.fps }, targetFps).toFloat()
    val minFps = 0f

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val width = size.width
        val height = size.height
        val padding = 40f

        // Draw target FPS line
        val targetY = height - padding - ((targetFps - minFps) / (maxFps - minFps)) * (height - padding * 2)
        drawLine(
            color = Color(0x44FFFFFF),
            start = Offset(padding, targetY),
            end = Offset(width - padding, targetY),
            strokeWidth = 1f
        )

        // Draw FPS line chart
        if (records.size >= 2) {
            val path = Path()
            val stepX = (width - padding * 2) / (records.size - 1).coerceAtLeast(1)

            records.forEachIndexed { index, record ->
                val x = padding + index * stepX
                val y = height - padding - ((record.fps - minFps) / (maxFps - minFps)) * (height - padding * 2)

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(
                    width = 2.5f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        // Draw axes labels
        val paint = android.graphics.Paint().apply {
            color = 0x88FFFFFF.toInt()
            textSize = 24f
            isAntiAlias = true
        }

        // Y-axis: FPS values
        drawContext.canvas.nativeCanvas.drawText("${maxFps.toInt()} fps", padding, padding, paint)
        drawContext.canvas.nativeCanvas.drawText("0 fps", padding, height - padding + 24f, paint)

        // X-axis: Target FPS label
        drawContext.canvas.nativeCanvas.drawText(
            "Target: $targetFps fps",
            width - padding - 120f,
            targetY - 8f,
            paint
        )
    }
}