package com.xtoolbox

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity

class SplashActivity : ComponentActivity() {

    private lateinit var dot: View
    private lateinit var ping: View
    private lateinit var line: View
    private lateinit var titleContainer: LinearLayout
    private lateinit var subtitle: TextView
    private lateinit var decoLine: View
    private lateinit var particlesContainer: FrameLayout
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootView = FrameLayout(this).apply {
            setBackgroundColor(0xFFFFFFFF.toInt())
        }

        particlesContainer = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        rootView.addView(particlesContainer)
        createParticles()

        val centerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        val stageLayout = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        dot = View(this).apply {
            setBackgroundColor(0xFF111111.toInt())
            val size = dpToPx(14)
            layoutParams = FrameLayout.LayoutParams(size, size).apply {
                gravity = Gravity.CENTER
            }
            alpha = 0f
        }
        stageLayout.addView(dot)

        ping = View(this).apply {
            background = createPingDrawable()
            val size = dpToPx(14)
            layoutParams = FrameLayout.LayoutParams(size, size).apply {
                gravity = Gravity.CENTER
            }
            alpha = 0f
        }
        stageLayout.addView(ping)

        line = View(this).apply {
            setBackgroundColor(0xFF111111.toInt())
            layoutParams = FrameLayout.LayoutParams(0, dpToPx(2)).apply {
                gravity = Gravity.CENTER
                topMargin = dpToPx(30)
            }
        }
        stageLayout.addView(line)

        titleContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
                topMargin = dpToPx(30)
            }
        }

        val letters = "XTOOLBOX"
        for (ch in letters) {
            val tv = TextView(this).apply {
                text = ch.toString()
                setTextColor(0xFF111111.toInt())
                textSize = 36f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                letterSpacing = 0.15f
                alpha = 0f
                translationY = dpToPx(60).toFloat()
                scaleX = 0.7f
                scaleY = 0.7f
                rotation = -6f
            }
            titleContainer.addView(tv)
        }
        stageLayout.addView(titleContainer)

        centerLayout.addView(stageLayout)

        subtitle = TextView(this).apply {
            text = "一个没什么用，但又有点用的工具箱"
            setTextColor(0xFF111111.toInt())
            textSize = 13f
            letterSpacing = 0.08f
            alpha = 0f
            translationY = dpToPx(20).toFloat()
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(16)
            }
        }
        centerLayout.addView(subtitle)

        decoLine = View(this).apply {
            setBackgroundColor(0xFFDDDDDD.toInt())
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(1)).apply {
                gravity = Gravity.CENTER
                topMargin = dpToPx(24)
            }
        }
        centerLayout.addView(decoLine)

        rootView.addView(centerLayout)
        setContentView(rootView)

        startAnimation()
    }

    private fun startAnimation() {
        animateDotIn()

        handler.postDelayed({
            animatePing()
            animateDotOut()

            handler.postDelayed({
                animateLineExpand()

                handler.postDelayed({
                    animateLineVanish()
                    animateTitleReveal()

                    handler.postDelayed({
                        animateSubtitleReveal()

                        handler.postDelayed({
                            animateDecoLine()

                            handler.postDelayed({
                                navigateToMain()
                            }, 1800)
                        }, 400)
                    }, 900)
                }, 850)
            }, 100)
        }, 1000)
    }

    private fun animateDotIn() {
        dot.alpha = 0f
        dot.scaleX = 0.15f
        dot.scaleY = 0.15f
        dot.translationY = dpToPx(70).toFloat()

        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 900
        animator.interpolator = OvershootInterpolator(1.8f)
        animator.addUpdateListener { anim ->
            val f = anim.animatedFraction
            when {
                f < 0.45f -> {
                    val p = f / 0.45f
                    dot.translationY = dpToPx(70) * (1f - p * 1.3f)
                    dot.scaleX = 0.15f + p * 1.05f
                    dot.scaleY = 0.15f + p * 1.05f
                    dot.alpha = p
                }
                f < 0.65f -> {
                    val p = (f - 0.45f) / 0.2f
                    dot.translationY = dpToPx(-22) + dpToPx(32) * p
                    dot.scaleX = 1.2f - p * 0.3f
                    dot.scaleY = 1.2f - p * 0.3f
                    dot.alpha = 1f
                }
                else -> {
                    val p = (f - 0.65f) / 0.35f
                    dot.translationY = dpToPx(10) * (1f - p)
                    dot.scaleX = 0.9f + p * 0.1f
                    dot.scaleY = 0.9f + p * 0.1f
                    dot.alpha = 1f
                }
            }
        }
        animator.start()
    }

    private fun animateDotOut() {
        dot.animate().alpha(0f).setDuration(300).start()
    }

    private fun animatePing() {
        ping.alpha = 0.6f
        ping.scaleX = 1f
        ping.scaleY = 1f

        ping.animate()
            .alpha(0f)
            .scaleX(4f)
            .scaleY(4f)
            .setDuration(600)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun animateLineExpand() {
        val targetWidth = minOf(dpToPx(320), (resources.displayMetrics.widthPixels * 0.75).toInt())
        val animator = ValueAnimator.ofInt(0, targetWidth)
        animator.duration = 800
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { anim ->
            val w = anim.animatedValue as Int
            line.layoutParams = FrameLayout.LayoutParams(w, dpToPx(2)).apply {
                gravity = Gravity.CENTER
                topMargin = dpToPx(30)
            }
        }
        animator.start()
    }

    private fun animateLineVanish() {
        line.animate().alpha(0f).setDuration(500).start()
    }

    private fun animateTitleReveal() {
        val childCount = titleContainer.childCount
        for (i in 0 until childCount) {
            val tv = titleContainer.getChildAt(i) as TextView
            val delay = i * 90L

            handler.postDelayed({
                tv.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .rotation(0f)
                    .setDuration(650)
                    .setInterpolator(OvershootInterpolator(1.7))
                    .start()
            }, delay)
        }
    }

    private fun animateSubtitleReveal() {
        subtitle.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(1000)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun animateDecoLine() {
        val targetWidth = minOf(dpToPx(80), (resources.displayMetrics.widthPixels * 0.15).toInt())
        val animator = ValueAnimator.ofInt(0, targetWidth)
        animator.duration = 800
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { anim ->
            val w = anim.animatedValue as Int
            decoLine.layoutParams = LinearLayout.LayoutParams(w, dpToPx(1)).apply {
                gravity = Gravity.CENTER
                topMargin = dpToPx(24)
            }
        }
        animator.start()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun createParticles() {
        for (i in 0 until 20) {
            val particle = View(this).apply {
                setBackgroundColor(0xFFE8E8E8.toInt())
                val size = dpToPx(3)
                layoutParams = FrameLayout.LayoutParams(size, size).apply {
                    leftMargin = (Math.random() * resources.displayMetrics.widthPixels).toInt()
                    topMargin = (Math.random() * resources.displayMetrics.heightPixels).toInt()
                }
                alpha = 0f
            }
            particlesContainer.addView(particle)
            animateParticle(particle)
        }
    }

    private fun animateParticle(view: View) {
        val duration = (6000 + Math.random() * 6000).toLong()
        val delay = (Math.random() * 5000).toLong()

        handler.postDelayed({
            view.animate()
                .alpha(0.6f)
                .translationY(-resources.displayMetrics.heightPixels.toFloat())
                .scaleX(0.5f)
                .scaleY(0.5f)
                .setDuration(duration)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction {
                    view.alpha = 0f
                    view.translationY = 0f
                    view.scaleX = 1f
                    view.scaleY = 1f
                    view.layoutParams = FrameLayout.LayoutParams(dpToPx(3), dpToPx(3)).apply {
                        leftMargin = (Math.random() * resources.displayMetrics.widthPixels).toInt()
                        topMargin = (Math.random() * resources.displayMetrics.heightPixels).toInt()
                    }
                    animateParticle(view)
                }
                .start()
        }, delay)
    }

    private fun createPingDrawable(): android.graphics.drawable.Drawable {
        return android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.OVAL
            setColor(0x00000000)
            setStroke(dpToPx(2), 0xFF111111.toInt())
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
