package org.torproject.vpn.ui.appdetail.model

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.CompoundButton
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.torproject.onionmasq.OnionMasq
import org.torproject.onionmasq.circuit.CircuitCountryCodes
import org.torproject.vpn.R
import org.torproject.vpn.ui.approuting.data.AppManager
import org.torproject.vpn.utils.PreferenceHelper
import org.torproject.vpn.utils.formatBitRate
import org.torproject.vpn.utils.formatBits
import org.torproject.vpn.utils.formatByteRateToBitRate
import org.torproject.vpn.utils.updateDataUsage
import org.torproject.vpn.vpn.DataUsage
import org.torproject.vpn.vpn.VpnServiceCommand
import org.torproject.vpn.vpn.VpnStatusObservable
import java.util.Timer
import java.util.TimerTask


class AppDetailFragmentViewModel(application: Application) : AndroidViewModel(application) {

    val appUID: MutableLiveData<Int?> = MutableLiveData()
    val appId = MutableLiveData("")
    val appName = MutableLiveData("")
    val isBrowser = MutableLiveData(false)
    val hasTorSupport = MutableLiveData(false)
    var packageManager: PackageManager? = application.packageManager
    private val appManager = AppManager(application)
    private var _circuitList: MutableLiveData<List<CircuitCountryCodes>> = MutableLiveData(ArrayList())
    val circuitList: LiveData<List<CircuitCountryCodes>> = _circuitList

    val openAppButtonText: StateFlow<String> = appName.asFlow().map { appName ->
        return@map application.getString(R.string.action_open_app, appName)
    }.stateIn(viewModelScope, Eagerly, "")

    val independentTorAppDescriptionText: StateFlow<String> = appName.asFlow().map { appName ->
        return@map application.getString(R.string.description_independent_tor_powered_app, appName)
    }.stateIn(viewModelScope, Eagerly, "")

    private val _dataUsage: MutableStateFlow<DataUsage> = MutableStateFlow(DataUsage())
    val dataUsage: StateFlow<DataUsage> = _dataUsage

    val dataUsageDownstreamRate: StateFlow<String> = dataUsage.map { data ->
	return@map formatByteRateToBitRate(data.downstreamDataPerSec)
    }.stateIn(viewModelScope, Eagerly, formatBitRate(0))
    
    val dataUsageUpstreamRate: StateFlow<String> = dataUsage.map { data ->
        return@map formatByteRateToBitRate(data.upstreamDataPerSec)
    }.stateIn(viewModelScope, Eagerly, formatBitRate(0))

    val dataUsageDownstream: StateFlow<String> = dataUsage.map { data ->
        return@map formatBits(data.downstreamData)
    }.stateIn(viewModelScope, Eagerly, formatBits(0))

    val dataUsageUpstream: StateFlow<String> = dataUsage.map { data ->
        return@map formatBits(data.upstreamData)
    }.stateIn(viewModelScope, Eagerly, formatBits(0))

    private val preferenceHelper = PreferenceHelper(getApplication())

    val protectThisApp: Boolean get() {
        val apps = preferenceHelper.protectedApps
        return apps.contains(appId.value)
    }
    fun onProtectThisAppChanged(compoundButton: CompoundButton, isChecked: Boolean) {
        appManager.onAppIdChanged(isChecked, appId.value)
    }


    private val timer: Timer by lazy {
        Timer()
    }

    init {
        viewModelScope.launch {
            timer.schedule(object: TimerTask() {
                override fun run() {
                    appUID.value?.let { id ->
                        _dataUsage.update { updateDataUsage(dataUsage, OnionMasq.getBytesReceivedForApp(id.toLong()), OnionMasq.getBytesSentForApp(id.toLong())) }
                        _circuitList.postValue(OnionMasq.getCircuitCountryCodesForAppUid(id))
                    }
                }
            }, 0, 1000)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
        packageManager = null
    }

    fun onOpenAppClicked() {
        appId.value?.let {
            val launchIntent: Intent? = packageManager?.getLaunchIntentForPackage(it)
            launchIntent?.let { intent ->
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(getApplication(), intent, null)
            } ?: kotlin.run {
                Toast.makeText(getApplication(), "Couldn't find launcher activity for ${appName.value}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun updateVPNSettings() {
        if (VpnStatusObservable.isVPNActive()) {
            // update VpnService settings via restart
            VpnServiceCommand.startVpn(getApplication())
        }
    }

}
