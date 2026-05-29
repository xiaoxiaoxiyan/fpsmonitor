package com.xtoolbox

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scrollView = ScrollView(this)

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(24), dpToPx(48), dpToPx(24), dpToPx(24))
        }

        val title = TextView(this).apply {
            text = "XToolbox"
            textSize = 32f
            setTextColor(Color.parseColor("#4A9C6D"))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        container.addView(title)

        val version = TextView(this).apply {
            text = "v1.0.0"
            textSize = 14f
            setTextColor(Color.parseColor("#717971"))
        }
        container.addView(version)

        addSpacer(container, 24)

        addSectionTitle(container, "设备信息")

        addInfoRow(container, "设备", "${Build.MANUFACTURER} ${Build.MODEL}")
        addInfoRow(container, "Android", "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        addInfoRow(container, "品牌", Build.BRAND)
        addInfoRow(container, "SoC", Build.SOC_MODEL ?: "Unknown")
        addInfoRow(container, "安全补丁", Build.VERSION.SECURITY_PATCH)
        addInfoRow(container, "指纹", Build.FINGERPRINT)

        addSpacer(container, 24)

        addSectionTitle(container, "功能列表")

        val features = listOf(
            "首页 - 设备状态总览",
            "脚本 - 脚本管理与执行",
            "模块 - 模块安装与管理",
            "文件 - 文件管理器",
            "终端 - 终端模拟器",
            "更多 - 高级工具集"
        )
        for (feature in features) {
            val tv = TextView(this).apply {
                text = "\u2022 $feature"
                textSize = 14f
                setTextColor(Color.parseColor("#414941"))
                setPadding(0, dpToPx(4), 0, dpToPx(4))
            }
            container.addView(tv)
        }

        addSpacer(container, 24)

        val note = TextView(this).apply {
            text = "提示：此为简化编译版本，完整功能需要 Root 权限及完整依赖库支持。"
            textSize = 12f
            setTextColor(Color.parseColor("#717971"))
        }
        container.addView(note)

        scrollView.addView(container)
        setContentView(scrollView)
    }

    private fun addSectionTitle(container: LinearLayout, title: String) {
        val tv = TextView(this).apply {
            text = title
            textSize = 18f
            setTextColor(Color.parseColor("#111111"))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, dpToPx(12))
        }
        container.addView(tv)
    }

    private fun addInfoRow(container: LinearLayout, label: String, value: String) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dpToPx(4), 0, dpToPx(4))
        }

        val labelTv = TextView(this).apply {
            text = label
            textSize = 14f
            setTextColor(Color.parseColor("#717971"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        row.addView(labelTv)

        val valueTv = TextView(this).apply {
            text = value
            textSize = 14f
            setTextColor(Color.parseColor("#191D18"))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
        }
        row.addView(valueTv)

        container.addView(row)
    }

    private fun addSpacer(container: LinearLayout, dp: Int) {
        val spacer = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(dp))
        }
        container.addView(spacer)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
