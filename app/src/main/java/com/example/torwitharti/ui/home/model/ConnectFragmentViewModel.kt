package com.example.torwitharti.ui.home.model

import android.app.Application
import android.content.Intent
import android.text.format.Formatter
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.example.torwitharti.BuildConfig
import com.example.torwitharti.R
import com.example.torwitharti.utils.PreferenceHelper
import com.example.torwitharti.vpn.ConnectionState
import com.example.torwitharti.vpn.ConnectionState.*
import com.example.torwitharti.vpn.DataUsage
import com.example.torwitharti.vpn.VpnServiceCommand
import com.example.torwitharti.vpn.VpnStatusObservable
import kotlinx.coroutines.flow.*

/**
 * ViewModel for slider fragment, mostly place holder at this point
 */

class ConnectFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val _prepareVpn = MutableLiveData<Intent?>()
    val prepareVpn: LiveData<Intent?> = _prepareVpn
    private val dataUsage = VpnStatusObservable.dataUsage.asFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = DataUsage()
        )

    val dataUsageDownstream: StateFlow<String> = dataUsage.map { data ->
        val received = Formatter.formatFileSize(application, data.downstreamData)
        return@map application.getString(R.string.stats_down, received)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = ""
    )

    val dataUsageUpstream: StateFlow<String> = dataUsage.map { data ->
        val sent = Formatter.formatFileSize(application, data.upstreamData)
        return@map application.getString(R.string.stats_up, sent)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = ""
    )

    val dataUsageDiffDownstream: StateFlow<String> = dataUsage.map { data ->
        val receivedDiff = Formatter.formatFileSize(application, data.downstreamDataPerSec)
        return@map application.getString(R.string.stats_delta, receivedDiff)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = ""
    )

    val dataUsageDiffUpstream: StateFlow<String> = dataUsage.map { data ->
        val receivedDiff = Formatter.formatFileSize(application, data.upstreamDataPerSec)
        return@map application.getString(R.string.stats_delta, receivedDiff)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = ""
    )

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
                    application.getString(R.string.state_connecting),
                    ContextCompat.getColor(application, R.color.purpleNormal)
                )
                PAUSED -> Pair(
                    application.getString(R.string.state_paused),
                    ContextCompat.getColor(application, R.color.yellowNormal)
                )
                CONNECTED -> Pair(
                    application.getString(R.string.state_connected),
                    ContextCompat.getColor(application, R.color.greenNormal)
                )
                DISCONNECTING -> Pair(
                    application.getString(R.string.state_disconnecting),
                    ContextCompat.getColor(application, R.color.purpleNormal)
                )
                DISCONNECTED -> Pair(
                    application.getString(R.string.state_disconnected),
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
            INIT, PAUSED -> application.getString(R.string.action_connect)
            DISCONNECTED -> application.getString(R.string.action_reconnect)
            CONNECTION_ERROR -> application.getString(R.string.action_try_again)
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

    val allAppsProtected: Boolean get() = PreferenceHelper(getApplication()).protectAllApps
    fun onProtectAppsChanged(compoundButton: CompoundButton, isChecked: Boolean) {
        PreferenceHelper(getApplication()).protectAllApps = isChecked
    }

    private fun attemptPause() {
        //TODO: what are we going to try in paused state?
        VpnStatusObservable.update(DISCONNECTING)
        VpnServiceCommand.stopVpn(getApplication())
        PreferenceHelper(getApplication()).startOnBoot = false
    }

    private fun attemptConnect() {
        VpnStatusObservable.update(CONNECTING)
        prepareToStartVPN()
    }

    private fun attemptDisconnect() {
        VpnStatusObservable.update(DISCONNECTING)
        VpnServiceCommand.stopVpn(getApplication())
        PreferenceHelper(getApplication()).startOnBoot = false
    }

    private fun attemptCancelConnect() {
        VpnStatusObservable.update(DISCONNECTING)
        VpnServiceCommand.stopVpn(getApplication())
        PreferenceHelper(getApplication()).startOnBoot = false
        //TODO
    }

    fun prepareToStartVPN() {
        val vpnIntent: Intent? = VpnServiceCommand.prepareVpn(getApplication())
        if (vpnIntent != null) {
            _prepareVpn.postValue(vpnIntent)
        } else {
            VpnServiceCommand.startVpn(getApplication())
            PreferenceHelper(getApplication()).startOnBoot = true
        }
    }

    fun onVpnPrepared() {
        _prepareVpn.value = null
    }
}
