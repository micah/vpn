package com.example.torwitharti.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Point
import android.view.View
import androidx.lifecycle.Lifecycle


/**
 * calculates center of the view
 */
fun View.center() = Point((width / 2), (height / 2))

/**
 * calculates center of the view in parent
 */
fun View.centerInParent() = Point((left + width / 2), (top + height / 2))

/**
 * Complete shorthand for Animators
 */
fun Animator.animateWithEndCallback(lifecycle: Lifecycle, onAnimationEnd: () -> Unit) {
    addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                onAnimationEnd()
            }
        }
    })

    start()
}