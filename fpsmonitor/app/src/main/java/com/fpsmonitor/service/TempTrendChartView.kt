package com.fpsmonitor.service

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.View

/**
 * TempTrendChartView - Line chart showing CPU temperature trend (last 30 samples).
 * Teal line (#00897B), light background (#F0F4F8), secondary text (#607D8B).
 */
class TempTrendChartView(context: Context) : View(context) {

    private var temps: List<Float> = emptyList()

    private val linePaint = Paint().apply {
        color = Color.parseColor("#00897B")
        strokeWidth = 3f
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val textPaint = Paint().apply {
        color = Color.parseColor("#607D8B")
        textSize = 22f
        isAntiAlias = true
    }

    private val bgPaint = Paint().apply {
        color = Color.parseColor("#F0F4F8")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    fun updateData(data: List<Float>) {
        temps = data
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

        if (temps.size < 2) return

        val maxTemp = temps.maxOrNull()?.coerceAtLeast(1f) ?: 50f
        val minTemp = temps.minOrNull()?.coerceAtMost(maxTemp) ?: 0f

        val path = Path()
        val stepX = chartW / (temps.size - 1).coerceAtLeast(1)

        for (i in temps.indices) {
            val x = padding + i * stepX
            val y = h - padding - ((temps[i] - minTemp) / (maxTemp - minTemp)) * chartH

            if (i == 0) path.moveTo(x, y)
            else path.lineTo(x, y)
        }
        canvas.drawPath(path, linePaint)

        // Labels
        canvas.drawText("${maxTemp.toInt()}°C", padding + 2f, padding + 18f, textPaint)
        canvas.drawText("${minTemp.toInt()}°C", padding + 2f, h - padding - 4f, textPaint)
    }
}