package org.torproject.vpn.vpn

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import org.torproject.vpn.vpn.TorVpnService.Companion.ACTION_START_VPN
import org.torproject.vpn.vpn.TorVpnService.Companion.ACTION_STOP_VPN

object VpnServiceCommand {

    fun prepareVpn(context: Context?): Intent? {
        try {
            return VpnService.prepare(context?.applicationContext) // stops the VPN connection created by another application.
        } catch (npe: NullPointerException) {
            VpnStatusObservable.update(ConnectionState.CONNECTION_ERROR)
        } catch (ise: IllegalStateException) {
            VpnStatusObservable.update(ConnectionState.CONNECTION_ERROR)
        }
        return null
    }

    fun startVpn(context: Context?) {
        launchTorVpnServiceWithAction(context, ACTION_START_VPN)
    }


    fun stopVpn(context: Context?) {
        launchTorVpnServiceWithAction(context, ACTION_STOP_VPN)
    }

    private fun launchTorVpnServiceWithAction(context: Context?, action: String) {
        if (context == null) {
            return
        }

        val data = Data.Builder()
            .putString(VpnServiceLauncher.COMMAND, action)
            .build()

        val work = OneTimeWorkRequestBuilder<VpnServiceLauncher>()
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueue(work)
    }
}