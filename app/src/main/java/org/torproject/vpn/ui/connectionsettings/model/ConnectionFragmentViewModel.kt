package org.torproject.vpn.ui.connectionsettings.model

import android.app.Application
import android.content.SharedPreferences
import android.widget.CompoundButton
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import org.torproject.vpn.ui.exitselection.model.ViewTypeDependentModel
import org.torproject.vpn.utils.PreferenceHelper

class ConnectionFragmentViewModel(application: Application) : AndroidViewModel(application) {

    val preferenceHelper = PreferenceHelper(application)

    val startOnBoot: Boolean get() = preferenceHelper.startOnBoot
    fun onStartOnBootChanged(compoundButton: CompoundButton, isChecked: Boolean) {
        preferenceHelper.startOnBoot = isChecked
    }

    fun onUseBridgeChanged(compoundButton: CompoundButton, isChecked: Boolean) {
        preferenceHelper.useBridge = isChecked
        _useBridge.postValue(isChecked)
    }

    private val _useBridge = MutableLiveData(preferenceHelper.useBridge)
    val useBridge: LiveData<Boolean> = _useBridge
}