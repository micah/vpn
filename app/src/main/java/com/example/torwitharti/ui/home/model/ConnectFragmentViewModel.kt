package com.example.torwitharti.ui.home.model

import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.torwitharti.R
import com.example.torwitharti.ui.home.argIndex
import com.example.torwitharti.ui.home.argShowActionCommands
import com.example.torwitharti.utils.DummyConnectionState
import com.example.torwitharti.utils.PreferenceHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update


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
    private val _connectingProgress = MutableLiveData<Int>()
    private val _switchToErrorScene = MutableLiveData<Boolean>()
    private val _switchToErrorSceneExpanded = MutableLiveData<Boolean>()
//    private val _onAppsPressed = MutableLiveData<Unit>()
//    private val _switchToReportFrag = MutableLiveData<Unit>()

    private val _onAppsPressed =
        MutableStateFlow(NavigateFromConnectUiState(isSafeToNavigate = false))

    private val _switchToReportFrag =
        MutableStateFlow(NavigateFromConnectUiState(isSafeToNavigate = false))


    val showGuideTour: LiveData<Boolean> = _showGuideTour
    val switchToConnectingScene: LiveData<Boolean> = _switchToConnectingScene
    val mainActionButtonTitle: LiveData<String> = _mainActionButtonTitle
    val switchToIdleScene: LiveData<Boolean> = _switchToIdleScene
    val switchToConnectedScene: LiveData<Boolean> = _switchToConnectedScene
    val connectingProgress: LiveData<Int> = _connectingProgress
    val switchToErrorScene: LiveData<Boolean> = _switchToErrorScene
    val switchToErrorSceneExpanded: LiveData<Boolean> = _switchToErrorSceneExpanded
//    val onAppsPressed: LiveData<Unit> = _onAppsPressed
//    val switchToReportFrag: LiveData<Unit> = _switchToReportFrag

    val onAppsPressed: StateFlow<NavigateFromConnectUiState> = _onAppsPressed
    val switchToReportFrag: StateFlow<NavigateFromConnectUiState> = _switchToReportFrag

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
            DummyConnectionState.CONNECTION_FAILED -> attemptConnect()
            DummyConnectionState.DISCONNECTED -> {}
        }
    }

    fun globPressed() {
        _switchToReportFrag.update { it.copy(isSafeToNavigate = true) }
    }

    fun appsPressed() {
        //  some validation on when the navigation should be possible
        _onAppsPressed.update { it.copy(isSafeToNavigate = true) }
    }

    fun collapsedErrorClicked() {
        if (connectionState == DummyConnectionState.CONNECTION_FAILED) {
            _switchToErrorScene.value = false
            _switchToErrorSceneExpanded.value = true
        }
    }

    fun notNowInExpandedErrorClicked() {
        if (connectionState == DummyConnectionState.CONNECTION_FAILED) {
            _switchToErrorSceneExpanded.value = false
            _switchToErrorScene.value = true
        }
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
            if (connectionState == DummyConnectionState.CONNECTING) {

                connectionState = DummyConnectionState.CONNECTION_FAILED
                changeActionButtonTitle()
                _switchToConnectingScene.value = false
                //_switchToConnectedScene.value = true
                _switchToErrorScene.value = true
            }
        }, 2000)
    }

    private fun attemptDisconnect() {
        connectionState = DummyConnectionState.IDLE
        changeActionButtonTitle()
        _switchToIdleScene.value = true
        _switchToConnectedScene.value = false
    }

    private fun attemptCancelConnect() {
        connectionState = DummyConnectionState.IDLE
        changeActionButtonTitle()
        _switchToConnectingScene.value = false
        _switchToIdleScene.value = true
    }

    private fun changeActionButtonTitle() {
        when (connectionState) {
            DummyConnectionState.IDLE -> _mainActionButtonTitle.value =
                getApplication<Application>().getString(R.string.frag_connect_connect)

            DummyConnectionState.CONNECTING -> _mainActionButtonTitle.value =
                getApplication<Application>().getString(R.string.frag_connect_cancel)

            DummyConnectionState.CONNECTED -> _mainActionButtonTitle.value =
                getApplication<Application>().getString(R.string.frag_connect_disconnect)

            DummyConnectionState.CONNECTION_FAILED -> {
                _mainActionButtonTitle.value =
                    getApplication<Application>().getString(R.string.frag_connect_connect)
            }
            DummyConnectionState.DISCONNECTED -> {}
        }
    }

    fun appNavigationCompleted() {
        _onAppsPressed.update { it.copy(isSafeToNavigate = false) }
    }

    fun reportNavigationCompleted() {
        _switchToReportFrag.update { it.copy(isSafeToNavigate = false) }
    }

    data class NavigateFromConnectUiState(val isSafeToNavigate: Boolean)
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