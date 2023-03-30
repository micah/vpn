package com.example.torwitharti.ui.home.model

import android.app.Application
import android.content.Intent
import android.net.VpnService
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.example.torwitharti.BuildConfig
import com.example.torwitharti.R
import com.example.torwitharti.vpn.ConnectionState
import com.example.torwitharti.vpn.ConnectionState.*
import com.example.torwitharti.vpn.VpnServiceCommand
import com.example.torwitharti.vpn.VpnStatusObservable
import kotlinx.coroutines.flow.*

/**
 * ViewModel for slider fragment, mostly place holder at this point
 */

class ConnectFragmentViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        val TAG: String = ConnectFragmentViewModel.javaClass.simpleName
    }

    private val _prepareVpn = MutableLiveData<Intent?>()
    val prepareVpn: LiveData<Intent?> = _prepareVpn

    val connectionState = VpnStatusObservable.statusLiveData.asFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = INIT
        )

    val toolBarTitleAndColor: StateFlow<Pair<String, Int>> =
        connectionState.map { connectionState ->

            when (connectionState) {
                CONNECTING -> Pair(
                    application.getString(R.string.frag_connect_connecting),
                    ContextCompat.getColor(application, R.color.purpleNormal)
                )
                PAUSED -> Pair(
                    application.getString(R.string.frag_connect_paused),
                    ContextCompat.getColor(application, R.color.yellowNormal)
                )
                CONNECTED -> Pair(
                    application.getString(R.string.frag_connect_connected),
                    ContextCompat.getColor(application, R.color.greenNormal)
                )
                DISCONNECTING -> Pair(
                    application.getString(R.string.frag_connect_disconnecting),
                    ContextCompat.getColor(application, R.color.purpleNormal)
                )
                DISCONNECTED -> Pair(
                    application.getString(R.string.frag_connect_disconnected),
                    ContextCompat.getColor(application, R.color.redNormal)
                )
                else -> {
                    return@map Pair("", R.color.purpleDark)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            SharingStarted.Lazily,
            initialValue = Pair("", R.color.purpleDark)
        )

    val connectButtonText: StateFlow<String> = connectionState.map { connectionState ->
        when (connectionState) {
            INIT, PAUSED -> application.getString(R.string.frag_connect_connect)
            DISCONNECTED -> application.getString(R.string.frag_connect_reconnect)
            CONNECTION_ERROR -> application.getString(R.string.frag_connect_try_again)
            else -> {
                return@map ""
            }
        }
    }.stateIn(scope = viewModelScope, SharingStarted.WhileSubscribed(), initialValue = "")

    //these are static one-time-fetch value on viewModel init. Dont need to be LiveData or StateFlow.
    val flavor = "Pre-alpha"
    val version = BuildConfig.VERSION_NAME

    fun connectStateButtonClicked() {
        when (VpnStatusObservable.statusLiveData.value as ConnectionState) {
            INIT -> attemptConnect()
            CONNECTING -> attemptPause()
            CONNECTED -> attemptDisconnect()
            CONNECTION_ERROR -> attemptDisconnect()
            DISCONNECTING -> attemptConnect()
            DISCONNECTED -> attemptConnect()
            PAUSED -> {}
        }
    }

    //TODO
    fun viewLogsClicked() {
    }

    private fun attemptPause() {
        //TODO: what are we going to try in paused state?
        VpnStatusObservable.update(DISCONNECTING)
        VpnServiceCommand.stopVpn(getApplication())
    }

    private fun attemptConnect() {
        VpnStatusObservable.update(CONNECTING)
        prepareToStartVPN()

    }

    private fun attemptDisconnect() {
        VpnStatusObservable.update(DISCONNECTING)
        VpnServiceCommand.stopVpn(getApplication())
    }

    private fun attemptCancelConnect() {
        VpnStatusObservable.update(DISCONNECTING)
        VpnServiceCommand.stopVpn(getApplication())
        //TODO
    }

    private fun prepareToStartVPN() {
        var vpnIntent: Intent? = null
        try {
            vpnIntent =
                VpnService.prepare(getApplication()) // stops the VPN connection created by another application.
        } catch (npe: NullPointerException) {
            VpnStatusObservable.update(CONNECTION_ERROR)
        } catch (ise: IllegalStateException) {
            VpnStatusObservable.update(CONNECTION_ERROR)
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
}
