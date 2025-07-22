package org.torproject.vpn.ui.iconsetting.model

import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import android.util.Log
import android.widget.CompoundButton
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.torproject.vpn.utils.PreferenceHelper

class IconSettingFragmentViewModel(private val application: Application) : AndroidViewModel(application) {
    val preferenceHelper = PreferenceHelper(application)

    private val _launcherList = MutableLiveData<List<LauncherModel>>(mutableListOf())
    val launcherList: LiveData<List<LauncherModel>> = _launcherList

    val warningEnabled: Boolean get() = preferenceHelper.warningsEnabled

    init {
        loadLauncherList()
    }

    fun onWarningsSettingsChanged(compoundButton: CompoundButton, isChecked: Boolean) {
        preferenceHelper.warningsEnabled = isChecked
    }

    private fun loadLauncherList() {
        val list = LauncherSet.list
        val launcherClass = preferenceHelper.launcherClass
        list.forEach {
            it.selected = it.launcherClass == launcherClass
        }
        _launcherList.postValue(list)
    }

    fun onLauncherSelected(item: LauncherModel) {
        Log.d("appearance", ">>>>> save icon")
        preferenceHelper.launcherClass = item.launcherClass
        _launcherList.value?.let {
            it.forEach { appIconModel ->
                if (appIconModel.launcherClass == item.launcherClass) {
                    Log.d("appearance", ">>>>> enabling ${appIconModel.launcherClass}")

                    appIconModel.selected = true
                    application.packageManager.setComponentEnabledSetting(
                        ComponentName(
                            application,
                            appIconModel.launcherClass
                        ), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
                    )
                } else {
                    appIconModel.selected = false
                    Log.d("appearance", ">>>>> disabling ${appIconModel.launcherClass}")
                    application.packageManager.setComponentEnabledSetting(
                        ComponentName(
                            application,
                            appIconModel.launcherClass
                        ), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
                    )
                }
            }

            _launcherList.postValue(it)
        }
    }

}