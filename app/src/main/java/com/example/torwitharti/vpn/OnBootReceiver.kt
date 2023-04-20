package com.example.torwitharti.vpn

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.torwitharti.utils.PreferenceHelper

class OnBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            if (PreferenceHelper(context).startOnBoot && !VpnStatusObservable.isAlwaysOnBooting.get()) {
                VpnServiceCommand.startVpn(context)
            }
        }
    }
}
