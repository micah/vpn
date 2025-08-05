package org.torproject.vpn.ui.approuting.model

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.torproject.vpn.ui.approuting.data.AppListAdapter.Companion.CELL
import org.torproject.vpn.ui.approuting.data.AppManager
import org.torproject.vpn.utils.PreferenceHelper.Companion.CACHED_APPS
import org.torproject.vpn.utils.PreferenceHelper.Companion.PROTECTED_APPS
import org.torproject.vpn.utils.PreferenceHelper.Companion.PROTECT_ALL_APPS
import org.torproject.vpn.vpn.VpnServiceCommand
import org.torproject.vpn.vpn.VpnStatusObservable

class AppRoutingViewModel(application: Application) : AndroidViewModel(application) {
    private val appList: MutableLiveData<List<AppItemModel>> = MutableLiveData<List<AppItemModel>>()
    private val isLoadingAppList = MutableLiveData<Boolean>()


    private val appManager: AppManager = AppManager(application)

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
             PROTECTED_APPS -> {
                 reloadApps()
                 updateVPNSettings()
             }
            CACHED_APPS -> loadCachedApps()
        }
    }


    val enableAllBridges = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (PROTECT_ALL_APPS == changedKey) {
                trySend(appManager.preferenceHelper.protectAllApps)
            }
        }
        appManager.preferenceHelper.registerListener(listener)
        awaitClose { appManager.preferenceHelper.unregisterListener(listener) }
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        appManager.preferenceHelper.protectAllApps
    )


    init {
        loadApps()
        appManager.preferenceHelper.registerListener(preferenceChangeListener)
    }

    override fun onCleared() {
        super.onCleared()
        appManager.preferenceHelper.unregisterListener(preferenceChangeListener)
    }

    private fun loadCachedApps() {
        val cachedViewModels = appManager.loadCachedApps()
        appList.postValue(cachedViewModels)
    }

    private fun loadApps() {
        val cachedViewModels = appManager.loadCachedApps()

        appList.postValue(cachedViewModels)
        if (cachedViewModels.isEmpty()) {
            isLoadingAppList.postValue(true)
        }

        viewModelScope.launch(Dispatchers.IO) {
            val apps = appManager.queryInstalledApps()
            val appIds = apps.toList().mapNotNull { it.appId }
            val appListIds = appList.value.orEmpty().toList().map { it.appId }
            if (appIds.minus(appListIds).isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    appList.postValue(apps)
                    isLoadingAppList.postValue(false)
                }
            } else {
                withContext(Dispatchers.Main) {
                    isLoadingAppList.postValue(false)
                }
            }
        }
    }

    private fun reloadApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = appManager.queryInstalledApps()
            withContext(Dispatchers.Main) {
                appList.postValue(apps)
            }
        }
    }

    fun onItemModelChanged(pos: Int, model: AppItemModel) {
        val mutableList = getAppList().toMutableList()
        mutableList[pos] = model.copy()
        val protectedApps = persistProtectedApp(model)
        appManager.preferenceHelper.cachedApps = Gson().toJson(mutableList)
        appManager.preferenceHelper.protectAllApps = mutableList.filter { it.viewType == CELL }.size == protectedApps.size
        appList.postValue(mutableList)
    }

    private fun persistProtectedApp(model: AppItemModel): MutableSet<String>{
        val protectedApps = appManager.preferenceHelper.protectedApps
        if (model.isRoutingEnabled == true) {
            model.appId?.let { protectedApps.add(it) }
        } else {
            protectedApps.remove(model.appId)
        }
        appManager.preferenceHelper.protectedApps = protectedApps
        return protectedApps
    }

    fun onProtectAllAppsPrefsChanged(protectAllApps: Boolean) {
        val mutableList = getAppList().toMutableList()
        val protectedApps = emptySet<String>().toMutableSet()
        mutableList.onEach {
            it.isRoutingEnabled = protectAllApps
            if (protectAllApps) {
                it.appId?.let { appId ->
                    protectedApps.add(appId)
                }
            }
        }
        appManager.preferenceHelper.protectedApps = protectedApps
        appManager.preferenceHelper.protectAllApps = protectAllApps
        appManager.preferenceHelper.cachedApps = Gson().toJson(mutableList)
        appList.postValue(mutableList)
    }

    private fun updateVPNSettings() {
        if (VpnStatusObservable.isVPNActive()) {
            // update VpnService settings via restart
            VpnServiceCommand.startVpn(getApplication())
        }
    }

    fun getObservableAppList(): LiveData<List<AppItemModel>> {
        return appList
    }

    fun getObservableProgress(): LiveData<Boolean> {
        return isLoadingAppList
    }

    private fun getAppList(): List<AppItemModel> {
        appList.value?.also {
            return it
        }
        return ArrayList()
    }

}