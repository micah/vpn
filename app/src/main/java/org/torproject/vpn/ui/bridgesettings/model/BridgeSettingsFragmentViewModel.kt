package org.torproject.vpn.ui.bridgesettings.model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import org.torproject.vpn.utils.PreferenceHelper

class BridgeSettingsFragmentViewModel(application: Application) : AndroidViewModel(application) {

    val preferenceHelper = PreferenceHelper(application)


    fun selectBuiltInObfs4() {
        preferenceHelper.bridgeType = PreferenceHelper.Companion.BridgeType.Obfs4.name
        Log.d("BridgeSettingsFragmentViewModel", "selectBuiltInObfs4")
    }

    fun selectBuiltInSnowflake() {
        preferenceHelper.bridgeType = PreferenceHelper.Companion.BridgeType.Snowflake.name
        Log.d("BridgeSettingsFragmentViewModel", "selectBuiltInSnowflake")
    }
}