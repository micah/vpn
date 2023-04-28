package com.example.torwitharti.vpn

import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.torwitharti.utils.isRunningOnMainThread
import org.torproject.onionmasq.logging.LogObservable
import java.util.concurrent.atomic.AtomicBoolean

enum class ConnectionState {
    INIT /*shows 'connect'*/, CONNECTING /*shows 'pause'*/, PAUSED, CONNECTED, DISCONNECTING, DISCONNECTED, CONNECTION_ERROR
}

object VpnStatusObservable {

    val TAG: String = VpnStatusObservable::class.java.simpleName

    private var _statusLiveData: MutableLiveData<ConnectionState> = MutableLiveData(ConnectionState.INIT)
    val statusLiveData: LiveData<ConnectionState> = _statusLiveData
    private val _dataUsage: MutableLiveData<DataUsage> = MutableLiveData(DataUsage())
    val dataUsage: LiveData<DataUsage> = _dataUsage
    private var startTime = 0L

    var isAlwaysOnBooting = AtomicBoolean(false)

    fun update(status: ConnectionState) {
        Log.d(TAG, "status update: $status")
        if (status == ConnectionState.CONNECTED) {
            startTime = SystemClock.elapsedRealtime()

        } else if (status == ConnectionState.DISCONNECTED || status == ConnectionState.CONNECTION_ERROR) {
            startTime = 0L
        }
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

    fun getStartTimeBase() = startTime
    fun resetDataUsage() {
        if (isRunningOnMainThread()) {
            _dataUsage.setValue(DataUsage())
        } else {
            _dataUsage.postValue(DataUsage())
        }
    }

}

