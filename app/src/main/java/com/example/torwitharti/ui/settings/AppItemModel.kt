package com.example.torwitharti.ui.settings

import android.graphics.drawable.Drawable

class AppItemModel (name: String, appId: String, isRoutingEnabled: Boolean, isBrowserApp: Boolean, hasTorSocksSupport: Boolean) {
    var name: String
    var appId: String
    var isRoutingEnabled: Boolean
    var isBrowserApp: Boolean
    var hasTorSocksSupport: Boolean
    lateinit var drawable: Drawable

    init {
        this.name = name
        this.appId = appId
        this.isRoutingEnabled = isRoutingEnabled
        this.isBrowserApp = isBrowserApp
        this.hasTorSocksSupport = hasTorSocksSupport
    }
}