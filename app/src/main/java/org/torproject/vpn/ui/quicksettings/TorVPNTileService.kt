package org.torproject.vpn.ui.quicksettings


import android.annotation.SuppressLint
import android.graphics.drawable.Icon
import android.os.Handler
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.lifecycle.Observer
import org.torproject.vpn.R
import org.torproject.vpn.vpn.ConnectionState
import org.torproject.vpn.vpn.VpnServiceCommand
import org.torproject.vpn.vpn.VpnStatusObservable


class TorVPNTileService : TileService() {

    private var vpnStatusObserver: Observer<ConnectionState>? = null
    private val mainHandler: Handler by lazy {
        Handler(mainLooper)
    }

    @SuppressLint("Override")
    override fun onClick() {
        super.onClick()
        if (!isLocked) {
            onTileTap()
        } else {
            unlockAndRun { onTileTap() }
        }
    }

    private fun onTileTap() {
        if (VpnStatusObservable.isVPNActive()) {
            VpnServiceCommand.stopVpn(this)
        } else {
            VpnServiceCommand.startVpn(this)
        }
    }

    override fun onTileAdded() {
    }

    override fun onCreate() {
        super.onCreate()
        vpnStatusObserver = Observer<ConnectionState> { connectionState: ConnectionState? ->
            onStatusChanged(connectionState)
        }
    }

    private fun onStatusChanged(connectionState: ConnectionState?) {
        val t = qsTile ?: return
        Log.d("TILE_TOR_VPN", "CONNECTION STATE CHANGED: ${connectionState?.name}")
        //TODO: add and update an icon
        when (connectionState) {
            ConnectionState.INIT,
            ConnectionState.DISCONNECTED -> {
                t.state = Tile.STATE_INACTIVE
                t.label = getString(R.string.qs_connect)
            }
            ConnectionState.CONNECTING -> {
                t.state = Tile.STATE_ACTIVE
                t.label = getString(R.string.qs_connecting)
            }
            ConnectionState.DISCONNECTING,
            ConnectionState.CONNECTED -> {
                t.state = Tile.STATE_ACTIVE
                t.label = getString(R.string.qs_disconnect)
            }
            ConnectionState.CONNECTION_ERROR -> {
                t.state = Tile.STATE_UNAVAILABLE
                t.label = getString(R.string.qs_error)
            }
            else -> {
                t.state = Tile.STATE_UNAVAILABLE
                t.label = getString(R.string.qs_connect)
            }
        }
        t.icon = Icon.createWithResource(this, R.drawable.ic_launcher_foreground_qs)

        t.updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        Log.d("TILE_TOR_VPN", "onStartListening")
        vpnStatusObserver?.let {
            mainHandler.post {
                Log.d("TILE_TOR_VPN", "onStartListening - observe forever")
                VpnStatusObservable.statusLiveData.observeForever(it) }
        }
        onStatusChanged(VpnStatusObservable.statusLiveData.value)
    }

    override fun onStopListening() {
        vpnStatusObserver?.let {
            Log.d("TILE_TOR_VPN", "onStopListening")
            mainHandler.post {
                Log.d("TILE_TOR_VPN", "onStopListening - remove observer")
                VpnStatusObservable.statusLiveData.removeObserver(it)
            }
        }
        super.onStopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        vpnStatusObserver = null;

    }
}
