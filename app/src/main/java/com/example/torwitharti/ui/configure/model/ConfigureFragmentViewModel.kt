package com.example.torwitharti.ui.configure.model

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.torwitharti.ui.configure.ConfigureFragment

class ConfigureFragmentViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        val TAG = ConfigureFragmentViewModel::class.java.simpleName
    }

    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text

    fun appsEntryClicked() {
        Log.d(TAG, "apps entry clicked")
    }

    fun torLogsClicked() {
        Toast.makeText(this.getApplication(), "tor logs clicked", Toast.LENGTH_SHORT).show()
    }
}