package org.torproject.vpn.ui.connectionsettings.model

import android.app.Application
import android.widget.CompoundButton
import androidx.lifecycle.AndroidViewModel
import org.torproject.vpn.utils.PreferenceHelper

class ConnectionFragmentViewModel(application: Application) : AndroidViewModel(application) {

    val startOnBoot: Boolean get() = PreferenceHelper(getApplication()).startOnBoot
    fun onStartOnBootChanged(compoundButton: CompoundButton, isChecked: Boolean) {
        PreferenceHelper(getApplication()).startOnBoot = isChecked
    }

}