package com.example.torwitharti.vpn

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.torproject.onionmasq.logging.LogObservable
import com.example.torwitharti.utils.isRunningOnMainThread

enum class ConnectionState {
    INIT /*shows 'connect'*/, CONNECTING /*shows 'pause'*/, PAUSED, CONNECTED, DISCONNECTING, DISCONNECTED, CONNECTION_ERROR
}

object VpnStatusObservable {

    val TAG: String = VpnStatusObservable::class.java.simpleName

    private var _statusLiveData: MutableLiveData<ConnectionState> = MutableLiveData(ConnectionState.INIT)
    val statusLiveData: LiveData<ConnectionState> = _statusLiveData

    fun update(status: ConnectionState) {
        Log.d(TAG, "status update: $status")
        if (isRunningOnMainThread()) {
            _statusLiveData.setValue(status)
        } else {
            _statusLiveData.postValue(status)
        }
        LogObservable.getInstance().addLog(status.toString())
    }
}

