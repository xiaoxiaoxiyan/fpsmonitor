package com.fpsmonitor.service

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.View

/**
 * CpuFreqChartView - Custom View that draws CPU frequency curves for each core.
 * Each core gets a different color.
 */
class CpuFreqChartView(context: Context) : View(context) {

    // History: List of core frequency snapshots (each snapshot is a list of per-core frequencies)
    private var history: List<List<Long>> = emptyList()
    private var coreCount = 0

    private val coreColors = intArrayOf(
        Color.parseColor("#F44336"), // Red
        Color.parseColor("#2196F3"), // Blue
        Color.parseColor("#4CAF50"), // Green
        Color.parseColor("#FF9800"), // Orange
        Color.parseColor("#9C27B0"), // Purple
        Color.parseColor("#00BCD4"), // Cyan
        Color.parseColor("#E91E63"), // Pink
        Color.parseColor("#795548")  // Brown
    )

    private val paints = mutableListOf<Paint>()
    private val legendPaint = Paint().apply {
        textSize = 20f
        isAntiAlias = true
    }

    private val bgPaint = Paint().apply {
        color = Color.parseColor("#F5F5F5")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.parseColor("#888888")
        textSize = 20f
        isAntiAlias = true
    }

    init {
        for (color in coreColors) {
            paints.add(Paint().apply {
                this.color = color
                strokeWidth = 2f
                style = Paint.Style.STROKE
                isAntiAlias = true
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            })
        }
    }

    fun updateData(freqHistory: List<List<Long>>, cores: Int) {
        history = freqHistory
        coreCount = cores
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0 || history.isEmpty()) return

        val padding = 36f
        val chartW = w - padding * 2
        val chartH = h - padding * 2

        // Background
        canvas.drawRoundRect(padding, padding, w - padding, h - padding, 8f, 8f, bgPaint)

        // Find max frequency across all data
        var maxFreq = 0L
        for (snapshot in history) {
            for (freq in snapshot) {
                if (freq > maxFreq) maxFreq = freq
            }
        }
        if (maxFreq == 0L) maxFreq = 3000L

        // Draw lines for each core
        for (core in 0 until coreCount.coerceAtMost(8)) {
            if (history.size < 2) continue

            val path = Path()
            val stepX = chartW / (history.size - 1).coerceAtLeast(1)
            var valid = false

            for (i in history.indices) {
                val snapshot = history[i]
                if (core >= snapshot.size) continue

                val x = padding + i * stepX
                val freq = snapshot[core]
                val y = h - padding - (freq.toFloat() / maxFreq.toFloat()) * chartH

                if (!valid) {
                    path.moveTo(x, y)
                    valid = true
                } else {
                    path.lineTo(x, y)
                }
            }
            if (valid) {
                canvas.drawPath(path, paints[core])
            }
        }

        // Y-axis labels
        canvas.drawText("${maxFreq}MHz", padding + 2f, padding + 18f, textPaint)
        canvas.drawText("0", padding + 2f, h - padding - 4f, textPaint)

        // Legend
        val legendY = padding + 4f
        val legendSpacing = 90f
        for (core in 0 until coreCount.coerceAtMost(8)) {
            val x = padding + 60f + core * legendSpacing
            if (x + 60f > w - padding) break

            legendPaint.color = coreColors[core]
            val rect = RectF(x, legendY, x + 30f, legendY + 16f)
            canvas.drawRect(rect, legendPaint.apply { style = Paint.Style.FILL })
            legendPaint.color = Color.parseColor("#888888")
            canvas.drawText("C$core", x + 34f, legendY + 14f, legendPaint)
        }
    }
}