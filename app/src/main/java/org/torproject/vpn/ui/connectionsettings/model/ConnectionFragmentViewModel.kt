package org.torproject.vpn.ui.connectionsettings.model

import android.animation.ValueAnimator
import android.app.Application
import android.content.SharedPreferences
import android.view.View.GONE
import android.view.View.VISIBLE
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
import org.torproject.vpn.utils.PreferenceHelper
import org.torproject.vpn.utils.PreferenceHelper.Companion.BridgeType
import org.torproject.vpn.utils.getDpInPx

class ConnectionFragmentViewModel(private val application: Application) : AndroidViewModel(application) {

    val preferenceHelper = PreferenceHelper(application)

    val startOnBoot: Boolean get() = preferenceHelper.startOnBoot
    private val bridgeSettingsContainerMaxHeight = getDpInPx(application, 84f)
    private var _bridgeSettingsContainerHeight = MutableLiveData(0)
    private var _bridgeSettingsContainerVisibility = MutableLiveData(GONE)
    private var _bridgeSettingsContainerAlpha = MutableLiveData(0f)
    val bridgeSettingsContainerHeight = _bridgeSettingsContainerHeight as LiveData<Int>
    var bridgeSettingContainerVisibility = _bridgeSettingsContainerVisibility as LiveData<Int>
    var bridgeSettingsContainerAlpha = _bridgeSettingsContainerAlpha as LiveData<Float>

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
                trySend(getStringForBridgeType(preferenceHelper.bridgeType))
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

    fun animateBridgeSettingsContainerHeight(isSuperToggleChecked: Boolean) {
        val startHeight =
            if (isSuperToggleChecked) 0 else bridgeSettingsContainerMaxHeight
        val endHeight =
            if (isSuperToggleChecked) bridgeSettingsContainerMaxHeight else 0

        val heightAnimator: ValueAnimator = ValueAnimator.ofInt(startHeight, endHeight)
        heightAnimator.duration = 250
        heightAnimator.addUpdateListener { valueAnimator ->
            _bridgeSettingsContainerHeight.postValue(valueAnimator.animatedValue as Int)
            _bridgeSettingsContainerVisibility.postValue(if (valueAnimator.animatedValue as Int > 0) VISIBLE else GONE)
            _bridgeSettingsContainerAlpha.postValue((valueAnimator.animatedValue as Int).toFloat() / bridgeSettingsContainerMaxHeight.toFloat())
        }
        heightAnimator.start()
    }

    fun setBridgeSettingsContainerHeight() {
        if (useBridge.value == true) {
            _bridgeSettingsContainerHeight.postValue(bridgeSettingsContainerMaxHeight)
            _bridgeSettingsContainerVisibility.postValue(VISIBLE)
            _bridgeSettingsContainerAlpha.postValue(1f)
        } else {
            _bridgeSettingsContainerHeight.postValue(0)
            _bridgeSettingsContainerVisibility.postValue(GONE)
            _bridgeSettingsContainerAlpha.postValue(0f)
        }
    }

}