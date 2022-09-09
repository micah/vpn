package com.example.torwitharti.ui.settings

import android.graphics.drawable.Drawable

class AppItemModel (viewType: Int,
                    text: String,
                    appId: String?,
                    uid: Int?,
                    icon: Drawable?,
                    isRoutingEnabled: Boolean?,
                    protectAllApps: Boolean?,
                    isBrowserApp: Boolean?,
                    hasTorSocksSupport: Boolean?,
                    appList: List<AppItemModel>?) : Comparable<AppItemModel> {
    constructor(viewType: Int,
                text: String,
                appId: String?,
                uid: Int?,
                icon: Drawable?,
                isRoutingEnabled: Boolean?,
                protectAllApps: Boolean?,
                isBrowserApp: Boolean?,
                hasTorSocksSupport: Boolean?) : this(viewType, text, appId, uid, icon, isRoutingEnabled, protectAllApps, isBrowserApp, hasTorSocksSupport, null)
    constructor(viewType: Int,
                text: String) : this(viewType, text, null, null, null, null, null, null, null, null)
    constructor(viewType: Int, appList: List<AppItemModel>?) : this(viewType, "", null, null, null, null, null, null, null, appList)
    constructor(viewType: Int) : this(viewType, "", null, null, null, null, null, null, null, null)

    val viewType: Int
    val text: String
    val appId: String?
    val uid: Int?
    val icon: Drawable?
    var isRoutingEnabled: Boolean?
    var protectAllApps: Boolean?
    val isBrowserApp: Boolean?
    val hasTorSupport: Boolean?
    val appList: List<AppItemModel>?

    init {
        this.viewType = viewType
        this.text = text
        this.appId = appId
        this.uid = uid
        this.icon = icon
        this.isRoutingEnabled = isRoutingEnabled
        this.protectAllApps = protectAllApps
        this.isBrowserApp = isBrowserApp
        this.hasTorSupport = hasTorSocksSupport
        this.appList = appList
    }

    override fun compareTo(other: AppItemModel): Int {
        return compareValuesBy(this, other, { it.isRoutingEnabled }, { it.text })
    }
}