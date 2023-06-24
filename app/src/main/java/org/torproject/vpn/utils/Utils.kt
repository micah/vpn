package org.torproject.vpn.utils

import android.animation.*
import android.content.Context
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Looper
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import org.torproject.vpn.R
import kotlin.math.abs
import java.text.SimpleDateFormat
import java.util.*


/**
 * this can be an extension function but bifurcation between AnimatedVectorDrawableCompat and AnimatedVectorDrawable needs to be done manually.
 */
fun repeatVectorAnimation(drawable: Drawable, lifecycle: Lifecycle) {
    fun startIfResumed() {
        startVectorAnimationWithEndCallback(drawable, lifecycle) { startIfResumed() }
    }
    startIfResumed()
}

/**
 * Top level function for end animation callback. Mainly to use with vector animation but can be used with anything that uses #AnimatedVectorDrawableCompat.registerAnimationCallback
 */
fun startVectorAnimationWithEndCallback(
    drawable: Drawable,
    lifecycle: Lifecycle,
    onAnimationEnd: () -> Unit
) {
    AnimatedVectorDrawableCompat.registerAnimationCallback(
        drawable,
        object : Animatable2Compat.AnimationCallback() {
            override fun onAnimationEnd(drawable: Drawable?) {
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    onAnimationEnd()
                }
            }
        })
    (drawable as Animatable).start()
}

/**
 * Wrapper around animator listener
 */
fun startRevealWithEndCallback(animator: Animator, onAnimationEnd: () -> Unit) {
    with(animator) {
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onAnimationEnd()
            }
        })

        start()
    }
}

fun animateTextSizeChange(
    textView: TextView,
    startSize: Float,
    endSize: Float,
    lifecycle: Lifecycle,
    endCallback: () -> Unit
) {
    val animationDuration: Long = 6000 // Animation duration in ms

    val animator = ValueAnimator.ofFloat(startSize, endSize)
    animator.duration = animationDuration

    animator.addUpdateListener { valueAnimator ->
        textView.textSize = (valueAnimator.animatedValue as Float)
    }

    animator.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                endCallback()
            }
        }
    })

    animator.start()
}

fun connectionStateGradientAnimation(
    drawable: Drawable,
    context: Context,
    @ColorRes sixColors: IntArray
) {

    val colors = sixColors.map { ContextCompat.getColor(context, it) }
    val duration = context.resources.getInteger(R.integer.default_transition_anim_duration)
    //val gradient = drawable as GradientDrawable
    val gd = GradientDrawable()
    gd.orientation = GradientDrawable.Orientation.TOP_BOTTOM
    val evaluator = ArgbEvaluator()
    val animator = TimeAnimator.ofFloat(0.0f, 1.0f)
    animator.duration = duration.toLong()
    animator.interpolator = AccelerateInterpolator()
    animator.addUpdateListener {
        val fraction = it.animatedFraction

        val newStart = evaluator.evaluate(fraction, colors[0], colors[3]) as Int
        val newMid = evaluator.evaluate(fraction, colors[1], colors[4]) as Int
        val newEnd = evaluator.evaluate(fraction, colors[2], colors[5]) as Int

        gd.colors = intArrayOf(newStart, newMid, newEnd)
        //TODO cant use gd here.... :(. custom view required.
        drawable.setTint(newMid)
    }

    animator.start()
}


fun createStatusBarAnimation(
    drawable: Drawable,
    context: Context,
    @ColorRes twoColors: IntArray
): Animator {

//    val duration = context.resources.getInteger(R.integer.statusbar_progress_anim_duration)
    val duration = 800
    val colors = twoColors.map { ContextCompat.getColor(context, it) }
//content.background is set as a GradientDrawable in layout xml file
    val gradient = drawable as GradientDrawable
    val evaluator = ArgbEvaluator()
    val animator = TimeAnimator.ofFloat(0.0f, 1.0f)
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
        val newStart = evaluator.evaluate(fraction, colors[0], colors[1]) as Int
        val newMid = evaluator.evaluate(midFraction, colors[0], colors[1]) as Int
        val newEnd = evaluator.evaluate(fraction, colors[1], colors[0]) as Int

        gradient.colors = intArrayOf(newEnd, newMid, newStart)
    }

    return animator

}

/**
 * @param threeColors the first two colors are the end state color of connecting anim, the third one should be for paused of connected.
 */
fun createStatusBarConnectedGradientAnimation(
    drawable: Drawable,
    context: Context,
    @ColorRes threeColors: IntArray,
    lifecycle: Lifecycle,
    endCallback: () -> Unit
) {

    val colors = threeColors.map { ContextCompat.getColor(context, it) }
    val duration = 1800
    val gradient = drawable as GradientDrawable
    gradient.orientation = GradientDrawable.Orientation.LEFT_RIGHT
    val evaluator = ArgbEvaluator()
    val animator = TimeAnimator.ofFloat(0.0f, 1.0f)
    animator.duration = duration.toLong()
    animator.interpolator = DecelerateInterpolator()
    animator.addUpdateListener {
        val fraction = it.animatedFraction

        val newStart = evaluator.evaluate(fraction, colors[0], colors[2]) as Int
        val newEnd = evaluator.evaluate(fraction, colors[1], colors[2]) as Int

        gradient.colors = intArrayOf(newStart, newEnd)
    }
    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
        endCallback()
    }

    animator.start()
}

fun isRunningOnMainThread(): Boolean {
    return Looper.getMainLooper().thread === Thread.currentThread()
}

fun getFormattedDate(timestamp: Long, locale: Locale?): String? {
    val sdf = SimpleDateFormat("dd/mm/yy, hh:mm:ss.SSS", locale)
    return sdf.format(timestamp)
}