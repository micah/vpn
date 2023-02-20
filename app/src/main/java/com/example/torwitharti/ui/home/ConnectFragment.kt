package com.example.torwitharti.ui.home

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.torwitharti.R
import com.example.torwitharti.StatusBarProgressInterface
import com.example.torwitharti.databinding.*
import com.example.torwitharti.ui.home.model.ConnectFragmentViewModel
import com.example.torwitharti.utils.DummyConnectionState2.*
import com.example.torwitharti.utils.connectionStateGradientAnimation
import com.example.torwitharti.utils.startVectorAnimationWithEndCallback
import kotlinx.coroutines.launch

class ConnectFragment : Fragment() {
    private lateinit var binding: FragmentConnectBinding
    private lateinit var connectFragmentViewModel: ConnectFragmentViewModel

    //this is required for transitions
    private lateinit var currentVpnState: ConnectFragmentViewModel.VpnConnectionUIState
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        connectFragmentViewModel = ViewModelProvider(this)[ConnectFragmentViewModel::class.java]

        binding = FragmentConnectBinding.inflate(inflater, container, false)
        binding.viewModel = connectFragmentViewModel

        lifecycleScope.launch {
            connectFragmentViewModel.vpnStatusFlow
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { vpnState ->
                    if (vpnState.isNavigationPending) {
                        Log.d(
                            "TAG",
                            "setUIState: ${vpnState.connectionState.name} ${vpnState.isNavigationPending}"
                        )
                        currentVpnState = vpnState
                        setUIState(vpnState)
                        connectFragmentViewModel.appNavigationCompleted()
                    }


                }
        }




        return binding.root
    }

    private fun setUIState(vpnState: ConnectFragmentViewModel.VpnConnectionUIState) {

        when (vpnState.connectionState) {
            INIT -> {
                binding.tvConnectActionBtn.setBackgroundResource(R.drawable.av_green_to_purple)
                binding.tvConnectActionBtn.setText(R.string.frag_connect_connect)
                binding.ivConnectBtnOverlay.visibility = View.GONE
            }
            CONNECTING -> {
                if (vpnState.animate) {
                    idleToConnectingTransition()

                } else {
                    binding.tvConnectActionBtn.setBackgroundResource(R.drawable.bg_connect_purple)
                }
            }
            PAUSED -> {}
            CONNECTED -> if (vpnState.animate) {

            } else {
                binding.tvConnectActionBtn.setBackgroundResource(R.drawable.bg_connect_red)
            }
            DISCONNECTED -> binding.tvConnectActionBtn.setBackgroundResource(R.drawable.bg_connect_green)
            CONNECTION_ERROR -> binding.tvConnectActionBtn.setBackgroundResource(R.drawable.bg_connect_green)

        }
        //This is responsible for status bar progress changes
        (requireActivity() as StatusBarProgressInterface).setStatus(vpnState.connectionState)
        currentVpnState = vpnState


    }


    /*
    * Scene transitions
    *
    * ************
     */

    private fun idleToConnectingTransition() {
        binding.ivConnectBtnOverlay.visibility = View.VISIBLE
        binding.tvConnectActionBtn.setBackgroundResource(R.drawable.av_green_to_purple)
        binding.ivConnectBtnOverlay.setImageResource(R.drawable.av_connect_to_pause)
        binding.tvConnectActionBtn.text = ""

        startVectorAnimationWithEndCallback(
            binding.tvConnectActionBtn.background,
            viewLifecycleOwner.lifecycle
        ) {}
        startVectorAnimationWithEndCallback(
            binding.ivConnectBtnOverlay.drawable,
            viewLifecycleOwner.lifecycle
        ) {}

        connectionStateGradientAnimation(
            binding.tvConnectActionBtn.background,
            requireContext(),
            intArrayOf(
                R.color.greenGradientLight,
                R.color.greenGradientNormal,
                R.color.greenGradientDark,
                R.color.purpleGradientLight,
                R.color.purpleGradientLight,
                R.color.purpleGradientDark
            )
        )


    }

    private fun connectingToIdleTransition() {

    }

    private fun connectingToConnectedTransition() {

    }

    private fun connectingToErrorTransition() {

    }

    private fun connectedToDisconnectedTransition() {

    }

    private fun showConnectedScene(show: Boolean) {
        if (show) {

        }
    }

    private fun showIdleScene(show: Boolean) {
        if (show) {
        }
    }

    private fun showCollapsedErrorInConnectScreen(show: Boolean) {
        if (show) {
        }
    }

    private fun showExpandedErrorInConnectScreen(show: Boolean) {
        if (show) {

        }
    }


}

