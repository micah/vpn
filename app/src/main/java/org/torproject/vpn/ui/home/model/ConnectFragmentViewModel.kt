package org.torproject.vpn.ui.home.model

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.format.Formatter
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.torproject.vpn.BuildConfig
import org.torproject.vpn.R
import org.torproject.vpn.utils.PreferenceHelper
import org.torproject.vpn.utils.getFlagByCountryCode
import org.torproject.vpn.vpn.ConnectionState
import org.torproject.vpn.vpn.ConnectionState.CONNECTED
import org.torproject.vpn.vpn.ConnectionState.CONNECTING
import org.torproject.vpn.vpn.ConnectionState.CONNECTION_ERROR
import org.torproject.vpn.vpn.ConnectionState.DISCONNECTED
import org.torproject.vpn.vpn.ConnectionState.DISCONNECTING
import org.torproject.vpn.vpn.ConnectionState.INIT
import org.torproject.vpn.vpn.DataUsage
import org.torproject.vpn.vpn.VpnServiceCommand
import org.torproject.vpn.vpn.VpnStatusObservable

/**
 * ViewModel for slider fragment, mostly place holder at this point
 */
const val ACTION_LOGS = 110
const val ACTION_REQUEST_NOTIFICATION_PERMISSION = 111
const val ACTION_EXIT_NODE_SELECTION = 113

class ConnectFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val _prepareVpn = MutableLiveData<Intent?>()
    private val preferenceHelper = PreferenceHelper(application)

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
            INIT -> application.getString(R.string.action_connect)
            CONNECTING -> application.getString(R.string.action_cancel)
            DISCONNECTED -> application.getString(R.string.action_reconnect)
            CONNECTION_ERROR -> application.getString(R.string.action_try_again)
            else -> {
                return@map ""
            }
        }
    }.stateIn(scope = viewModelScope, SharingStarted.WhileSubscribed(), initialValue = "")

    val selectedCountry: MutableLiveData<String?> = MutableLiveData(if (preferenceHelper.automaticExitNodeSelection) null else preferenceHelper.exitNodeCountry)
    val countryDrawable: StateFlow<Drawable?> = selectedCountry.asFlow().map { countryCode ->
        return@map getFlagByCountryCode(application, countryCode)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    //these are static one-time-fetch values on viewModel init. Dont need to be LiveData or StateFlow.
    val flavor = "Pre-alpha"
    val version = BuildConfig.VERSION_NAME

    private val _action = MutableSharedFlow<Int>(replay = 0)

    val action: SharedFlow<Int>
        get() = _action


    fun connectStateButtonClicked() {
        when (VpnStatusObservable.statusLiveData.value as ConnectionState) {
            INIT -> attemptConnect()
            CONNECTING -> attemptCancel()
            CONNECTED -> attemptDisconnect()
            CONNECTION_ERROR -> attemptConnect()
            DISCONNECTING -> attemptConnect()
            DISCONNECTED -> attemptConnect()
        }
    }

    fun exitNodeSelectionButtonClicked() {
        viewModelScope.launch {
            _action.emit(ACTION_EXIT_NODE_SELECTION)
        }
    }

    fun viewLogsClicked() {
        viewModelScope.launch {
            _action.emit(ACTION_LOGS)
        }
    }

    val allAppsProtected: Boolean get() = PreferenceHelper(getApplication()).protectAllApps
    fun onProtectAppsChanged(compoundButton: CompoundButton, isChecked: Boolean) {
        preferenceHelper.protectAllApps = isChecked
    }

    private fun attemptCancel() {
        VpnStatusObservable.update(DISCONNECTING)
        VpnServiceCommand.stopVpn(getApplication())
    }

    private fun attemptConnect() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            VpnStatusObservable.update(CONNECTING)
            prepareToStartVPN()
            return
        }

        if (ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            VpnStatusObservable.update(CONNECTING)
            prepareToStartVPN()
        } else {
            viewModelScope.launch {
                _action.emit(ACTION_REQUEST_NOTIFICATION_PERMISSION)
            }
        }
    }

    private fun attemptDisconnect() {
        VpnStatusObservable.update(DISCONNECTING)
        VpnServiceCommand.stopVpn(getApplication())
    }

    private fun attemptCancelConnect() {
        VpnStatusObservable.update(DISCONNECTING)
        VpnServiceCommand.stopVpn(getApplication())
    }

    fun prepareToStartVPN() {
        val vpnIntent: Intent? = VpnServiceCommand.prepareVpn(getApplication())
        if (vpnIntent != null) {
            _prepareVpn.postValue(vpnIntent)
        } else {
            VpnServiceCommand.startVpn(getApplication())
        }
    }

    fun updateVPNSettings() {
        if (VpnStatusObservable.isVPNActive()) {
            VpnServiceCommand.startVpn(getApplication())
        }
    }

    fun updateExitNodeButton() {
        if (preferenceHelper.automaticExitNodeSelection) {
            selectedCountry.postValue(null)
        } else {
            selectedCountry.postValue(preferenceHelper.exitNodeCountry)
        }
    }

    fun onVpnPrepared() {
        _prepareVpn.value = null
    }

    fun onNotificationPermissionResult() {
        VpnStatusObservable.update(CONNECTING)
        prepareToStartVPN()
    }
}
