package com.example.torwitharti.ui.home.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * VM, sorta, to wrap the boilerplate.
 */
class ReportTabViewModel(cw: ConnectedWith, directDuration: Long, obfs4Duration: Long, snowflakeDuration: Long) {
    val connectedWith: LiveData<ConnectedWith> = MutableLiveData(cw)
    val connectedTimingDirect: LiveData<String> = MutableLiveData(millisToHR(directDuration))
    val connectedTimingObfs4: LiveData<String> = MutableLiveData(millisToHR(obfs4Duration))
    val connectedTimingSnowFlake: LiveData<String> = MutableLiveData(millisToHR(snowflakeDuration))

    private fun millisToHR(millis: Long): String {
        val sec = millis / 1000
        val min = sec / 60
        val secRem = sec % 60

        return "${min}m ${secRem}s"
    }

}

enum class ConnectedWith {
    DIRECT, OBFS4, SNOWFLAKE
}