package com.ankit.gradientglowtest

import android.animation.ArgbEvaluator
import android.animation.TimeAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ComposeShader
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import org.torproject.vpn.R
import kotlin.math.abs


class GradientGlowView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val rect = Rect()
    private var bitmap: Bitmap? = null
    private lateinit var bitmapCanvas: Canvas
    private lateinit var gradientShader: LinearGradient
    private lateinit var bitmapShader: BitmapShader
    private lateinit var composedShader: ComposeShader

    private val paintForBitmapShader = Paint().apply {
        isDither = true
        strokeWidth = 20f
    }
    private val paintForTransparentGradient = Paint().apply {
        isDither = true

    }


    //    val duration = context.resources.getInteger(R.integer.statusbar_progress_anim_duration)
    val duration = 800
    var startColor = ContextCompat.getColor(context, R.color.connectingRainbowStart)
    var endColor = ContextCompat.getColor(context, R.color.connectingRainbowEnd)
    val blackForGradient = ColorUtils.setAlphaComponent(Color.BLACK, 64)
    val evaluator = ArgbEvaluator()
    val animator = TimeAnimator.ofFloat(0.0f, 1.0f)

    init {
        animator.duration = duration.toLong()
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.REVERSE

        animator.addUpdateListener {
            val fraction = it.animatedFraction
            val midFraction = it.animatedFraction.let { f ->
                val value = 2.0f * f + 0.5f
                if (value > 1.0) {
                    abs(2f - value)
                } else {
                    value
                }
            }
            val newStart = evaluator.evaluate(fraction, startColor, endColor) as Int
            val newMid = evaluator.evaluate(midFraction, startColor, endColor) as Int
            val newEnd = evaluator.evaluate(fraction, endColor, startColor) as Int

            if (width > 0 && height > 0) {
                createGradient(width, height, newStart, newMid, newEnd)
                invalidate()
            }

        }
    }

    fun setState(state: Int){
        when (state){
            1 ->{
                startColor = ContextCompat.getColor(context, R.color.connectingRainbowStart)
                endColor = ContextCompat.getColor(context, R.color.connectingRainbowEnd)
            }
            2 ->{
                startColor = ContextCompat.getColor(context, R.color.greenNormal)
                endColor = ContextCompat.getColor(context, R.color.greenNormal)
            }
            3 ->{
                startColor = ContextCompat.getColor(context, R.color.redNormal)
                endColor = ContextCompat.getColor(context, R.color.redNormal)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animator.start()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    private fun createGradient(w: Int, h: Int, c1: Int, c2: Int, c3: Int) {
        bitmap?.recycle()

        bitmap = Bitmap.createBitmap(w, 1, Bitmap.Config.ARGB_8888)
        bitmapCanvas = Canvas(bitmap!!)

        rect.set(0, 0, w, h)

        val colorShader = LinearGradient(
            0f, 0f, w.toFloat(), 0f,
            intArrayOf(c1, c3),
            floatArrayOf(0f, 1f), Shader.TileMode.CLAMP
        )

        paintForBitmapShader.shader = colorShader
        bitmapCanvas.drawRect(rect, paintForBitmapShader)

        gradientShader = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            intArrayOf(blackForGradient, Color.TRANSPARENT),
            floatArrayOf(0f, 0.4f), Shader.TileMode.CLAMP
        )

        bitmapShader = BitmapShader(bitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

        composedShader = ComposeShader(bitmapShader, gradientShader, PorterDuff.Mode.DST_IN)

        paintForTransparentGradient.shader = composedShader
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawLine(0f, 0f, width.toFloat(), 0f, paintForBitmapShader)
        canvas.drawRect(0f, 10f, width.toFloat(), height.toFloat() - 20, paintForTransparentGradient)
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
    }

}