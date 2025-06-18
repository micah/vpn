package org.torproject.vpn

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.torproject.onionmasq.OnionMasq
import org.torproject.vpn.ui.approuting.data.AppManager
import android.util.Log
import org.torproject.vpn.ui.approuting.AppQueryReceiver

class TorApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        OnionMasq.init(this)
        AppQueryReceiver.register(this)
        val appManager = AppManager(this)
        CoroutineScope(Dispatchers.IO).launch {
            appManager.checkNewInstalledApps()
        }
    }
}