package com.example.torwitharti.ui.glide

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher


class AppIconDataFetcher internal constructor(
    context: Context,
    private val mModel: ApplicationInfoModel
) :
    DataFetcher<Bitmap> {
    private val mContext: Context

    init {
        mContext = context
    }

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        val applicationInfo = mContext.packageManager.getApplicationInfo(mModel.toString(), 0)
        val icon: Drawable = mContext.packageManager.getApplicationIcon(applicationInfo)
        val bitmap = drawableToBitmap(icon)
        callback.onDataReady(bitmap)
    }

    override fun cleanup() {
        // Empty Implementation
    }

    override fun cancel() {
        // Empty Implementation
    }

    override fun getDataClass(): Class<Bitmap> {
        return Bitmap::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.LOCAL
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable) {
            drawable.bitmap?.also {
                return it
            }
        }
        val bitmap: Bitmap? = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(
                80,
                80,
                Bitmap.Config.ARGB_8888
            )
        } else {
            Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }

        bitmap?.also {
            val canvas = Canvas(it)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        }
        return bitmap
    }
}