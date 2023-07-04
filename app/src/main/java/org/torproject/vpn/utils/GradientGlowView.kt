package org.torproject.vpn.utils

import android.animation.AnimatorSet
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
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import org.torproject.vpn.R
import org.torproject.vpn.vpn.ConnectionState

/**
 * State indicator View for Connect fragment.
 */
class GradientGlowView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private lateinit var bitmap: Bitmap
    private lateinit var bitmapCanvas: Canvas

    //initial state is just transparent
    private var colorConfig: ColorConfig = StaticColorConfig(android.R.color.transparent, null)

    private val paintForBitmapShader = Paint().apply {
        isDither = true
        strokeWidth = 28f
    }
    private val paintForTransparentGradient = Paint().apply {
        isDither = true
    }

    /**
     * black with alpha for ComposedShader.
     */
    private val blackForGradient = ColorUtils.setAlphaComponent(Color.BLACK, 48)

    init {
        //set current config to animate. This prevents black background for entire view until state is set
        //TODO test required to see if this is necessary
        colorConfig.animate()
    }

    /**
     * set VPN state for this view to sync up with
     */
    fun setState(connectionState: ConnectionState) {
        colorConfig = when (connectionState) {
            ConnectionState.INIT, ConnectionState.PAUSED -> StaticColorConfig(android.R.color.transparent, previousConfig = colorConfig)
            ConnectionState.CONNECTING -> AnimatedColorConfig(previousConfig = colorConfig)
            ConnectionState.CONNECTED -> StaticColorConfig(R.color.greenNormal, previousConfig = colorConfig)
            ConnectionState.DISCONNECTING -> AnimatedColorConfig(previousConfig = colorConfig)
            ConnectionState.DISCONNECTED -> StaticColorConfig(R.color.redNormal, previousConfig = colorConfig)
            ConnectionState.CONNECTION_ERROR -> StaticColorConfig(R.color.yellowNormal, previousConfig = colorConfig)
        }
        colorConfig.animate()
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        colorConfig.onViewAttachedToWindow()

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        colorConfig.onViewDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (this::bitmap.isInitialized) {
            bitmap.recycle()
        }
        bitmap = Bitmap.createBitmap(width, 1, Bitmap.Config.ARGB_8888)
        bitmapCanvas = Canvas(bitmap)
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isInEditMode) {
            return
        }
        //draw opaque 20f thick line
        canvas.drawLine(0f, 0f, width.toFloat(), 0f, paintForBitmapShader)
        //draw the glow
        //todo the 'top' padding needs ot be calculated for 20f stroke width, although it kind of works with approximate value
        canvas.drawRect(0f, 10f, width.toFloat(), height.toFloat() - 20, paintForTransparentGradient)
    }

    /**
     * Color config parent. Provides abstraction for managing animation start/stop and shader calculations.
     */
    abstract inner class ColorConfig {
        @ColorInt
        var color1Current: Int = 0
        @ColorInt
        var color2Current: Int = 0
        val animatorSet = AnimatorSet()
        private val evaluator = ArgbEvaluator()
        private var waitingToBeAttachedToWindow: Boolean = false

        fun onViewAttachedToWindow() {
            if (waitingToBeAttachedToWindow) {
                animatorSet.start()
            }
        }

        fun onViewDetachedFromWindow() {
            animatorSet.cancel()
        }

        fun animate() {
            if (isAttachedToWindow) {
                animatorSet.start()
            } else {
                waitingToBeAttachedToWindow = true
            }
        }

        private fun calculateCurrentColor(fraction: Float, color1Start: Int, color1End: Int, color2Start: Int, color2End: Int) {
            color1Current = evaluator.evaluate(fraction, color1Start, color1End) as Int
            color2Current = evaluator.evaluate(fraction, color2Start, color2End) as Int
        }

        protected fun calculateShaders(fraction: Float, color1Start: Int, color1End: Int, color2Start: Int, color2End: Int) {
            calculateCurrentColor(fraction, color1Start, color1End, color2Start, color2End)

            if (this@GradientGlowView::bitmapCanvas.isInitialized) {

                paintForBitmapShader.shader = LinearGradient(
                    0f, 0f, width.toFloat(), 0f,
                    intArrayOf(color1Current, color2Current),
                    floatArrayOf(0f, 1f), Shader.TileMode.CLAMP
                )

                val gradientShader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    intArrayOf(blackForGradient, Color.TRANSPARENT),
                    floatArrayOf(0f, 0.8f), Shader.TileMode.CLAMP
                )

                bitmapCanvas.drawRect(0f, 0f, width.toFloat(), 1f, paintForBitmapShader)

                val bitmapShader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

                paintForTransparentGradient.shader = ComposeShader(bitmapShader, gradientShader, PorterDuff.Mode.DST_IN)

                invalidate()
            }

        }
    }

    /**
     * Static color configuration for this view. This will animate from current color(s) to the static color passed in the argument.
     */
    inner class StaticColorConfig(@ColorRes color: Int, previousConfig: ColorConfig?) : ColorConfig() {
        @ColorInt
        val color1: Int = ContextCompat.getColor(context, color)

        init {

            previousConfig?.animatorSet?.cancel()

            val animatorA = TimeAnimator.ofFloat(0f, 1f)
            animatorA.duration = 800

            animatorA.addUpdateListener {
                calculateShaders(
                    it.animatedFraction,
                    previousConfig?.color1Current ?: android.R.color.transparent,
                    color1,
                    previousConfig?.color2Current ?: android.R.color.transparent,
                    color1
                )
            }

            animatorSet.play(animatorA)
        }

    }

    /**
     * Static color configuration for this view.
     * First the current colors are animated to progress colors(pink and green), those colors are then animated indefinitely!
     */
    inner class AnimatedColorConfig(previousConfig: ColorConfig?) : ColorConfig() {
        @ColorInt
        val color1Start: Int = ContextCompat.getColor(context, R.color.connectingRainbowStart)

        @ColorInt
        val color1End: Int = ContextCompat.getColor(context, R.color.connectingRainbowEnd)

        @ColorInt
        val color2Start: Int = ContextCompat.getColor(context, R.color.connectingRainbowEnd)

        @ColorInt
        val color2End: Int = ContextCompat.getColor(context, R.color.connectingRainbowStart)

        init {
            previousConfig?.animatorSet?.cancel()

            val animatorA = TimeAnimator.ofFloat(0f, 1f)
            animatorA.duration = 800
            animatorA.addUpdateListener {
                calculateShaders(
                    it.animatedFraction,
                    previousConfig?.color1Current ?: android.R.color.transparent,
                    color1Start,
                    previousConfig?.color2Current ?: android.R.color.transparent,
                    color2Start
                )
            }

            val animatorB = TimeAnimator.ofFloat(0f, 1f)
            animatorB.duration = 600
            animatorB.repeatCount = ValueAnimator.INFINITE
            animatorB.repeatMode = ValueAnimator.REVERSE

            animatorB.addUpdateListener {
                calculateShaders(
                    it.animatedFraction,
                    color1Start,
                    color1End,
                    color2Start,
                    color2End

                )
            }

            animatorSet.playSequentially(animatorA, animatorB)

        }
    }
}
