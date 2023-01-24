package com.example.torwitharti.ui.home.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class ReportFragmentViewModel(application: Application) : AndroidViewModel(application) {
    val connectedWith = MutableLiveData<TransportType>()
    val connectedTimingDirect = MutableLiveData<String>()
    val connectedTimingObfs4 = MutableLiveData<String>()
    val connectedTimingSnowFlake = MutableLiveData<String>()

    fun setReportParams(cw: TransportType, directDuration: Long, obfs4Duration: Long, snowflakeDuration: Long){
        connectedWith.value = cw
        connectedTimingDirect.value = millisToHR(directDuration)
        connectedTimingObfs4.value = millisToHR(obfs4Duration)
        connectedTimingSnowFlake.value = millisToHR(snowflakeDuration)
    }


    private fun millisToHR(millis: Long): String {
        val sec = millis / 1000
        val min = sec / 60
        val secRem = sec % 60

        return "${min}m ${secRem}s"
    }

}

enum class TransportType {
    DIRECT, OBFS4, SNOWFLAKE
}