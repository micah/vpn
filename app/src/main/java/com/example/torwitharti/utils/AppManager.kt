package com.example.torwitharti.utils

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.example.torwitharti.R
import com.example.torwitharti.ui.settings.AppItemModel

class AppManager(context: Context) {
    private val context: Context
    init {
        this.context = context
    }
    fun queryInstalledApps() : List<AppItemModel>  {
        // TODO: implement me!
        val arrayList = mutableListOf<AppItemModel>().apply {


            val drawable = ContextCompat.getDrawable(context, R.drawable.ic_dummy_app)
            add(AppItemModel("Bitmask", "se.leap.bitmaskclient", drawable, false, false, false))
            add(AppItemModel("RiseupVPN", "se.leap.riseupvpn", drawable, false, false, false))
            add(AppItemModel("F-Droid", "org.fdroid.fdroid", drawable, false, false, true))
        }
        return arrayList
    }
}