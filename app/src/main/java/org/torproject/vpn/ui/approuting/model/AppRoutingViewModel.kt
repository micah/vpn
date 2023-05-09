package org.torproject.vpn.ui.approuting.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.torproject.vpn.ui.approuting.data.AppManager
import org.torproject.vpn.vpn.VpnServiceCommand
import org.torproject.vpn.vpn.VpnStatusObservable

class AppRoutingViewModel(application: Application) : AndroidViewModel(application) {
    private val appList: MutableLiveData<List<AppItemModel>> = MutableLiveData<List<AppItemModel>>()
    private val isLoadingAppList = MutableLiveData<Boolean>()

    private val appManager: AppManager

    init {
        appManager = AppManager(application)
        loadApps()
    }

    private fun loadApps() {
        val cachedViewModels = appManager.loadCachedApps()

        appList.postValue(cachedViewModels)
        if (cachedViewModels.isEmpty()) {
            isLoadingAppList.postValue(true)
        }

        viewModelScope.launch(Dispatchers.Default) {
            val apps = appManager.queryInstalledApps()
            withContext(Dispatchers.Main) {
                appList.postValue(apps)
                isLoadingAppList.postValue(false)
            }
        }
    }

    fun onItemModelChanged(pos: Int, model: AppItemModel) {
        val mutableList = getAppList().toMutableList()
        mutableList[pos] = model
        persistProtectedApp(model)
        appManager.preferenceHelper.cachedApps = Gson().toJson(mutableList)
        appList.postValue(mutableList)
    }

    private fun persistProtectedApp(model: AppItemModel) {
        val protectedApps = appManager.preferenceHelper.protectedApps?.toMutableSet()
        if (model.isRoutingEnabled == true) {
            protectedApps?.add(model.appId)
        } else {
            protectedApps?.remove(model.appId)
        }
        appManager.preferenceHelper.protectedApps = protectedApps
    }

    fun onProtectAllAppsPrefsChanged(protectAllApps: Boolean) {
        val mutableList = getAppList().toMutableList()
        mutableList.onEach {
            it.protectAllApps = protectAllApps
        }
        appManager.preferenceHelper.cachedApps = Gson().toJson(mutableList)
        appList.postValue(mutableList)
    }

    fun updateVPNSettings() {
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

    fun getAppList(): List<AppItemModel> {
        appList.value?.also {
            return it
        }
        return ArrayList()
    }

}