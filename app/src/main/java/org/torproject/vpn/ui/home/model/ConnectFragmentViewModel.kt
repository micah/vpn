package org.torproject.vpn.ui.home.model

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.torproject.vpn.BuildConfig
import org.torproject.vpn.R
import org.torproject.vpn.ui.approuting.model.AppItemModel
import org.torproject.vpn.utils.PreferenceHelper
import org.torproject.vpn.utils.PreferenceHelper.Companion.BridgeType
import org.torproject.vpn.utils.PreferenceHelper.Companion.PROTECTED_APPS
import org.torproject.vpn.utils.PreferenceHelper.Companion.PROTECT_ALL_APPS
import org.torproject.vpn.utils.PreferenceHelper.Companion.SHOULD_SHOW_GUIDE
import org.torproject.vpn.utils.formatBits
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
import java.lang.reflect.Type

/**
 * ViewModel for slider fragment, mostly place holder at this point
 */
const val ACTION_LOGS = 110
const val ACTION_REQUEST_NOTIFICATION_PERMISSION = 111
const val ACTION_EXIT_NODE_SELECTION = 113
const val ACTION_APPS = 114
const val ACTION_CONNECTION = 115

class ConnectFragmentViewModel(private val application: Application) : AndroidViewModel(application) {

    private val _prepareVpn = MutableLiveData<Intent?>()
    private val preferenceHelper = PreferenceHelper(application)

    val prepareVpn: LiveData<Intent?> = _prepareVpn

