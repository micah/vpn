package org.torproject.vpn.ui.approuting.data

import android.Manifest.permission.INTERNET
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.text.TextUtils
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.core.net.toUri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.torproject.vpn.BuildConfig
import org.torproject.vpn.R
import org.torproject.vpn.ui.approuting.data.AppListAdapter.Companion.CELL
import org.torproject.vpn.ui.approuting.data.AppListAdapter.Companion.HORIZONTAL_RECYCLER_VIEW
import org.torproject.vpn.ui.approuting.data.AppListAdapter.Companion.SECTION_HEADER_VIEW
import org.torproject.vpn.ui.approuting.data.AppListAdapter.Companion.SHOW_APPS_VIEW
import org.torproject.vpn.ui.approuting.model.AppItemModel
import org.torproject.vpn.utils.PreferenceHelper
import java.lang.Exception
import java.lang.reflect.Type


class AppManager(private val context: Context) {
    val preferenceHelper: PreferenceHelper = PreferenceHelper(context)
    private val pm: PackageManager = context.packageManager


    companion object {
        val TAG = AppManager::class.java.simpleName
        val TOR_POWERED_APP_PACKAGE_NAMES: List<String> = listOf(
            // cwtch
            "im.cwtch.flwtch",
            // briar
            "org.briarproject.briar.android",
            "org.briarproject.mailbox",
            // onionshare
            "org.onionshare.android",
            "org.onionshare.android.fdroid",
            "org.onionshare.android.nightly",
            // orbot
            "org.torproject.android",
            "org.torproject.android.nightly",
            // tor-browser
            "org.torproject.torbrowser",
            "org.torproject.torbrowser_alpha",
            "org.torproject.torbrowser_debug",
            "org.torproject.torbrowser_nightly",
            // tor services
            "org.torproject.torservices"
        )
    }

