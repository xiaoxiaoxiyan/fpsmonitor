package com.fpsmonitor.service

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

/**
 * FrameTimeChartView - Bar chart showing frame time of the last 30 frames.
 * Cyan bars (#00ACC1), light background (#F0F4F8), secondary text (#607D8B).
 */
class FrameTimeChartView(context: Context) : View(context) {

    private var frameTimes: List<Float> = emptyList()

    private val barPaint = Paint().apply {
        color = Color.parseColor("#00ACC1")
        style = Paint.Style.FILL
        isAntiAlias = true
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
        frameTimes = data
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

        if (frameTimes.isEmpty()) return

        val maxTime = frameTimes.maxOrNull()?.coerceAtLeast(1f) ?: 1f
        val barCount = frameTimes.size
        val barWidth = (chartW / barCount) * 0.7f
        val barGap = (chartW / barCount) * 0.3f

        for (i in frameTimes.indices) {
            val ft = frameTimes[i]
            val barH = (ft / maxTime) * chartH
            val left = padding + i * (chartW / barCount) + barGap / 2f
            val top = h - padding - barH
            val right = left + barWidth
            canvas.drawRect(left, top, right, h - padding, barPaint)
        }

        // Labels
        canvas.drawText("${maxTime.toInt()}ms", padding + 2f, padding + 18f, textPaint)
        canvas.drawText("0ms", padding + 2f, h - padding - 4f, textPaint)
    }
}