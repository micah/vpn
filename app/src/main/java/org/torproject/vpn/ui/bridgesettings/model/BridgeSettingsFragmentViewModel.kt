package org.torproject.vpn.ui.bridgesettings.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import org.torproject.vpn.R
import org.torproject.vpn.utils.PreferenceHelper
import org.torproject.vpn.utils.PreferenceHelper.Companion.BridgeType

class BridgeSettingsFragmentViewModel(application: Application) : AndroidViewModel(application) {

    val preferenceHelper = PreferenceHelper(application)

    fun getSelectedBridgeTypeId(): Int {
        return when(preferenceHelper.bridgeType) {
            BridgeType.Obfs4 -> R.id.rb_obfs4
            BridgeType.Snowflake -> R.id.rb_snowflake
            else -> -1
        }
    }

    fun selectBuiltInObfs4() {
        preferenceHelper.bridgeType = BridgeType.Obfs4
    }

    fun selectBuiltInSnowflake() {
        preferenceHelper.bridgeType = BridgeType.Snowflake
    }
}