package com.example.torwitharti.utils

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.example.torwitharti.R
import kotlin.math.log


class GlowDrawable @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    var lgA: LinearGradient? = null
    var lgB: LinearGradient? = null
    var shader: ComposeShader? = null


    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        // Paint styles used for rendering are initialized here. This
        // is a performance optimization, since onDraw() is called
        // for every screen refresh.
        //style = Paint.Style.FILL
        isFilterBitmap = true
    }


    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null);

    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d("GlowDrawable", "onSizeChanged: w:$w h:$h")
        val lg =
            LinearGradient(
                0f, 0f, 0f, h/2f,
                Color.YELLOW,
                Color.BLUE,
                Shader.TileMode.CLAMP
            );
        val sat =
            LinearGradient(0f, h/2.toFloat(), 0f, h.toFloat(), Color.RED, Color.RED, Shader.TileMode.CLAMP);
        shader = ComposeShader(lg, sat, PorterDuff.Mode.MULTIPLY)

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        Log.d("GlowDrawable", "onDraw:")
        paint.shader = shader;
        canvas?.drawPaint(paint)
        //canvas?.drawColor(Color.WHITE);
    }
}
