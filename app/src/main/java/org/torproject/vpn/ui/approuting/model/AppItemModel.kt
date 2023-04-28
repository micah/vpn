package org.torproject.vpn.ui.approuting.model

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

    // protectAllApps is excluded on purpose, so that List<AppItemModel> comparisons for
    // equality return true, even if protectAllApps changed
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AppItemModel) return false

        if (viewType != other.viewType) return false
        if (text != other.text) return false
        if (appId != other.appId) return false
        if (uid != other.uid) return false
        if (isRoutingEnabled != other.isRoutingEnabled) return false
        if (isBrowserApp != other.isBrowserApp) return false
        if (hasTorSupport != other.hasTorSupport) return false
        if (appList != other.appList) return false

        return true
    }

    // protectAllApps is excluded on purpose
    override fun hashCode(): Int {
        var result = viewType
        result = 31 * result + text.hashCode()
        result = 31 * result + (appId?.hashCode() ?: 0)
        result = 31 * result + (uid ?: 0)
        result = 31 * result + (isRoutingEnabled?.hashCode() ?: 0)
        result = 31 * result + (isBrowserApp?.hashCode() ?: 0)
        result = 31 * result + (hasTorSupport?.hashCode() ?: 0)
        result = 31 * result + (appList?.hashCode() ?: 0)
        return result
    }

}