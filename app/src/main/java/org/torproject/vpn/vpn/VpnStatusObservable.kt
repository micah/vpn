package org.torproject.vpn.vpn

import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.torproject.onionmasq.circuit.Circuit
import org.torproject.onionmasq.circuit.CircuitStore
import org.torproject.onionmasq.events.OnionmasqEvent
import org.torproject.vpn.utils.isRunningOnMainThread
import org.torproject.onionmasq.logging.LogObservable
import org.torproject.vpn.utils.updateDataUsage
import java.util.concurrent.atomic.AtomicBoolean

enum class ConnectionState {
    INIT /*shows 'connect'*/, CONNECTING /*shows 'cancel'*/,CONNECTED, DISCONNECTING, DISCONNECTED, CONNECTION_ERROR
}

object VpnStatusObservable {

    val TAG: String = VpnStatusObservable::class.java.simpleName

    private var _statusLiveData: MutableLiveData<ConnectionState> = MutableLiveData(ConnectionState.INIT)
    val statusLiveData: LiveData<ConnectionState> = _statusLiveData
    private val _dataUsage: MutableLiveData<DataUsage> = MutableLiveData(DataUsage())
    val dataUsage: LiveData<DataUsage> = _dataUsage

    private val circuitStore: CircuitStore = CircuitStore()
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
        val updatedDataUsage = updateDataUsage(dataUsage, downstream, upstream)
        if (isRunningOnMainThread()) {
            _dataUsage.setValue(updatedDataUsage)
        } else {
            _dataUsage.postValue(updatedDataUsage)
        }
    }

    fun handleConnectionEvent(event: OnionmasqEvent) {
        circuitStore.handleEvent(event)
    }

    fun getCircuitsForUid(appUid: Int): ArrayList<Circuit> {
        return circuitStore.getCircuitsForAppUid(appUid)
    }

    fun getStartTimeBase() = startTime
    fun reset() {
        if (isRunningOnMainThread()) {
            _dataUsage.setValue(DataUsage())
        } else {
            _dataUsage.postValue(DataUsage())
        }
        circuitStore.reset()
    }

    fun isVPNActive(): Boolean {
        val status = _statusLiveData.value
        return status == ConnectionState.CONNECTING || status == ConnectionState.CONNECTED
    }

}

