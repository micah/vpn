package com.example.torwitharti.ui.settings

import android.app.Application
import androidx.lifecycle.*
import com.example.torwitharti.utils.AppManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppRoutingViewModel(application: Application) : AndroidViewModel(application) {
    private val appList: MutableLiveData<List<AppItemModel>> = MutableLiveData<List<AppItemModel>>()
    private val isLoadingAppList = MutableLiveData<Boolean>()

    private val appManager: AppManager

    init {
        appManager = AppManager(application)
        loadApps()
    }

    private fun loadApps() {
        isLoadingAppList.postValue(true)
        viewModelScope.launch(Dispatchers.Default) {
            val apps = appManager.queryInstalledApps()
            withContext(Dispatchers.Main) {
                appList.postValue(apps)
                isLoadingAppList.postValue(false)
            }
        }
    }

    fun onItemModelChanged(pos: Int, model: AppItemModel) {
        persistProtectedApp(model)

        val mutableList = getAppList().toMutableList()
        mutableList[pos] = model
        appList.postValue(mutableList)
    }

    fun onProtectedAppsPrefsChanged(protectAllApps: Boolean) {
        val mutableList = getAppList().toMutableList()
        mutableList.forEach { it.protectAllApps = protectAllApps }
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

    fun getObservableAppList(): LiveData<List<AppItemModel>> {
        return appList
    }

    fun getAppList(): List<AppItemModel> {
        appList.value?.also {
            return it
        }
        return ArrayList()
    }

}