package com.example.torwitharti.ui.approuting.data

import android.Manifest.permission.INTERNET
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import androidx.annotation.WorkerThread
import com.example.torwitharti.R
import com.example.torwitharti.ui.approuting.data.AppListAdapter.Companion.CELL
import com.example.torwitharti.ui.approuting.data.AppListAdapter.Companion.HORIZONTAL_RECYCLER_VIEW
import com.example.torwitharti.ui.approuting.data.AppListAdapter.Companion.SECTION_HEADER_VIEW
import com.example.torwitharti.ui.approuting.model.AppItemModel
import com.example.torwitharti.utils.PreferenceHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class AppManager(context: Context) {
    private val context: Context
    val preferenceHelper: PreferenceHelper
    private val pm: PackageManager
    private val torPoweredAppPackageNames: List<String> = listOf(
        // orbot
        "org.torproject.android",
        "org.torproject.android.nightly",
        // tor-browser
        "org.torproject.torbrowser",
        "org.torproject.torbrowser_alpha",
        "org.torproject.torbrowser_nightly",
        "org.torproject.torbrowser_debug",
        //onionshare
        "org.onionshare.android",
        "org.onionshare.android.nightly"
    )

    companion object {
        val TAG = AppManager::class.java.simpleName
    }
    init {
        this.context = context
        this.preferenceHelper = PreferenceHelper(context)
        this.pm = context.packageManager
    }


    @WorkerThread
    fun queryInstalledApps() : List<AppItemModel> {
        val installedPackages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val installedBrowserPackageNames = getInstalledBrowserPackages()
        val installedBrowsersApps = mutableListOf<AppItemModel>()
        val installedTorApps = mutableListOf<AppItemModel>()
        val protectedApps = preferenceHelper.protectedApps?.toSet()
        val protectAllApps = preferenceHelper.protectAllApps
        var androidSystemUid = 0
        val installedOtherApps = mutableListOf<AppItemModel>()
        try {
            val system = pm.getApplicationInfo("android", PackageManager.GET_META_DATA)
            androidSystemUid = system.uid
            createAppItemModel(system, protectedApps = protectedApps, protectAllApps = protectAllApps)?.also {
                installedOtherApps.add(it)
            }
        } catch (e: PackageManager.NameNotFoundException) {
        }

        for (appInfo in installedPackages) {
            // only add apps which are allowed to use internet
            if (pm.checkPermission(INTERNET, appInfo.packageName) == PERMISSION_GRANTED &&
                appInfo.uid != androidSystemUid) {
                createAppItemModel(appInfo, installedBrowserPackageNames, torPoweredAppPackageNames, protectedApps, protectAllApps)?.also {
                    if (it.hasTorSupport == true) {
                        installedTorApps.add(it)
                    } else if (it.isBrowserApp == true) {
                        installedBrowsersApps.add(it)
                    } else {
                        installedOtherApps.add(it)
                    }
                }
            }
        }

        val sortedTorApps = installedTorApps.sorted()
        val sortedBrowsers = installedBrowsersApps.sorted()
        val sortedOtherApps = installedOtherApps.sorted()
        val resultList = mutableListOf<AppItemModel>()
        if (sortedTorApps.isNotEmpty()) {
            resultList.add(AppItemModel(SECTION_HEADER_VIEW, context.getString(R.string.app_routing_tor_apps)))
            resultList.add(AppItemModel(HORIZONTAL_RECYCLER_VIEW, appList = sortedTorApps))
        }
        if (sortedBrowsers.isNotEmpty()) {
            resultList.add(AppItemModel(SECTION_HEADER_VIEW, context.getString(R.string.app_routing_browsers)))
            resultList.addAll(sortedBrowsers)
            resultList.add(AppItemModel(SECTION_HEADER_VIEW, context.getString(R.string.app_routing_other_apps)))
        }
        resultList.addAll(sortedOtherApps)
        preferenceHelper.cachedApps = Gson().toJson(resultList)
        return resultList
    }

    private fun createAppItemModel(
        applicationInfo: ApplicationInfo,
        browserPackages: List<String> = listOf(),
        torPackages: List<String> = listOf(),
        protectedApps: Set<String>? = setOf(),
        protectAllApps: Boolean
    ): AppItemModel? {
        var appName = applicationInfo.loadLabel(pm) as? String
        if (TextUtils.isEmpty(appName))
            appName = applicationInfo.packageName

        appName?.also {
            val appUID = applicationInfo.uid
            val packageName = applicationInfo.packageName
            return AppItemModel(
                CELL,
                it,
                packageName,
                appUID,
                protectedApps?.contains(packageName) ?:run { false },
                protectAllApps,
                browserPackages.contains(packageName),
                torPackages.contains(packageName))
        }

        return null
    }

    private fun  getInstalledBrowserPackages() :  List<String> {
            val queryBrowsersIntent = Intent()
                .setAction(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .setData(Uri.parse("https://"))

            val installedBrowsers = mutableListOf<ResolveInfo>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                installedBrowsers.addAll(
                    pm.queryIntentActivities(
                        queryBrowsersIntent,
                        PackageManager.MATCH_ALL
                    )
                )
            } else {
                installedBrowsers.addAll(pm.queryIntentActivities(queryBrowsersIntent, PackageManager.MATCH_DEFAULT_ONLY))
            }

            val browserPackageNames = mutableListOf<String>()
            for (browser in installedBrowsers) {
                browserPackageNames.add(browser.activityInfo.packageName)
            }
            return browserPackageNames
        }

    fun loadCachedApps(): List<AppItemModel> {
        val gson = Gson()
        val appItemModelListType: Type = object : TypeToken<ArrayList<AppItemModel?>?>() {}.type
        return gson.fromJson(preferenceHelper.cachedApps, appItemModelListType)
    }
}