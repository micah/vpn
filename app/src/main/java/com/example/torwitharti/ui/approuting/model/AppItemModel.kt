package com.example.torwitharti.ui.approuting.model

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder

class AppItemModel (viewType: Int,
                    text: String,
                    appId: String?,
                    uid: Int?,
                    isRoutingEnabled: Boolean?,
                    protectAllApps: Boolean?,
                    isBrowserApp: Boolean?,
                    hasTorSocksSupport: Boolean?,
                    appList: List<AppItemModel>?) : Comparable<AppItemModel> {
    constructor(viewType: Int,
                text: String,
                appId: String?,
                uid: Int?,
                isRoutingEnabled: Boolean?,
                protectAllApps: Boolean?,
                isBrowserApp: Boolean?,
                hasTorSocksSupport: Boolean?) : this(viewType, text, appId, uid, isRoutingEnabled, protectAllApps, isBrowserApp, hasTorSocksSupport, null)
    constructor(viewType: Int,
                text: String) : this(viewType, text, null, null, null, null, null, null, null)
    constructor(viewType: Int, appList: List<AppItemModel>?) : this(viewType, "", null, null, /*null,*/ null, null, null, null, appList)
    constructor(viewType: Int) : this(viewType, "", null, null, null, null, null, null, null)

    val viewType: Int
    val text: String
    val appId: String?
    val uid: Int?
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
        this.isRoutingEnabled = isRoutingEnabled
        this.protectAllApps = protectAllApps
        this.isBrowserApp = isBrowserApp
        this.hasTorSupport = hasTorSocksSupport
        this.appList = appList
    }

    companion object {
        fun fromJson(json: String): AppItemModel {
            val builder = GsonBuilder()
            return builder.create().fromJson(json, AppItemModel::class.java)
        }
    }

    override fun compareTo(other: AppItemModel): Int {
        return compareValuesBy(this, other, { it.isRoutingEnabled == false }, { it.text })
    }

    override fun toString(): String {
        val json = Gson().toJson(this)
        return json
    }

}