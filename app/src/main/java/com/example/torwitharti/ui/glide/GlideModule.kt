package com.example.torwitharti.ui.glide

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule


@GlideModule
class GlideModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(
            ApplicationInfoModel::class.java,
            Bitmap::class.java,
            AppIconModelLoaderFactory(context)
        )
    }
}