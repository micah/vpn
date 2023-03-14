package com.example.torwitharti.ui.glide

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.signature.ObjectKey


internal class AppIconModelLoader(context: Context) :
    ModelLoader<ApplicationInfoModel, Bitmap> {
    private val mContext: Context

    init {
        mContext = context
    }

    override fun buildLoadData(
        applicationInfo: ApplicationInfoModel,
        width: Int,
        height: Int,
        options: Options
    ): LoadData<Bitmap> {
        return LoadData(
            ObjectKey(applicationInfo),
            AppIconDataFetcher(mContext, applicationInfo)
        )
    }

    override fun handles(applicationInfo: ApplicationInfoModel): Boolean {
        return true
    }
}