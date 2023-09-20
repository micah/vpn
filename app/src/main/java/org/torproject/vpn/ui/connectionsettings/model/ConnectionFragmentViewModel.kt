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
import org.torproject.vpn.R
import org.torproject.vpn.ui.exitselection.model.ViewTypeDependentModel
import org.torproject.vpn.utils.PreferenceHelper
import org.torproject.vpn.utils.PreferenceHelper.Companion.BridgeType

class ConnectionFragmentViewModel(private val application: Application) : AndroidViewModel(application) {

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

    val bridgeType = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (PreferenceHelper.BRIDGE_TYPE == changedKey) {
                preferenceHelper.bridgeType?.let {
                    trySend(getStringForBridgeType(it))
                } ?: kotlin.run {
                    trySend(application.getString(R.string.none))
                }
            }
        }
        preferenceHelper.registerListener(listener)
        awaitClose { preferenceHelper.unregisterListener(listener) }
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        getStringForBridgeType(preferenceHelper.bridgeType)
    )

    private fun getStringForBridgeType(type: BridgeType): String {
        return try {
            when (type) {
                BridgeType.None -> application.getString(R.string.none)
                BridgeType.Snowflake -> application.getString(R.string.snowflake_built_in)
                BridgeType.Obfs4 -> application.getString(R.string.obfs4_built_in)
                BridgeType.Manual -> application.getString(R.string.manual_bridge)
            }
        } catch (ise: java.lang.IllegalStateException) {
            ise.printStackTrace()
            application.getString(R.string.none)
        }
    }

}