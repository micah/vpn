package org.torproject.vpn.ui.bridgesettings.model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import org.torproject.vpn.utils.PreferenceHelper
import org.torproject.vpn.utils.PreferenceHelper.Companion.BridgeType

class BridgeSettingsFragmentViewModel(application: Application) : AndroidViewModel(application) {

    val preferenceHelper = PreferenceHelper(application)


    fun selectBuiltInObfs4() {
        preferenceHelper.bridgeType = BridgeType.Obfs4
        Log.d("BridgeSettingsFragmentViewModel", "selectBuiltInObfs4")
    }

    fun selectBuiltInSnowflake() {
        preferenceHelper.bridgeType = BridgeType.Snowflake
        Log.d("BridgeSettingsFragmentViewModel", "selectBuiltInSnowflake")
    }
}