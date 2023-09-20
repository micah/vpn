package org.torproject.vpn.ui.bridgesettings.model

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.flow.*
import org.torproject.vpn.R
import org.torproject.vpn.utils.PreferenceHelper
import org.torproject.vpn.utils.PreferenceHelper.Companion.BridgeType

class BridgeDialogFragmentViewModel(private val application: Application) : AndroidViewModel(application) {

    val preferenceHelper = PreferenceHelper(application)

    private val _bridgeLines: MutableLiveData<List<String>> = MutableLiveData(preferenceHelper.bridgeLines.toList())
    val bridgeLines: LiveData<List<String>> get() = _bridgeLines

    val helperText: StateFlow<String> = bridgeLines.asFlow().map { bridgeLines ->
        if (bridgeLines.isEmpty()) {
            return@map application.resources.getString(R.string.action_paste_bridges)
        }
        return@map application.resources.getQuantityString(R.plurals.n_bridges, bridgeLines.size, bridgeLines.size)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = ""
    )

    fun addBridgeLine(value: String) {
        val list = _bridgeLines.value?.toMutableList() ?: emptyList<String>().toMutableList()
        list.add(value)
        _bridgeLines.value = list
    }

    fun removeBridgeLine(value: String) {
        val list = _bridgeLines.value?.toMutableList() ?: emptyList<String>().toMutableList()
        list.remove(value)
        _bridgeLines.value = list
    }

    fun save() {
        val bridgeLineSet = _bridgeLines.value?.toSet() ?: emptySet()
        preferenceHelper.bridgeLines = bridgeLineSet
        if (bridgeLineSet.isEmpty()) {
            preferenceHelper.bridgeType = BridgeType.None
        } else {
            preferenceHelper.bridgeType = BridgeType.Manual
        }
    }

}