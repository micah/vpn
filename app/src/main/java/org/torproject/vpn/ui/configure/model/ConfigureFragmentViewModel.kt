package org.torproject.vpn.ui.configure.model

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.torproject.vpn.R
import org.torproject.vpn.utils.PreferenceHelper

class ConfigureFragmentViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        val TAG = ConfigureFragmentViewModel::class.java.simpleName
    }
    private val preferenceHelper = PreferenceHelper(application)

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