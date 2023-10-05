package org.torproject.vpn.ui.appdetail.model

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.text.format.Formatter
import android.widget.CompoundButton
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.*
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.torproject.onionmasq.OnionMasq
import org.torproject.onionmasq.circuit.Circuit
import org.torproject.vpn.R
import org.torproject.vpn.utils.PreferenceHelper
import org.torproject.vpn.utils.updateDataUsage
import org.torproject.vpn.vpn.DataUsage
import org.torproject.vpn.vpn.VpnStatusObservable
import java.util.*


class AppDetailFragmentViewModel(application: Application) : AndroidViewModel(application) {

    val appUID: MutableLiveData<Int?> = MutableLiveData()
    val appId = MutableLiveData("")
    val appName = MutableLiveData("")
    val isBrowser = MutableLiveData(false)
    val hasTorSupport = MutableLiveData(false)
    var packageManager: PackageManager? = application.packageManager
    private var _circuitList: MutableLiveData<List<Circuit>> = MutableLiveData(ArrayList())
    val circuitList: LiveData<List<Circuit>> = _circuitList

    val openAppButtonText: StateFlow<String> = appName.asFlow().map { appName ->
        return@map application.getString(R.string.action_open_app, appName)
    }.stateIn(viewModelScope, Eagerly, "")

    val independentTorAppDescriptionText: StateFlow<String> = appName.asFlow().map { appName ->
        return@map application.getString(R.string.description_independent_tor_powered_app, appName)
    }.stateIn(viewModelScope, Eagerly, "")

    private val _dataUsage: MutableLiveData<DataUsage> = MutableLiveData(DataUsage())
    val dataUsage: LiveData<DataUsage> = _dataUsage

    val dataUsageDownstream: StateFlow<String> = dataUsage.asFlow().map { data ->
        return@map Formatter.formatFileSize(application, data.downstreamData)
    }.stateIn(viewModelScope, Eagerly, Formatter.formatFileSize(application, 0))

    val dataUsageUpstream: StateFlow<String> = dataUsage.asFlow().map { data ->
        return@map Formatter.formatFileSize(application, data.upstreamData)
    }.stateIn(viewModelScope, Eagerly, Formatter.formatFileSize(application, 0))

    private val preferenceHelper = PreferenceHelper(getApplication())

    val protectThisApp: Boolean get() {
        val apps = preferenceHelper.protectedApps?.toSet() ?: emptySet()
        return apps.contains(appId.value)
    }
    fun onProtectThisAppChanged(compoundButton: CompoundButton, isChecked: Boolean) {
        val protectedApps = preferenceHelper.protectedApps?.toMutableSet() ?: emptySet<String>().toMutableSet()
        if (isChecked) {
            protectedApps.add(appId.value)
        } else {
            protectedApps.remove(appId.value)
        }
        preferenceHelper.protectedApps = protectedApps
    }

    private val timer: Timer by lazy {
        Timer()
    }

    init {
        viewModelScope.launch {
            timer.schedule(object: TimerTask() {
                override fun run() {
                    appUID.value?.let {
                        _dataUsage.postValue(updateDataUsage(dataUsage, OnionMasq.getBytesReceivedForApp(it.toLong()), OnionMasq.getBytesSentForApp(it.toLong())))
                        _circuitList.postValue(VpnStatusObservable.getCircuitsForUid(it))
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

}