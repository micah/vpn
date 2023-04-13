package com.example.torwitharti.ui.approuting.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder

data class AppItemModel (
    val viewType: Int,
    val text: String,
    val appId: String?,
    val uid: Int?,
    var isRoutingEnabled: Boolean?,
    var protectAllApps: Boolean?,
    val isBrowserApp: Boolean?,
    val hasTorSupport: Boolean?,
    val appList: List<AppItemModel>?) : Comparable<AppItemModel> {
    constructor(viewType: Int,
                text: String,
                appId: String?,
                uid: Int?,
                isRoutingEnabled: Boolean?,
                protectAllApps: Boolean?,
                isBrowserApp: Boolean?,
                hasTorSupport: Boolean?) : this(viewType, text, appId, uid, isRoutingEnabled, protectAllApps, isBrowserApp, hasTorSupport, null)
    constructor(viewType: Int,
                text: String) : this(viewType, text, null, null, null, null, null, null, null)
    constructor(viewType: Int, appList: List<AppItemModel>?) : this(viewType, "", null, null, /*null,*/ null, null, null, null, appList)
    constructor(viewType: Int) : this(viewType, "", null, null, null, null, null, null, null)

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