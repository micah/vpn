package com.example.torwitharti.ui.home.model

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.torwitharti.R
import com.example.torwitharti.TorApplication
import com.example.torwitharti.utils.DummyConnectionState
import com.example.torwitharti.utils.DummyConnectionState2
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update


/**
 * ViewModel for slider fragment, mostly place holder at this point
 */


class ConnectFragmentViewModel(application: Application) : AndroidViewModel(application) {
    private val _vpnStatusFlow =
        MutableStateFlow(VpnConnectionUIState(
            isNavigationPending = true,
            connectionState = DummyConnectionState2.INIT,
            animate = false
        ))
    private val _showGuideSlide = MutableLiveData<Boolean>(true)


    val vpnStatusFlow: StateFlow<VpnConnectionUIState> = _vpnStatusFlow
    val showGuideSlide: LiveData<Boolean> = _showGuideSlide

    /*
    * dummy flags
    ************
     */


    fun connectStateButtonClicked() {
        when (vpnStatusFlow.value.connectionState) {
            DummyConnectionState2.INIT -> attemptConnect()
            DummyConnectionState2.CONNECTING -> attemptPause()
            DummyConnectionState2.PAUSED -> attemptConnect()
            DummyConnectionState2.CONNECTED -> attemptDisconnect()
            DummyConnectionState2.DISCONNECTED -> attemptConnect()
            DummyConnectionState2.CONNECTION_ERROR -> attemptConnect()
        }
    }

    private fun attemptPause() {
        _vpnStatusFlow.update {
            it.copy(
                connectionState = DummyConnectionState2.PAUSED,
                animate = true,
                isNavigationPending = true
            )
        }
    }

    private fun attemptConnect() {
        _vpnStatusFlow.update {
            it.copy(
                connectionState = DummyConnectionState2.CONNECTING,
                animate = true,
                isNavigationPending = true
            )
        }
        Handler(Looper.getMainLooper()).postDelayed({
            if (vpnStatusFlow.value.connectionState == DummyConnectionState2.CONNECTING) {
                _vpnStatusFlow.update {
                    it.copy(
                        connectionState = DummyConnectionState2.CONNECTED,
                        animate = true,
                        isNavigationPending = true
                    )
                }
            }
        }, 6000)

    }

    private fun attemptDisconnect() {
        _vpnStatusFlow.update {
            it.copy(
                connectionState = DummyConnectionState2.DISCONNECTED,
                animate = true,
                isNavigationPending = true
            )
        }
    }

    private fun attemptCancelConnect() {
        //TODO
    }

    fun appNavigationCompleted() {
        _vpnStatusFlow.update { it.copy(isNavigationPending = false) }
    }

    data class VpnConnectionUIState(
        val connectionState: DummyConnectionState2,
        val isNavigationPending: Boolean,
        val animate: Boolean
    )
}
