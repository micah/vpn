package com.example.torwitharti.ui.home.model

import android.app.Application
import android.content.Intent
import android.net.VpnService
import android.util.Log
import androidx.lifecycle.*
import com.example.torwitharti.vpn.ConnectionState
import com.example.torwitharti.vpn.VpnServiceCommand
import com.example.torwitharti.vpn.VpnStatusObservable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for slider fragment, mostly place holder at this point
 */

class ConnectFragmentViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        val TAG: String = ConnectFragmentViewModel.javaClass.simpleName
    }

    private val _vpnStatusFlow =
        MutableStateFlow(VpnConnectionUIState(
            isNavigationPending = true,
            connectionState = ConnectionState.INIT,
            animate = false
        ))
    private val _showGuideSlide = MutableLiveData<Boolean>(true)
    private val _prepareVpn = MutableLiveData<Intent?>()


    val vpnStatusFlow: StateFlow<VpnConnectionUIState> = _vpnStatusFlow
    val showGuideSlide: LiveData<Boolean> = _showGuideSlide
    val prepareVpn: LiveData<Intent?> = _prepareVpn

    val connectionState: LiveData<ConnectionState> = Transformations.map(
        VpnStatusObservable.statusLiveData) { connection ->
        Log.d(TAG, "connectionState from ConnectFragmentViewModel: $connection")

        _vpnStatusFlow.update {
            it.copy(
                connectionState = connection,
                animate = true,
                isNavigationPending = true
            )
        }
        connection
    }


    fun connectStateButtonClicked() {
        when (VpnStatusObservable.statusLiveData.value as ConnectionState) {
            ConnectionState.INIT -> attemptConnect()
            ConnectionState.CONNECTING -> attemptPause()
            ConnectionState.CONNECTED -> attemptDisconnect()
            ConnectionState.CONNECTION_ERROR -> attemptDisconnect()
            ConnectionState.DISCONNECTING -> attemptConnect()
            ConnectionState.DISCONNECTED -> attemptConnect()
            ConnectionState.PAUSED -> {}
        }
    }

    private fun attemptPause() {
        //TODO: what are we going to try in paused state?
        VpnServiceCommand.stopVpn(getApplication())
    }

    private fun attemptConnect() {
        VpnStatusObservable.update(ConnectionState.CONNECTING)
        prepareToStartVPN()
    }

    private fun attemptDisconnect() {
        VpnStatusObservable.update(ConnectionState.DISCONNECTING)
        VpnServiceCommand.stopVpn(getApplication())
    }

    private fun attemptCancelConnect() {
        VpnStatusObservable.update(ConnectionState.DISCONNECTING)
        VpnServiceCommand.stopVpn(getApplication())
        //TODO
    }

    fun appNavigationCompleted() {
        _vpnStatusFlow.update { it.copy(isNavigationPending = false) }
    }

    private fun prepareToStartVPN() {
        var vpnIntent: Intent? = null
        try {
            vpnIntent =
                VpnService.prepare(getApplication()) // stops the VPN connection created by another application.
        } catch (npe: NullPointerException) {
            VpnStatusObservable.update(ConnectionState.CONNECTION_ERROR)
        } catch (ise: IllegalStateException) {
            VpnStatusObservable.update(ConnectionState.CONNECTION_ERROR)
        }
        if (vpnIntent != null) {
            _prepareVpn.postValue(vpnIntent)
        } else {
            VpnServiceCommand.startVpn(getApplication())
        }
    }

    fun onVpnPrepared() {
        _prepareVpn.value = null
    }

    data class VpnConnectionUIState(
        val connectionState: ConnectionState,
        val isNavigationPending: Boolean,
        val animate: Boolean
    )
}
