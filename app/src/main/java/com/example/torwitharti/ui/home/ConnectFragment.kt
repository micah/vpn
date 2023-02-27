package com.example.torwitharti.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Lifecycle
import com.example.torwitharti.R
import com.example.torwitharti.StatusBarProgressInterface
import com.example.torwitharti.databinding.*
import com.example.torwitharti.ui.home.model.ConnectFragmentViewModel
import com.example.torwitharti.utils.connectionStateGradientAnimation
import com.example.torwitharti.utils.startVectorAnimationWithEndCallback
import com.example.torwitharti.vpn.ConnectionState
import com.example.torwitharti.vpn.VpnServiceCommand
import com.example.torwitharti.vpn.VpnStatusObservable
import kotlinx.coroutines.launch

class ConnectFragment : Fragment() {
    companion object {
        val TAG: String = ConnectFragment::class.java.simpleName
    }
    private lateinit var binding: FragmentConnectBinding
    private lateinit var connectFragmentViewModel: ConnectFragmentViewModel

    //this is required for transitions
    private lateinit var currentVpnState: ConnectFragmentViewModel.VpnConnectionUIState

    private var startForResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            VpnServiceCommand.startVpn(context)
        } else {
            VpnStatusObservable.update(ConnectionState.CONNECTION_ERROR)
        }
        connectFragmentViewModel.onVpnPrepared()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        connectFragmentViewModel = ViewModelProvider(this)[ConnectFragmentViewModel::class.java]

        binding = FragmentConnectBinding.inflate(inflater, container, false)
        binding.viewModel = connectFragmentViewModel

        connectFragmentViewModel.prepareVpn.observe(viewLifecycleOwner) { intent ->
            if (intent != null) {
                startForResult.launch(intent)
            }
        }

        connectFragmentViewModel.connectionState.observe(viewLifecycleOwner) { state ->
            // nothing to see here.
            Log.d(TAG, "connection state observer: $state")
        }

        lifecycleScope.launch {
            connectFragmentViewModel.vpnStatusFlow
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { vpnState ->
                    Log.d(TAG, "flow connectionState $vpnState")
                    if (vpnState.isNavigationPending) {
                        Log.d(
                            TAG,
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
            ConnectionState.INIT -> {
                binding.tvConnectActionBtn.setBackgroundResource(R.drawable.av_green_to_purple)
                binding.tvConnectActionBtn.setText(R.string.frag_connect_connect)
                binding.ivConnectBtnOverlay.visibility = View.GONE
            }
            ConnectionState.CONNECTING -> {
                if (vpnState.animate) {
                    idleToConnectingTransition()

                } else {
                    binding.tvConnectActionBtn.setBackgroundResource(R.drawable.bg_connect_purple)
                }
            }
            ConnectionState.PAUSED -> {}
            ConnectionState.CONNECTED -> if (vpnState.animate) {

            } else {
                binding.tvConnectActionBtn.setBackgroundResource(R.drawable.bg_connect_red)
            }
            ConnectionState.DISCONNECTED -> {
                binding.tvConnectActionBtn.setBackgroundResource(R.drawable.bg_connect_green)
                binding.tvConnectActionBtn.setText(R.string.frag_connect_connect)
                binding.ivConnectBtnOverlay.visibility = View.GONE
            }
            ConnectionState.CONNECTION_ERROR -> binding.tvConnectActionBtn.setBackgroundResource(R.drawable.bg_connect_green)
            ConnectionState.DISCONNECTING -> {
                // disable btn?
            }
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

