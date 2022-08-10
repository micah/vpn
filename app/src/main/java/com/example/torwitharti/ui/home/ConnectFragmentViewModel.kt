package com.example.torwitharti.ui.home

import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.torwitharti.R
import com.example.torwitharti.utils.PreferenceHelper

class ConnectFragmentViewModel(application: Application) : AndroidViewModel(application) {
    private val preferenceHelper = PreferenceHelper(application)
    private val _showGuideTour = MutableLiveData<Boolean>()
    val showGuideTour: LiveData<Boolean> = _showGuideTour


    init {
        checkIfGuideNeedsToBeShown()
    }


    private fun checkIfGuideNeedsToBeShown() {
        if (preferenceHelper.shouldShowGuide) {
            //TODO commented for now so guide is visible all the time
            //preferenceHelper.shouldShowGuide = false
            Handler(Looper.getMainLooper()).postDelayed({_showGuideTour.value = true}, 500)

        }
    }

    fun guideCompleteActionClicked(){
        _showGuideTour.value = false
    }

    //TODO
    fun connectPressed() {

    }

    fun globPressed() {

    }

    fun appsPressed() {

    }


}

/**
 * ViewModel for slider fragment, mostly place holder at this point
 */
class GuideFrameVP2ViewModel(application: Application) : AndroidViewModel(application) {
    private lateinit var connectFragmentViewModel:ConnectFragmentViewModel
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

    fun setConnectFragmentViewModel(connectFragmentViewModel: ConnectFragmentViewModel){
        this.connectFragmentViewModel = connectFragmentViewModel
    }

    fun onGotItClicked(){
        connectFragmentViewModel.guideCompleteActionClicked()
    }


}