    private val dataUsage = VpnStatusObservable.dataUsage
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = DataUsage()
        )

    val dataUsageDownstream: StateFlow<String> = dataUsage.map { data ->
        val received = formatBits(data.downstreamData)
        return@map application.getString(R.string.stats_down, received)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = ""
    )

    val dataUsageUpstream: StateFlow<String> = dataUsage.map { data ->
        val sent = formatBits(data.upstreamData)
        return@map application.getString(R.string.stats_up, sent)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = ""
    )

    val dataUsageDiffDownstream: StateFlow<String> = dataUsage.map { data ->
        val receivedDiff = formatBits(data.downstreamDataPerSec)
        return@map "+${application.getString(R.string.stats_delta, receivedDiff)}"
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = ""
    )

    val dataUsageDiffUpstream: StateFlow<String> = dataUsage.map { data ->
        val receivedDiff = formatBits(data.upstreamDataPerSec)
        return@map "+${application.getString(R.string.stats_delta, receivedDiff)}"
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = ""
    )

    val connectionState = VpnStatusObservable.statusLiveData
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = VpnStatusObservable.statusLiveData.value ?: INIT
        )
    val internetConnectivity = VpnStatusObservable.hasInternetConnectivity
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = VpnStatusObservable.hasInternetConnectivity.value ?: true
        )

    val toolBarTitle: StateFlow<String> = combine(
        connectionState,
        internetConnectivity
    ) {
        connState, inetConnectivity ->
        return@combine when (connState) {
            INIT -> application.getString(R.string.idle_toolbar_title)
            CONNECTING -> if (inetConnectivity) application.getString(R.string.state_connecting) else application.getString(R.string.no_internet)
            CONNECTED -> if (inetConnectivity) application.getString(R.string.state_connected) else application.getString(R.string.no_internet)
            DISCONNECTING -> if (inetConnectivity) application.getString(R.string.state_disconnecting)else application.getString(R.string.no_internet)
            DISCONNECTED -> application.getString(R.string.state_disconnected)
            else -> {
                ""
            }
        }
    }.stateIn(
        scope = viewModelScope,
        SharingStarted.Lazily,
        initialValue = ""
    )

    val connectButtonText: StateFlow<String> = connectionState.map { connectionState ->
        when (connectionState) {
            INIT -> application.getString(R.string.action_connect)
            CONNECTING -> application.getString(R.string.action_cancel)
            DISCONNECTING -> application.getString(R.string.action_reconnect)
            DISCONNECTED -> application.getString(R.string.action_reconnect)
            CONNECTION_ERROR -> application.getString(R.string.action_try_again)
            CONNECTED -> application.getString(R.string.action_stop)
        }
    }.stateIn(scope = viewModelScope, SharingStarted.WhileSubscribed(), initialValue = "")

    val selectedCountry: MutableLiveData<String> =
        MutableLiveData(if (preferenceHelper.automaticExitNodeSelection) "" else preferenceHelper.exitNodeCountry)
    val countryDrawable: StateFlow<Drawable?> = selectedCountry.asFlow().map { countryCode ->
        return@map getFlagByCountryCode(application, countryCode)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _connectionHint: MutableLiveData<String> = MutableLiveData(getConnectionString())
    val connectionHint: LiveData<String> = _connectionHint

    //these are static one-time-fetch values on viewModel init. Don't need to be LiveData or StateFlow.
    val flavor = "Pre-alpha"
    val version = BuildConfig.VERSION_NAME

    private val _action = MutableSharedFlow<Int>(replay = 0)

    val action: SharedFlow<Int>
        get() = _action

    val guideScreenVisibility = callbackFlow {
        val listener = OnSharedPreferenceChangeListener { _, changedKey ->
            if (SHOULD_SHOW_GUIDE == changedKey) {
                trySend(preferenceHelper.shouldShowGuide)
            }
        }
        trySend(preferenceHelper.shouldShowGuide)
        preferenceHelper.registerListener(listener)
        awaitClose { preferenceHelper.unregisterListener(listener) }
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        true
    )

    val appCardVisibility = combine(
        guideScreenVisibility,
        connectionState
    ) { guideScreenVisibility, connectionState -> !guideScreenVisibility && (connectionState == INIT || connectionState == CONNECTED) }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            false
        )

    val connectionCardVisibility = combine(
        guideScreenVisibility,
        connectionState
    ) { guideScreenVisibility, connectionState -> !guideScreenVisibility && (connectionState == INIT) }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            false
        )


    fun setGuideInvisible() {
        preferenceHelper.shouldShowGuide = false
    }

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

    fun appCardClicked() {
        viewModelScope.launch {
            _action.emit(ACTION_APPS)
        }
    }

    fun connectionCardClicked() {
        viewModelScope.launch {
            _action.emit(ACTION_CONNECTION)
        }
    }

    val allAppsProtected: StateFlow<Boolean> = callbackFlow {
        val listener = OnSharedPreferenceChangeListener { _, changedKey ->
            if (PROTECT_ALL_APPS == changedKey) {
                trySend(preferenceHelper.protectAllApps)
            }
        }
        trySend(preferenceHelper.protectAllApps)
        preferenceHelper.registerListener(listener)
        awaitClose { preferenceHelper.unregisterListener(listener) }
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        preferenceHelper.protectAllApps
    )

    private val someAppsProtected: StateFlow<Boolean> = callbackFlow {
        val listener = OnSharedPreferenceChangeListener { _, changedKey ->
            if (PROTECTED_APPS == changedKey) {
                trySend(!preferenceHelper.protectedApps.isNullOrEmpty())
            }
        }
        trySend(!preferenceHelper.protectedApps.isNullOrEmpty())
        preferenceHelper.registerListener(listener)
        awaitClose { preferenceHelper.unregisterListener(listener) }
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        !preferenceHelper.protectedApps.isNullOrEmpty()
    )

    val appProtectionLabel: StateFlow<String> = allAppsProtected.combine(someAppsProtected) { allApps, someApps ->
        if (allApps) {
            application.getString(R.string.apps_description_all_protected)
        } else if (someApps) {
            application.getString(R.string.label_some_protected)
        } else {
            application.getString(R.string.label_not_protected)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, "")


    fun onProtectAppsChanged(compoundButton: CompoundButton, isChecked: Boolean) {
        val mutableList = loadCachedApps()
        val protectedApps = emptySet<String>().toMutableSet()
        mutableList.onEach {
            it.protectAllApps = isChecked
            it.isRoutingEnabled = isChecked
            if (isChecked) {
                it.appId?.let { appId ->
                    protectedApps.add(appId)
                }
            }
        }
        preferenceHelper.protectedApps = protectedApps
        preferenceHelper.cachedApps = Gson().toJson(mutableList)
        preferenceHelper.protectAllApps = isChecked
    }

    private fun loadCachedApps(): List<AppItemModel> {
        val gson = Gson()
        val appItemModelListType: Type = object : TypeToken<ArrayList<AppItemModel?>?>() {}.type
        gson.fromJson<List<AppItemModel>>(preferenceHelper.cachedApps, appItemModelListType)?.let { list ->
            return list
        } ?: run {
            return emptyList()
        }
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
            selectedCountry.postValue("")
        } else {
            selectedCountry.postValue(preferenceHelper.exitNodeCountry)
        }
    }

    fun updateConnectionLabel() {
        _connectionHint.postValue(getConnectionString())
    }

    private fun getConnectionString(): String {
        if (!preferenceHelper.useBridge) {
            return application.getString(R.string.connect_direct_to_tor)
        }
        return when (preferenceHelper.bridgeType) {
            BridgeType.None -> application.getString(R.string.connect_direct_to_tor)
            BridgeType.Snowflake -> application.getString(R.string.snowflake_built_in)
            BridgeType.Obfs4 -> application.getString(R.string.obfs4_built_in)
            BridgeType.Manual -> application.getString(R.string.manual_bridge)
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
