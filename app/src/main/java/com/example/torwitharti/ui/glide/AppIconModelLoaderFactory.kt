package com.example.torwitharti.ui.glide

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory


class AppIconModelLoaderFactory internal constructor(context: Context) :
    ModelLoaderFactory<ApplicationInfoModel, Bitmap> {
    private val mContext: Context

    init {
        mContext = context
    }

    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<ApplicationInfoModel, Bitmap> {
        return AppIconModelLoader(mContext)
    }

    override fun teardown() {
        // Empty Implementation.
    }
}