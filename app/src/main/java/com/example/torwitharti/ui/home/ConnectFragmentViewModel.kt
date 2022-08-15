package com.example.torwitharti.ui.home

import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.torwitharti.R
import com.example.torwitharti.utils.DummyConnectionState
import com.example.torwitharti.utils.PreferenceHelper


/**
 * ViewModel for slider fragment, mostly place holder at this point
 */


class ConnectFragmentViewModel(application: Application) : AndroidViewModel(application) {
    private val preferenceHelper = PreferenceHelper(application)
    private val _showGuideTour = MutableLiveData<Boolean>()
    private val _switchToConnectingScene = MutableLiveData<Boolean>()
    private val _mainActionButtonTitle = MutableLiveData<String>()
    private val _switchToIdleScene = MutableLiveData<Boolean>()
    private val _switchToConnectedScene = MutableLiveData<Boolean>()

    val showGuideTour: LiveData<Boolean> = _showGuideTour
    val switchToConnectingScene: LiveData<Boolean> = _switchToConnectingScene
    val mainActionButtonTitle: LiveData<String> = _mainActionButtonTitle
    val switchToIdleScene: LiveData<Boolean> = _switchToIdleScene
    val switchToConnectedScene: LiveData<Boolean> = _switchToIdleScene

    /*
    * dummy flags
    ************
     */
    private var connectionState = DummyConnectionState.IDLE

    init {
        checkIfGuideNeedsToBeShown()
        changeActionButtonTitle()
    }

    fun guideCompleteActionClicked() {
        _showGuideTour.value = false
    }

    fun mainActionButtonClicked() {
        when (connectionState) {
            DummyConnectionState.IDLE -> attemptConnect()
            DummyConnectionState.CONNECTING -> attemptCancelConnect()
            DummyConnectionState.CONNECTED -> attemptDisconnect()
            DummyConnectionState.CONNECTION_FAILED -> {}
            DummyConnectionState.DISCONNECTED -> {}
        }
    }

    fun globPressed() {

    }

    fun appsPressed() {

    }


    private fun checkIfGuideNeedsToBeShown() {
        if (preferenceHelper.shouldShowGuide) {
            //TODO commented for now so guide is visible all the time
            //preferenceHelper.shouldShowGuide = false
            Handler(Looper.getMainLooper()).postDelayed({ _showGuideTour.value = true }, 500)

        }
    }


    private fun attemptConnect() {
        connectionState = DummyConnectionState.CONNECTING
        changeActionButtonTitle()
        _switchToConnectingScene.value = true

        //TODO dummy success trigger
        Handler(Looper.getMainLooper()).postDelayed({
            connectionState = DummyConnectionState.CONNECTING
            changeActionButtonTitle()
            _switchToConnectingScene.value = false
            _switchToConnectedScene.value = true
        }, 3000)
    }

    private fun attemptDisconnect() {

    }

    private fun attemptCancelConnect() {
        connectionState = DummyConnectionState.IDLE
        changeActionButtonTitle()
        _switchToIdleScene.value = true
    }

    private fun changeActionButtonTitle(){
        when (connectionState) {
            DummyConnectionState.IDLE -> _mainActionButtonTitle.value =
                getApplication<Application>().getString(R.string.frag_connect_connect)

            DummyConnectionState.CONNECTING -> _mainActionButtonTitle.value =
                getApplication<Application>().getString(R.string.frag_connect_cancel)

            DummyConnectionState.CONNECTED -> _mainActionButtonTitle.value =
                getApplication<Application>().getString(R.string.frag_connect_disconnect)

            DummyConnectionState.CONNECTION_FAILED -> {}
            DummyConnectionState.DISCONNECTED -> {}
        }
    }
}

class GuideFrameVP2ViewModel(application: Application) : AndroidViewModel(application) {
    private lateinit var connectFragmentViewModel: ConnectFragmentViewModel
    private val _frameIndex = MutableLiveData<String>()

    private val _showAction = MutableLiveData<Boolean>()
    val showAction: LiveData<Boolean> = _showAction
    val frameIndex: LiveData<String> = _frameIndex

    fun setArgs(arguments: Bundle?) {
        _frameIndex.value = getApplication<Application>().getString(
            R.string.frag_connect_guide_slide_index, arguments!!.getInt(argIndex)
        )
        _showAction.value = arguments.getBoolean(argShowActionCommands)
    }

    fun setConnectFragmentViewModel(connectFragmentViewModel: ConnectFragmentViewModel) {
        this.connectFragmentViewModel = connectFragmentViewModel
    }

    fun onGotItClicked() {
        connectFragmentViewModel.guideCompleteActionClicked()
    }


}