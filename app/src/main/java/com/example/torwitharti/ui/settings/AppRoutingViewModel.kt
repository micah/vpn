package com.example.torwitharti.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.torwitharti.utils.AppManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppRoutingViewModel(application: Application) : AndroidViewModel(application) {
    private val torAppList: MutableLiveData<List<AppItemModel>> = MutableLiveData<List<AppItemModel>>()
    private val appList: MutableLiveData<List<AppItemModel>> = MutableLiveData<List<AppItemModel>>()
    private val isLoadingAppList = MutableLiveData<Boolean>()
    private val isLoadingTorAppsList = MutableLiveData<Boolean>()

    private val appManager: AppManager

    init {
        appManager = AppManager(application)
        loadApps()
    }

    fun loadApps() {
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

        var mutableList = getAppList().toMutableList()
        mutableList[pos] = model
        appList.postValue(mutableList)
    }

    private fun persistProtectedApp(model: AppItemModel) {
        var protectedApps = appManager.preferenceHelper.protectedApps?.toMutableSet()
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

    fun isLoading(): LiveData<Boolean> {
        return isLoadingAppList
    }

}