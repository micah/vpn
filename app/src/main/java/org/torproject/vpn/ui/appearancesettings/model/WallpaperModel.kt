package org.torproject.vpn.ui.appearancesettings.model

import android.content.Context

data class WallpaperModel(
    val drawableResName: String,
    val drawableResId: Int,
    val descriptionResName: Int,
    var selected: Boolean) {
    companion object {
        fun create(context: Context, drawableResId: Int, descriptionResName: Int, selectedResource: String): WallpaperModel {
            val resourceName = context.resources.getResourceName(drawableResId)
            return WallpaperModel(resourceName, drawableResId, descriptionResName, selectedResource == resourceName)
        }
    }
}