    /**
     * Checks if the app is system app or not.
     */
    private fun isSystemApp(packageInfo: PackageInfo): Boolean {
        val flags = packageInfo.applicationInfo?.flags
        return flags != null && ((flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                (flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0)

    }

    @WorkerThread
    fun checkNewInstalledApps() {
        val installedPackages = getInstalledPackages()
        val cachedPackageNames = loadCachedApps()
            .filter { appItemModel -> appItemModel.viewType == CELL }
            .mapNotNull { appItemModel -> appItemModel.appId }
            .toSet()
        val installedPackageNames = installedPackages
            .mapNotNull { applicationInfo -> applicationInfo?.packageName }
            .toSet()
        if (cachedPackageNames == installedPackageNames) {
            return
        }
        val newRemovedPackageNames = cachedPackageNames.minus(installedPackageNames)
        val newInstalledPackageNames = installedPackageNames
            .minus(cachedPackageNames)
            .minus(TOR_POWERED_APP_PACKAGE_NAMES.toSet())
        val protectedApps = preferenceHelper.protectedApps

        if (newInstalledPackageNames.isNotEmpty()) {
            protectedApps.addAll(newInstalledPackageNames.toSet())
        }
        if (newRemovedPackageNames.isNotEmpty()) {
            protectedApps.removeAll(newRemovedPackageNames.toSet())
        }

        queryInstalledApps(protectedApps)
    }

    @WorkerThread
    @Synchronized
    fun queryInstalledApps(protectedApps: MutableSet<String>) : List<AppItemModel> {
        val installedPackages = getInstalledPackages()
        val installedBrowserPackageNames = getInstalledBrowserPackages()
        val installedBrowsersApps = mutableListOf<AppItemModel>()
        val installedTorApps = mutableListOf<AppItemModel>()
        val protectAllApps = preferenceHelper.protectAllApps
        val installedOtherApps = mutableListOf<AppItemModel>()
        val systemApps = mutableListOf<AppItemModel>()

        try {
            val system = pm.getApplicationInfo("android", PackageManager.GET_META_DATA)
            createAppItemModel(system, protectedApps = protectedApps, protectAllApps = protectAllApps)?.also {
                systemApps.add(it)
            }
        } catch (e: PackageManager.NameNotFoundException) {
        }

        for (appInfo in installedPackages) {
            if (appInfo != null &&
                appInfo.packageName != BuildConfig.APPLICATION_ID) {
                try {
                    createAppItemModel(appInfo, installedBrowserPackageNames, TOR_POWERED_APP_PACKAGE_NAMES, protectedApps, protectAllApps)?.also {
                        if (it.hasTorSupport == true) {
                            installedTorApps.add(it)
                        } else if (it.isBrowserApp == true) {
                            installedBrowsersApps.add(it)
                        } else if (isSystemApp(pm.getPackageInfo(appInfo.packageName, 0))) {
                            systemApps.add(it)
                        } else {
                            installedOtherApps.add(it)
                        }
                    }
                } catch (e: PackageManager.NameNotFoundException){
                    Log.e(TAG, "app not found ${appInfo.packageName}",e)
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

        preferenceHelper.protectAllApps = (sortedBrowsers.size + sortedOtherApps.size + systemApps.size) == protectedApps.size
        preferenceHelper.cachedApps = Gson().toJson(resultList)
        preferenceHelper.protectedApps = protectedApps

        return resultList
    }

    @WorkerThread
    fun queryInstalledApps() : List<AppItemModel> {
        val protectedApps = preferenceHelper.protectedApps
        return queryInstalledApps(protectedApps)
    }

    private fun getInstalledPackages(): List<ApplicationInfo?> =
        // only add apps which are allowed to use internet
        pm.getInstalledPackages(PackageManager.GET_PERMISSIONS).filter {
            it.requestedPermissions?.contains(INTERNET) == true
        }.map { it.applicationInfo }

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
        } ?: run {
            Log.d(TAG, "no app name found for ${applicationInfo.packageName}")
        }

        return null
    }

    private fun  getInstalledBrowserPackages() :  List<String> {
        val queryBrowsersIntent = Intent()
            .setAction(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setData("https://".toUri())

        val installedBrowsers = mutableListOf<ResolveInfo>()
        installedBrowsers.addAll(
            pm.queryIntentActivities(
                queryBrowsersIntent,
                PackageManager.MATCH_ALL
            )
        )

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
        val systemApps = mutableListOf<AppItemModel>()
        var inSystemSection = false
        var systemAppsCount: String? = null

        for (model in list) {
            when (model.viewType) {
                HORIZONTAL_RECYCLER_VIEW -> installedTorApps = model
                SHOW_APPS_VIEW -> {
                    inSystemSection = true
                    systemAppsCount = model.text
                }
                CELL -> {
                    if (inSystemSection) {
                        systemApps.add(model)
                    } else if (model.isBrowserApp == true) {
                        installedBrowsersApps.add(model)
                    } else {
                        installedOtherApps.add(model)
                    }
                }
                else -> { /* skip headers */ }
            }
        }

        val sortedBrowsers = installedBrowsersApps.sorted()
        val sortedOtherApps = installedOtherApps.sorted()
        val sortedSystemApps = systemApps.sorted()
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
        resultList.add(AppItemModel(SECTION_HEADER_VIEW, context.getString(R.string.app_routing_system_apps)))
        resultList.add(AppItemModel(SHOW_APPS_VIEW, systemAppsCount ?: sortedSystemApps.size.toString()))
        resultList.addAll(sortedSystemApps)

        return resultList
    }

    fun onAppIdChanged(added: Boolean, packageName: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            packageName?.let { appId ->
                val protectedAppsList = preferenceHelper.protectedApps
                if (added && !protectedAppsList.contains(appId) && !TOR_POWERED_APP_PACKAGE_NAMES.contains(appId)) {
                    protectedAppsList.add(appId)
                } else if (!added) {
                    protectedAppsList.remove(appId)
                }
                queryInstalledApps(protectedAppsList)
            }
        }
    }
}