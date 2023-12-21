package org.torproject.vpn.ui.appearancesettings.model

import android.content.Context
import org.torproject.vpn.R
import org.torproject.vpn.utils.PreferenceHelper

object WallpaperSet {

    fun getWallpaperList(context: Context, preferenceHelper: PreferenceHelper): List<WallpaperModel> {
        val selectedResource = preferenceHelper.wallpaperResource ?:run { context.resources.getResourceName(R.drawable.bg_stars) }
        return listOf(
            WallpaperModel.create(context, R.drawable.bg_stars, R.string.wallpaper_1, selectedResource),
            WallpaperModel.create(context, R.color.blue20, R.string.wallpaper_2, selectedResource),
            WallpaperModel.create(context, R.color.yellowNormal, R.string.wallpaper_3, selectedResource),
            WallpaperModel.create(context, R.color.emerald40, R.string.wallpaper_4, selectedResource)
        )
    }

    fun getWallpaperResource(context: Context, preferenceHelper: PreferenceHelper): Int {
        val selectedResource = preferenceHelper.wallpaperResource ?:run { context.resources.getResourceName(R.drawable.bg_stars) }
        return context.resources.getIdentifier(selectedResource, null, context.packageName)
    }

}