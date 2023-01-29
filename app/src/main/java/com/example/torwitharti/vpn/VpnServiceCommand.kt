package com.example.torwitharti.vpn

import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.torwitharti.vpn.TorVpnService.Companion.ACTION_START_VPN
import com.example.torwitharti.vpn.TorVpnService.Companion.ACTION_STOP_VPN

object VpnServiceCommand {
    fun startVpn(context: Context?) {
        if (context == null) {
            return
        }
        val intent = Intent(context.applicationContext, TorVpnService::class.java)
        intent.action = ACTION_START_VPN
        startServiceIntent(context, intent)
    }


    fun stopVpn(context: Context?) {
        if (context == null) {
            return
        }
        val intent = Intent(context.applicationContext, TorVpnService::class.java)
        intent.action = ACTION_STOP_VPN
        startServiceIntent(context, intent)
    }

    private fun startServiceIntent(context: Context, intent: Intent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }
}