package org.torproject.vpn

import android.app.Application
import org.torproject.onionmasq.OnionMasq

class TorApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        OnionMasq.init(this)
    }
}