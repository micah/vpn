package com.example.torwitharti.ui.configure.model

import android.app.Application
import android.util.EventLog
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.torwitharti.ui.configure.ConfigureFragment
import kotlinx.coroutines.flow.MutableStateFlow

class ConfigureFragmentViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        val TAG = ConfigureFragmentViewModel::class.java.simpleName
    }
    // Currently we don't hold states in ConfigureFragment, so this remains a stub for later
}