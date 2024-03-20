package org.torproject.vpn.ui.bridgebot.model

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.torproject.vpn.R
import org.torproject.vpn.circumvention.CircumventionApiManager
import org.torproject.vpn.circumvention.SettingsRequest
import org.torproject.vpn.ui.bridgebot.BridgeBotFragment
import org.torproject.vpn.utils.PreferenceHelper

class BridgeBotFragmentViewModel(application: Application) : AndroidViewModel(application) {

    enum class BotState {
        INIT,
        FETCHING,
        SHOW_RESULT,
        SAVED_BRIDGES
    }

    private val TAG: String = BridgeBotFragmentViewModel::class.java.simpleName
    val preferenceHelper = PreferenceHelper(application)

    private var _botState: MutableLiveData<BotState> = MutableLiveData(BotState.INIT)
    val botState: LiveData<BotState> = _botState

    private var _bridgeResult: MutableLiveData<List<String>> = MutableLiveData(emptyList())
    val bridgeResult: LiveData<List<String>> = _bridgeResult

    val bridgeButtonText: StateFlow<String> = botState.asFlow().map { data ->
        return@map when (data) {
            BotState.SHOW_RESULT -> application.getString(R.string.start_again)
            else -> application.getString(R.string.new_bridges)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = application.applicationContext.getString(R.string.new_bridges)
    )

    val bridgeButtonTextColor: StateFlow<Int> = botState.asFlow().map { data ->
        return@map when (data) {
            BotState.FETCHING -> ContextCompat.getColor(application, R.color.inverse_on_surface)
            else -> ContextCompat.getColor(application, R.color.on_surface)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = ContextCompat.getColor(application, R.color.on_surface)
    )

    val isFetching: StateFlow<Boolean> = botState.asFlow().map { data ->
        return@map when (data) {
            BotState.FETCHING -> true
            else -> false
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = true
    )

    val botMessage: StateFlow<String> = botState.asFlow().map { data ->
        return@map when (data) {
            BotState.INIT, BotState.SAVED_BRIDGES -> application.getString(R.string.bot_msg_welcome)
            BotState.FETCHING -> application.getString(R.string.fetching_bridges)
            BotState.SHOW_RESULT -> application.getString(R.string.bot_msg_results)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = application.applicationContext.getString(R.string.new_bridges)
    )

    /*
        This functionality is still very alpha. We're requesting bridge lines from the circumvention API,
        but instead of evaluating the assumed device's country location, the country code of RU is hard coded
        in order to enforce to receive actual results. For requested (presumably) uncensored locations an
        empty array gets returned by the circumvention API. Moreover this feature doesn't use any
        circumvention strategy itself for the API communication. Finally the returned bridge lines cannot be
        used in reality to start tor, since PT support is not yet available. Only bridges without obfuscation
        are currently supported.
     */
    fun fetchBridges() {
        if (botState.value == BotState.FETCHING) {
            return
        }
        _botState.postValue(BotState.FETCHING)

        CircumventionApiManager().getSettings(SettingsRequest("ru"), {
            it?.let { response ->
                Log.d("result: ", "${response.settings}")
                val results = mutableListOf<String>()
                response.settings?.let { bridgesList ->
                    for (bridges in bridgesList) {
                        bridges.bridges.bridge_strings?.let { bridgeLines ->
                            results.addAll(bridgeLines)
                        }
                    }
                }
                _bridgeResult.postValue(results)
                _botState.postValue(BotState.SHOW_RESULT)
            } ?: run {
                _botState.postValue(BotState.INIT)
            }
        }, {
            Log.e(BridgeBotFragment.TAG, "Ask Tor was not available... $it")
            _botState.postValue(BotState.INIT)
            Toast.makeText(getApplication(),"Ask Tor was not available", Toast.LENGTH_LONG).show()
        })
    }

    fun useBridges() {
        val bridgeLineSet = bridgeResult.value?.toSet() ?: emptySet()
        preferenceHelper.bridgeLines = bridgeLineSet
        if (bridgeLineSet.isEmpty()) {
            preferenceHelper.bridgeType = PreferenceHelper.Companion.BridgeType.None
        } else {
            //TODO: figure out if asking the bridge bot should result in BridgeType.Manual, if we need to introduce another state etc.
            preferenceHelper.bridgeType = PreferenceHelper.Companion.BridgeType.Manual
        }
        _botState.postValue(BotState.SAVED_BRIDGES)
    }
}