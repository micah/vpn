package com.example.torwitharti.vpn

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.torwitharti.utils.PreferenceHelper

class OnBootReceiver : BroadcastReceiver() {

    val TAG = OnBootReceiver::class.java.simpleName
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED || intent.action != "android.intent.action.QUICKBOOT_POWERON") {
            Log.e(TAG, "unexpected intent action ${intent?.action}")
            return
        }

        context?.let {
            if (PreferenceHelper(context).startOnBoot && !VpnStatusObservable.isAlwaysOnBooting.get()) {
                if (VpnServiceCommand.prepareVpn(context) == null) {
                    VpnServiceCommand.startVpn(context)
                }
            }
        }
    }
}
