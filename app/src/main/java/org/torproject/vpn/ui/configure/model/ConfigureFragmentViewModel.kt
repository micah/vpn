package org.torproject.vpn.ui.configure.model

import android.app.Application
import android.content.SharedPreferences
import android.view.View
import android.widget.CompoundButton
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.torproject.vpn.R
import org.torproject.vpn.utils.PreferenceHelper
import org.torproject.vpn.utils.PreferenceHelper.Companion.BridgeType
import org.torproject.vpn.vpn.ConnectionState.CONNECTED
import org.torproject.vpn.vpn.ConnectionState.INIT
import org.torproject.vpn.vpn.VpnStatusObservable
import java.util.Locale

class ConfigureFragmentViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        val TAG = ConfigureFragmentViewModel::class.java.simpleName
    }
    private val preferenceHelper = PreferenceHelper(application)

    val startOnBoot: Boolean get() = preferenceHelper.startOnBoot

    val exitNodeCountry: String
        get() {
            val app = getApplication<Application>()
            val automaticExitNode = app.getString(R.string.exit_location_automatic)
            if (preferenceHelper.automaticExitNodeSelection) {
                return automaticExitNode
            }
            return preferenceHelper.exitNodeCountry?.let { countryCode ->
                Locale("", countryCode).displayCountry
            } ?: automaticExitNode
        }

    val selectedBridgeType: String
        get() {
            return getApplication<Application>().getString(
                when(preferenceHelper.bridgeType) {
                    BridgeType.None -> R.string.no_bridge
                    BridgeType.Snowflake -> R.string.snowflake_built_in
                    BridgeType.Obfs4 -> R.string.obfs4_built_in
                    BridgeType.Manual -> R.string.manual_bridge
                }
            )
        }

    val connectionState = VpnStatusObservable.statusLiveData
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = VpnStatusObservable.statusLiveData.value
        )

    val exitNodeSelectionVisibility = connectionState.map { state ->
        return@map if (state == CONNECTED) View.VISIBLE else View.GONE
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        View.GONE
    )

    private val allAppsProtected: StateFlow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (PreferenceHelper.PROTECT_ALL_APPS == changedKey) {
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
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (PreferenceHelper.PROTECTED_APPS == changedKey) {
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

    fun onStartOnBootChanged(compoundButton: CompoundButton, isChecked: Boolean) {
        preferenceHelper.startOnBoot = isChecked
    }

    val appProtectionLabel: StateFlow<String> = allAppsProtected.combine(someAppsProtected) { allApps, someApps ->
        if (allApps) {
            application.getString(R.string.apps_description_all_protected)
        } else if (someApps) {
            application.getString(R.string.label_some_protected)
        } else {
            application.getString(R.string.label_not_protected)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, "")
}