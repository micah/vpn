package org.torproject.vpn.ui.bridgesettings.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import org.torproject.vpn.utils.PreferenceHelper

class BridgeSettingsFragmentViewModel(application: Application) : AndroidViewModel(application) {

    val preferenceHelper = PreferenceHelper(application)


}