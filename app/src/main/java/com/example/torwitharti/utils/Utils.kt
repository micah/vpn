package com.example.torwitharti.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat


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
fun startVectorAnimationWithEndCallback(drawable: Drawable, lifecycle: Lifecycle, onAnimationEnd: () -> Unit) {
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
            override fun onAnimationEnd(animation: Animator?) {
                onAnimationEnd()
            }
        })

        start()
    }
}

fun animateTextSizeChange(textView: TextView, startSize: Float, endSize: Float, lifecycle: Lifecycle, endCallback: () -> Unit) {
    val animationDuration: Long = 6000 // Animation duration in ms

    val animator = ValueAnimator.ofFloat(startSize, endSize)
    animator.duration = animationDuration

    animator.addUpdateListener { valueAnimator ->
        textView.textSize = (valueAnimator.animatedValue as Float)
    }

    animator.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
            endCallback()
        }
    })

    animator.start()
}