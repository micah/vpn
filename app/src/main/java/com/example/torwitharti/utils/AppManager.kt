package com.example.torwitharti.utils

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
import com.example.torwitharti.ui.settings.AppItemModel


class AppManager(context: Context) {
    private val context: Context
    private val preferenceHelper: PreferenceHelper
    private val pm: PackageManager
    init {
        this.context = context
        this.preferenceHelper = PreferenceHelper(context)
        this.pm = context.packageManager
    }

    @WorkerThread
    fun queryInstalledApps() : List<AppItemModel> {
        val installedPackages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val installedBrowsers = getInstalledBrowserPackages()
        var androidSystemUid = 0
        val apps = mutableListOf<AppItemModel>()

        // only Add apps using which are allowed to use internet
        try {
            val system = pm.getApplicationInfo("android", PackageManager.GET_META_DATA)
            androidSystemUid = system.uid
            createAppItemModel(system)?.also {
                apps.add(it)
            }
        } catch (e: PackageManager.NameNotFoundException) {
        }

        for (appInfo in installedPackages) {
            if (pm.checkPermission(INTERNET, appInfo.packageName) == PERMISSION_GRANTED && appInfo.uid != androidSystemUid) {
                createAppItemModel(appInfo, installedBrowsers)?.also {
                    apps.add(it)
                }
            }
        }

        return apps.sortedBy { it.name }
    }

    private fun createAppItemModel(applicationInfo: ApplicationInfo, browserPackages: List<String> = listOf()): AppItemModel? {
        var appName = applicationInfo.loadLabel(pm) as? String
        if (TextUtils.isEmpty(appName))
            appName = applicationInfo.packageName

        appName?.also {
            val appIcon = applicationInfo.loadIcon(pm)
            val appUID = applicationInfo.uid
            val packageName = applicationInfo.packageName
            return AppItemModel(
                it,
                packageName,
                appUID,
                appIcon,
                false,
                browserPackages.contains(packageName),
                false)
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
}