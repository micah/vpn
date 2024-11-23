package org.torproject.vpn.ui.approuting.data

import android.Manifest.permission.INTERNET
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import androidx.annotation.WorkerThread
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.torproject.vpn.BuildConfig
import org.torproject.vpn.R
import org.torproject.vpn.ui.approuting.data.AppListAdapter.Companion.CELL
import org.torproject.vpn.ui.approuting.data.AppListAdapter.Companion.HORIZONTAL_RECYCLER_VIEW
import org.torproject.vpn.ui.approuting.data.AppListAdapter.Companion.SECTION_HEADER_VIEW
import org.torproject.vpn.ui.approuting.data.AppListAdapter.Companion.SHOW_APPS_VIEW
import org.torproject.vpn.ui.approuting.model.AppItemModel
import org.torproject.vpn.utils.PreferenceHelper
import java.lang.reflect.Type


class AppManager(context: Context) {
    private val context: Context
    val preferenceHelper: PreferenceHelper
    private val pm: PackageManager


    companion object {
        val TAG = AppManager::class.java.simpleName
        val TOR_POWERED_APP_PACKAGE_NAMES: List<String> = listOf(
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
    }
    init {
        this.context = context
        this.preferenceHelper = PreferenceHelper(context)
        this.pm = context.packageManager
    }

    /**
     * Checks if the app is system app or not.
     */
    private fun isSystemApp(packageInfo: PackageInfo): Boolean {
        val flags = packageInfo.applicationInfo.flags
        return (flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                (flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
    }

    @WorkerThread
    fun queryInstalledApps() : List<AppItemModel> {
        val installedPackages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS).filter {
            it.requestedPermissions?.contains(INTERNET) == true
        }.map { it.applicationInfo }
        val installedBrowserPackageNames = getInstalledBrowserPackages()
        val installedBrowsersApps = mutableListOf<AppItemModel>()
        val installedTorApps = mutableListOf<AppItemModel>()
        val protectedApps = preferenceHelper.protectedApps?.toSet()
        val protectAllApps = preferenceHelper.protectAllApps
        var androidSystemUid = 0
        val installedOtherApps = mutableListOf<AppItemModel>()
        val systemApps = mutableListOf<AppItemModel>()
        try {
            val system = pm.getApplicationInfo("android", PackageManager.GET_META_DATA)
            androidSystemUid = system.uid
            createAppItemModel(system, protectedApps = protectedApps, protectAllApps = protectAllApps)?.also {
                systemApps.add(it)
            }
        } catch (e: PackageManager.NameNotFoundException) {
        }

        for (appInfo in installedPackages) {
            // only add apps which are allowed to use internet
            if (appInfo.uid != androidSystemUid &&
                appInfo.packageName != BuildConfig.APPLICATION_ID) {
                createAppItemModel(appInfo, installedBrowserPackageNames, TOR_POWERED_APP_PACKAGE_NAMES, protectedApps, protectAllApps)?.also {
                    if (it.hasTorSupport == true) {
                        installedTorApps.add(it)
                    } else if (it.isBrowserApp == true) {
                        installedBrowsersApps.add(it)
                    } else if(isSystemApp(pm.getPackageInfo(appInfo.packageName, 0))) {
                        systemApps.add(it)
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

        resultList.add(AppItemModel(SECTION_HEADER_VIEW, context.getString(R.string.app_routing_system_apps)))
        resultList.add(AppItemModel(SHOW_APPS_VIEW, systemApps.size.toString()))
        resultList.addAll(systemApps.sorted())

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
                (protectedApps?.contains(packageName) ?:run { false }) || protectAllApps,
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
        gson.fromJson<List<AppItemModel>>(preferenceHelper.cachedApps, appItemModelListType)?.let { list ->
            return sortCachedApps(list)
        } ?: run {
            return emptyList()
        }
    }

    private fun sortCachedApps(list: List<AppItemModel>): List<AppItemModel> {
        val installedBrowsersApps = mutableListOf<AppItemModel>()
        var installedTorApps: AppItemModel? = null
        val installedOtherApps = mutableListOf<AppItemModel>()
        for (model in list) {
            when (model.viewType) {
                CELL -> {
                    if (model.isBrowserApp == true) {
                        installedBrowsersApps.add(model)
                    } else {
                        installedOtherApps.add(model)
                    }
                }
                HORIZONTAL_RECYCLER_VIEW -> {
                    installedTorApps = model
                }
                else -> {}
            }
        }
        val sortedBrowsers = installedBrowsersApps.sorted()
        val sortedOtherApps = installedOtherApps.sorted()
        val resultList = mutableListOf<AppItemModel>()
        if (installedTorApps != null) {
            resultList.add(AppItemModel(SECTION_HEADER_VIEW, context.getString(R.string.app_routing_tor_apps)))
            resultList.add(installedTorApps)
        }
        if (sortedBrowsers.isNotEmpty()) {
            resultList.add(AppItemModel(SECTION_HEADER_VIEW, context.getString(R.string.app_routing_browsers)))
            resultList.addAll(sortedBrowsers)
            resultList.add(AppItemModel(SECTION_HEADER_VIEW, context.getString(R.string.app_routing_other_apps)))
        }
        resultList.addAll(sortedOtherApps)
        return resultList
    }
}