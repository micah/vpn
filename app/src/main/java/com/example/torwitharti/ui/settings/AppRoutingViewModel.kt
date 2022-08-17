package com.example.torwitharti.ui.settings

import android.app.Application
import android.util.Pair
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.torwitharti.utils.AppManager
import com.example.torwitharti.utils.PreferenceHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppRoutingViewModel(application: Application) : AndroidViewModel(application) {
    private val appList: MutableLiveData<List<AppItemModel>> = MutableLiveData<List<AppItemModel>>()
    private val isLoading = MutableLiveData<Boolean>()

    private val appManager: AppManager

    init {
        appManager = AppManager()
        loadApps()
    }

    fun loadApps() {
        isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.Default) {
            val apps = appManager.queryInstalledApps()
            withContext(Dispatchers.Main) {
                appList.postValue(apps)
                isLoading.postValue(false)
            }
        }
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
        return isLoading
    }

}