package org.torproject.vpn.ui.approuting

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import org.torproject.vpn.ui.approuting.data.AppManager

class AppQueryReceiver : BroadcastReceiver() {

    val TAG = AppQueryReceiver::class.java.simpleName

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null ||
            (intent.action != Intent.ACTION_PACKAGE_ADDED &&
                    intent.action != Intent.ACTION_PACKAGE_REMOVED)
        ) {
            return
        }


        val appManager = AppManager(context)

        intent.data?.encodedSchemeSpecificPart?.let { packageName ->
            when (intent.action) {
                Intent.ACTION_PACKAGE_ADDED -> {
                    appManager.onAppIdChanged(true, packageName)
                }

                Intent.ACTION_PACKAGE_REMOVED -> {
                    appManager.onAppIdChanged(false, packageName)
                }

                else -> {}
            }
        }
    }

    companion object {
        fun register(context: Context) {
            val filter = IntentFilter()
            filter.addAction(Intent.ACTION_PACKAGE_ADDED)
            filter.addAction(Intent.ACTION_PACKAGE_REMOVED)
            filter.addDataScheme("package")
            context.registerReceiver(AppQueryReceiver(), filter)
        }
    }
}
