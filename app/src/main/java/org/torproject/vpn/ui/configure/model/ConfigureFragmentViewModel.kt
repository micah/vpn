package org.torproject.vpn.ui.configure.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class ConfigureFragmentViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        val TAG = ConfigureFragmentViewModel::class.java.simpleName
    }
    // Currently we don't hold states in ConfigureFragment, so this remains a stub for later
}