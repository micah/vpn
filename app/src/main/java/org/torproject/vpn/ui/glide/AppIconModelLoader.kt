package org.torproject.vpn.ui.glide

import android.content.Context
import android.graphics.drawable.Drawable
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.signature.ObjectKey


internal class AppIconModelLoader(context: Context) :
    ModelLoader<ApplicationInfoModel, Drawable> {
    private val mContext: Context

    init {
        mContext = context
    }

    override fun buildLoadData(
        applicationInfo: ApplicationInfoModel,
        width: Int,
        height: Int,
        options: Options
    ): LoadData<Drawable> {
        return LoadData(
            ObjectKey(applicationInfo),
            AppIconDataFetcher(mContext, applicationInfo)
        )
    }

    override fun handles(applicationInfo: ApplicationInfoModel): Boolean {
        return true
    }
}