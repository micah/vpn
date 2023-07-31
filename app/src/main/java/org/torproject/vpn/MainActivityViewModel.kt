package org.torproject.vpn

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import org.torproject.vpn.utils.PreferenceHelper

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val preferenceHelper = PreferenceHelper(application)

    //bottom nav visibility depends on guide screen visibility among other flags
    val guideScreenVisibility = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (PreferenceHelper.SHOULD_SHOW_GUIDE == changedKey) {
                trySend(!preferenceHelper.shouldShowGuide) // invert bottom nav visibility in respect to guide visibility
            }
        }
        trySend(!preferenceHelper.shouldShowGuide)
        preferenceHelper.registerListener(listener)
        awaitClose { preferenceHelper.unregisterListener(listener) }
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        false
    )
}