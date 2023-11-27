package org.torproject.vpn.ui.bridgesettings.model

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BridgeDialogViewModelFactory(val application: Application, private val loadFromPreferences: Boolean): ViewModelProvider.Factory {
    @Override
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BridgeDialogFragmentViewModel(application, loadFromPreferences) as T
    }
}