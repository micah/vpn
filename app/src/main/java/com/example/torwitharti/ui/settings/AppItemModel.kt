package com.example.torwitharti.ui.settings

import android.graphics.drawable.Drawable

class AppItemModel (name: String, appId: String, uid: Int, icon: Drawable?, isRoutingEnabled: Boolean, isBrowserApp: Boolean, hasTorSocksSupport: Boolean) {
    val name: String
    val appId: String
    val uid: Int
    val icon: Drawable?
    val isRoutingEnabled: Boolean
    val isBrowserApp: Boolean
    val hasTorSocksSupport: Boolean

    init {
        this.name = name
        this.appId = appId
        this.uid = uid
        this.icon = icon
        this.isRoutingEnabled = isRoutingEnabled
        this.isBrowserApp = isBrowserApp
        this.hasTorSocksSupport = hasTorSocksSupport
    }
}