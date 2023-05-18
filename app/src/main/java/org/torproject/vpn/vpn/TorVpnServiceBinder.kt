package org.torproject.vpn.vpn

import android.os.Binder
import org.torproject.onionmasq.ISocketProtect
import java.lang.ref.WeakReference

class TorVpnServiceBinder(private val torVpnServiceReference: WeakReference<TorVpnService>) : Binder(), ISocketProtect {
    override fun protect(socket: Int): Boolean {
        torVpnServiceReference.get()?.let {
            return it.protect(socket)
        }
        return false
    }
}