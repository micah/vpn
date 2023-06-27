package org.torproject.vpn.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ComposeShader
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import org.torproject.vpn.R


class GradientGlowView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //todo view size adjustments for uppper and lower glow
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // TODO Composite shader logic that applies transparency shade to existing bitmap.
        //TODO create 2 bitmaps, center line and glow. Apply composite shader to the glow, make a copy and flip it.

        // Load the bitmap from drawable
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.bg_connect_green)

        // Create a BitmapShader
        val bitmapShader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

        // Create a LinearGradient. This will be used to create the fade effect
        val bitmapHeight = bitmap.height.toFloat()
        val gradientShader = LinearGradient( 0f, bitmapHeight,0f, 0f,
            intArrayOf(Color.TRANSPARENT, Color.BLACK),
            floatArrayOf(0f, 1f), Shader.TileMode.CLAMP)

        // Combine the two shaders
        val combinedShader = ComposeShader(bitmapShader, gradientShader, PorterDuff.Mode.DST_IN)

        // Create the Paint and set the ComposeShader
        val paint = Paint()
        paint.shader = combinedShader

        // Draw the bitmap with the shader applied
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
    }

}