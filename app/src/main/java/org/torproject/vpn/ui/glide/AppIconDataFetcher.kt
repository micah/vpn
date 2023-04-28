package org.torproject.vpn.ui.glide

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.bumptech.glide.Priority
import com.bumptech.glide.Registry
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import org.torproject.vpn.R


class AppIconDataFetcher internal constructor(
    context: Context,
    private val mModel: ApplicationInfoModel
) :
    DataFetcher<Drawable> {
    private val mContext: Context

    init {
        mContext = context
    }

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Drawable>) {
        try {
            val applicationInfo = mContext.packageManager.getApplicationInfo(mModel.toString(), 0)
            val icon: Drawable = mContext.packageManager.getApplicationIcon(applicationInfo)
            callback.onDataReady(icon)
        } catch (e: Registry.NoResultEncoderAvailableException) {
            e.printStackTrace()
            callback.onDataReady(AppCompatResources.getDrawable(mContext, R.drawable.ic_dummy_app))
        }
    }

    override fun cleanup() {
        // Empty Implementation
    }

    override fun cancel() {
        // Empty Implementation
    }

    override fun getDataClass(): Class<Drawable> {
        return Drawable::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.LOCAL
    }
}