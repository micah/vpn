package com.example.torwitharti.vpn

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.torwitharti.utils.isRunningOnMainThread
import org.torproject.onionmasq.logging.LogObservable

enum class ConnectionState {
    INIT /*shows 'connect'*/, CONNECTING /*shows 'pause'*/, PAUSED, CONNECTED, DISCONNECTING, DISCONNECTED, CONNECTION_ERROR
}

object VpnStatusObservable {

    val TAG: String = VpnStatusObservable::class.java.simpleName

    private var _statusLiveData: MutableLiveData<ConnectionState> = MutableLiveData(ConnectionState.INIT)
    val statusLiveData: LiveData<ConnectionState> = _statusLiveData
    private val _dataUsage: MutableLiveData<DataUsage> = MutableLiveData(DataUsage())
    val dataUsage: LiveData<DataUsage> = _dataUsage

    fun update(status: ConnectionState) {
        Log.d(TAG, "status update: $status")
        if (isRunningOnMainThread()) {
            _statusLiveData.setValue(status)
        } else {
            _statusLiveData.postValue(status)
        }
        LogObservable.getInstance().addLog(status.toString())
    }

    fun updateDataUsage(downstream: Long, upstream: Long) {
        val lastDataUsage: DataUsage = dataUsage.value!!
        val updatedDataUsage = DataUsage()
        updatedDataUsage.downstreamData = downstream
        updatedDataUsage.upstreamData = upstream
        val timeDelta = Math.max((updatedDataUsage.timeStamp - lastDataUsage.timeStamp) / 1000, 1)
        updatedDataUsage.upstreamDataPerSec =
            (updatedDataUsage.upstreamData - lastDataUsage.upstreamData) / timeDelta
        updatedDataUsage.downstreamDataPerSec =
            (updatedDataUsage.downstreamData - lastDataUsage.downstreamData) / timeDelta
        if (isRunningOnMainThread()) {
            _dataUsage.setValue(updatedDataUsage)
        } else {
            _dataUsage.postValue(updatedDataUsage)
        }
    }

    fun resetDataUsage() {
        if (isRunningOnMainThread()) {
            _dataUsage.setValue(DataUsage())
        } else {
            _dataUsage.postValue(DataUsage())
        }
    }

}

