package org.torproject.vpn.utils

import android.animation.*
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Looper
import android.util.Log
import android.view.animation.AccelerateInterpolator
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import org.torproject.vpn.R
import org.torproject.vpn.vpn.DataUsage
import java.io.BufferedReader
import java.text.SimpleDateFormat
import java.util.*


fun getTextColorAnimation(
    textView: TextView,
    startColorRes: Int,
    endColorRes: Int,
    duration: Int
): ObjectAnimator {
    val startColor = ContextCompat.getColor(textView.context, startColorRes)
    val endColor = ContextCompat.getColor(textView.context, endColorRes)
    // Log.d("ANIMATE", "setUI animate colors ${String.format("#%08X", 0xFFFFFFFF and startColor.toLong())} -> ${String.format("#%08X", 0xFFFFFFFF and endColor.toLong())}")
    val animator: ObjectAnimator = ObjectAnimator.ofArgb(textView, "textColor", startColor, endColor)
    animator.duration = (duration.toLong() - 50).coerceAtLeast(50)
    return animator
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

fun isRunningOnMainThread(): Boolean {
    return Looper.getMainLooper().thread === Thread.currentThread()
}

fun getFormattedDate(timestamp: Long, locale: Locale?): String? {
    val sdf = SimpleDateFormat("dd/mm/yy, hh:mm:ss.SSS", locale)
    return sdf.format(timestamp)
}

fun updateDataUsage(dataUsage: LiveData<DataUsage>, downstream: Long, upstream: Long): DataUsage{
    val lastDataUsage: DataUsage = dataUsage.value!!
    val updatedDataUsage = DataUsage()
    updatedDataUsage.downstreamData = downstream
    updatedDataUsage.upstreamData = upstream
    val timeDelta = Math.max((updatedDataUsage.timeStamp - lastDataUsage.timeStamp) / 1000, 1)
    updatedDataUsage.upstreamDataPerSec =
        (updatedDataUsage.upstreamData - lastDataUsage.upstreamData) / timeDelta
    updatedDataUsage.downstreamDataPerSec =
        (updatedDataUsage.downstreamData - lastDataUsage.downstreamData) / timeDelta
    return updatedDataUsage
}

fun getDpInPx(context: Context, dp: Float): Int {
    val scale: Float = context.resources.displayMetrics.density
    return (dp * scale + 0.5f).toInt()
}

fun getFlagByCountryCode(context: Context, countryCode: String?): Drawable? {
    if (countryCode.isNullOrEmpty()) {
        return null
    }
    if (countryCode.length != 2) {
        Log.w("FLAG UTIL", "$countryCode is an invalid country code")
        return null
    }

    val countryCodeCaps = countryCode.uppercase()
    val firstLetter = Character.codePointAt(countryCodeCaps, 0) - 0x41 + 0x1F1E6
    val secondLetter = Character.codePointAt(countryCodeCaps, 1) - 0x41 + 0x1F1E6

    if (!countryCodeCaps[0].isLetter() || !countryCodeCaps[1].isLetter()) {
        return null
    }

    val drawableName =  "flag_${firstLetter.toString(16)}_${secondLetter.toString(16)}"
    val resID: Int = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
    return try {
        ContextCompat.getDrawable(context, resID)
    } catch (e: Resources.NotFoundException) {
        null
    }
}

fun getCountryByCode(context: Context, code: String?): String {
    if (code != null) {
        return  Locale("", code).displayCountry
    }
    return context.getString(R.string.unknown)
}

fun readAsset(context: Context, fileName: String): String {
    return context
        .assets
        .open(fileName)
        .bufferedReader()
        .use(BufferedReader::readText)
}
