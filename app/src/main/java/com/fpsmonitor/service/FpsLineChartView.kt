package com.fpsmonitor.service

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import com.fpsmonitor.core.FpsRecord

/**
 * FpsLineChartView - Custom View that draws a real-time FPS line chart.
 * Blue line for FPS, gray dashed line for target FPS reference.
 */
class FpsLineChartView(context: Context) : View(context) {

    private var records: List<FpsRecord> = emptyList()
    private var targetFps = 60

    private val linePaint = Paint().apply {
        color = Color.parseColor("#2196F3")
        strokeWidth = 3f
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val targetLinePaint = Paint().apply {
        color = Color.parseColor("#CCCCCC")
        strokeWidth = 1.5f
        style = Paint.Style.STROKE
        isAntiAlias = true
        pathEffect = DashPathEffect(floatArrayOf(8f, 4f), 0f)
    }

    private val textPaint = Paint().apply {
        color = Color.parseColor("#666666")
        textSize = 22f
        isAntiAlias = true
    }

    private val bgPaint = Paint().apply {
        color = Color.parseColor("#F8F8F8")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    fun updateData(data: List<FpsRecord>, target: Int = 60) {
        records = data
        targetFps = target
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        val padding = 36f
        val chartW = w - padding * 2
        val chartH = h - padding * 2

        // Background
        canvas.drawRoundRect(padding, padding, w - padding, h - padding, 8f, 8f, bgPaint)

        if (records.isEmpty()) return

        val maxFps = maxOf(records.maxOf { it.fps }, targetFps).toFloat()
        val minFps = 0f

        // Draw target FPS line
        val targetY = h - padding - ((targetFps - minFps) / (maxFps - minFps)) * chartH
        canvas.drawLine(padding, targetY, w - padding, targetY, targetLinePaint)

        // Draw FPS line
        if (records.size >= 2) {
            val path = Path()
            val stepX = chartW / (records.size - 1).coerceAtLeast(1)

            records.forEachIndexed { index, record ->
                val x = padding + index * stepX
                val y = h - padding - ((record.fps - minFps) / (maxFps - minFps)) * chartH

                if (index == 0) path.moveTo(x, y)
                else path.lineTo(x, y)
            }
            canvas.drawPath(path, linePaint)
        }

        // Labels
        canvas.drawText("${maxFps.toInt()}", padding + 2f, padding + 18f, textPaint)
        canvas.drawText("0", padding + 2f, h - padding - 4f, textPaint)
        canvas.drawText("target:$targetFps", w - padding - 100f, targetY - 6f, textPaint)
    }
}