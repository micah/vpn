package org.torproject.vpn.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Point
import android.graphics.Rect
import android.view.View
import androidx.annotation.FloatRange
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
        override fun onAnimationEnd(animation: Animator) {
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                onAnimationEnd()
            }
        }
    })

    start()
}

/**
 * scale for outline
 * source : https://github.com/rock3r/uplift
 */
fun Rect.scale(
    @FloatRange(from = -1.0, to = 1.0) scaleX: Float,
    @FloatRange(from = -1.0, to = 1.0) scaleY: Float
) {
    val newWidth = width() * scaleX
    val newHeight = height() * scaleY
    val deltaX = (width() - newWidth) / 2
    val deltaY = (height() - newHeight) / 2

    set(
        (left + deltaX).toInt(),
        (top + deltaY).toInt(),
        (right - deltaX).toInt(),
        (bottom - deltaY).toInt()
    )
}
