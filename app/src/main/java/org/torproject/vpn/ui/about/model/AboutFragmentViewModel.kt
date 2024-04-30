package org.torproject.vpn.ui.about.model

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.SharingStarted.Companion.Lazily
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.torproject.vpn.BuildConfig
import org.torproject.vpn.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AboutFragmentViewModel(application: Application) : AndroidViewModel(application) {

    var packageManager: PackageManager = application.packageManager

    private val _applicationInfo = MutableLiveData(packageManager.getApplicationInfo(BuildConfig.APPLICATION_ID, 0))
    private val _packageInfo = MutableLiveData(packageManager.getPackageInfo(BuildConfig.APPLICATION_ID, 0))

    val version = BuildConfig.VERSION_NAME
    val appId = BuildConfig.APPLICATION_ID

    val appName = _applicationInfo.asFlow().map { info ->
        return@map info.loadLabel(packageManager)
    }.stateIn(viewModelScope, Eagerly, null)

    val update: StateFlow<String> = _packageInfo.asFlow().map { info ->
        val sdf = SimpleDateFormat("MMM d", Locale.ENGLISH /*, application.resources.getConfiguration().locales.get(0)*/)
        return@map sdf.format(Date(info.lastUpdateTime))
    }.stateIn(viewModelScope, Lazily, application.getString(R.string.unknown))

}