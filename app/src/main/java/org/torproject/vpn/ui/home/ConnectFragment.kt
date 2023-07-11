package org.torproject.vpn.ui.home

import android.Manifest
import android.animation.AnimatorSet
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import org.torproject.onionmasq.logging.LogObservable
import org.torproject.vpn.MainActivity
import org.torproject.vpn.MainActivity.Companion.KEY_ACTION
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentConnectBinding
import org.torproject.vpn.ui.exitselection.ExitSelectionBottomSheetFragment
import org.torproject.vpn.ui.home.model.ACTION_EXIT_NODE_SELECTION
import org.torproject.vpn.ui.home.model.ACTION_LOGS
import org.torproject.vpn.ui.home.model.ACTION_REQUEST_NOTIFICATION_PERMISSON
import org.torproject.vpn.ui.home.model.ConnectFragmentViewModel
import org.torproject.vpn.utils.*
import org.torproject.vpn.vpn.ConnectionState
import org.torproject.vpn.vpn.VpnServiceCommand
import org.torproject.vpn.vpn.VpnStatusObservable

class ConnectFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        val TAG: String = ConnectFragment::class.java.simpleName
    }

    private var _binding: FragmentConnectBinding? = null
    private val binding get() = _binding!!
    private lateinit var connectFragmentViewModel: ConnectFragmentViewModel

    //this is required to store current state that UI is in so we can decide whether to animate to next state
    private lateinit var currentVpnState: ConnectionState

    private lateinit var preferenceHelper: PreferenceHelper

    private var vpnPermissionDialogStartTime = 0L

    private var startForResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            VpnServiceCommand.startVpn(context)

            // Fixes button color after permission dialog, Quick fix, need refactoring.
            startVectorAnimationWithEndCallback(
                binding.tvConnectActionBtn.background, viewLifecycleOwner.lifecycle
            ) {
                binding.tvConnectActionBtn.setBackgroundResource(R.drawable.av_pause_to_connect)
            }
        } else {
            //this indicates that the permission request failed almost instantly. One of the reason could be that other VPN has always-on flag started.
            if (System.currentTimeMillis() - vpnPermissionDialogStartTime < 200) {
                LogObservable.getInstance().addLog("VPN permission request failed instantly. Other VPN is likely on ALWAYS-ON mode!")
            }

            VpnStatusObservable.update(ConnectionState.CONNECTION_ERROR)
        }
        connectFragmentViewModel.onVpnPrepared()
    }

    private var startNotificationRequestForResult: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(
                requireContext(),
                "TODO: SHOW PROPER HINT HOW TO ALLOW AGAIN NOTIFICATION PERMISSION",
                Toast.LENGTH_LONG
            ).show()
        }
        connectFragmentViewModel.onNotificationPermissionResult()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        currentVpnState = ConnectionState.INIT
        preferenceHelper = PreferenceHelper(requireContext())
        preferenceHelper.registerListener(this)
        connectFragmentViewModel = ViewModelProvider(this)[ConnectFragmentViewModel::class.java]
        _binding = FragmentConnectBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = connectFragmentViewModel

        connectFragmentViewModel.prepareVpn.observe(
            viewLifecycleOwner,
            Observer<Intent?> { intent ->
                intent?.let {
                    vpnPermissionDialogStartTime = System.currentTimeMillis()
                    startForResult.launch(it)
                }
            })

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    connectFragmentViewModel.connectionState.collect { vpnState ->
                        setUIState(vpnState)
                    }
                }

                launch {
                    connectFragmentViewModel.buttonWidth.collect { width ->
                        binding.llExitSelectionBtn.layoutParams.width = width
                    }
                }

                launch {
                    connectFragmentViewModel.action.collect { action ->
                        when (action) {
                            ACTION_LOGS -> {
                                findNavController().navigate(R.id.action_navigation_connect_to_loggingFragment)
                            }
                            ACTION_REQUEST_NOTIFICATION_PERMISSON -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    startNotificationRequestForResult.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                            ACTION_EXIT_NODE_SELECTION -> {
                                if (isAdded) {
                                    ExitSelectionBottomSheetFragment().show(parentFragmentManager, "exitNodeSelector")
                                }
                            }
                            else -> {
                                //other cases of navigation.
                            }
                        }
                    }
                }
            }
        }

        arguments?.let { bundle ->
            val action = bundle.getString(KEY_ACTION)
            if (MainActivity.ACTION_REQUEST_VPN_PERMISSON == action) {
                connectFragmentViewModel.prepareToStartVPN()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        preferenceHelper.unregisterListener(this)
    }

    private fun setUIState(vpnState: ConnectionState) {
        Log.d(
            TAG,
            "setUIState: ${if (::currentVpnState.isInitialized) currentVpnState else "not initialized"} --> ${vpnState.name}"
        )
        if (::currentVpnState.isInitialized && currentVpnState == vpnState) {
            return
        }
        binding.gradientView.setState(vpnState)

        when (vpnState) {
            ConnectionState.INIT -> {

            }

            ConnectionState.CONNECTING -> showConnectingTransition()

            ConnectionState.PAUSED -> {}
            ConnectionState.CONNECTED -> {
                binding.includeStats.chronometer.base = VpnStatusObservable.getStartTimeBase()

                binding.includeStats.chronometer.start()
                showConnectedTransition()
            }

            ConnectionState.DISCONNECTED -> {
                binding.includeStats.chronometer.stop()
                showDisconnectedTransition()
            }

            ConnectionState.CONNECTION_ERROR -> {
                binding.includeStats.chronometer.stop()
                showErrorTransition()
            }

            ConnectionState.DISCONNECTING -> {
                // disable btn?
            }
        }
        currentVpnState = vpnState
    }

    /*
    * Scene transitions
    *
    * ************
     */

    private fun showConnectingTransition() {
        if (currentVpnState == ConnectionState.INIT || currentVpnState == ConnectionState.DISCONNECTED || currentVpnState == ConnectionState.CONNECTION_ERROR) {
            binding.tvConnectActionBtn.setBackgroundResource(R.drawable.av_connect_to_pause)

            //connect button vector anim
            startVectorAnimationWithEndCallback(
                binding.tvConnectActionBtn.background, viewLifecycleOwner.lifecycle
            ) {
                binding.tvConnectActionBtn.setBackgroundResource(R.drawable.av_pause_to_connect)
            }

        } else {
            binding.tvConnectActionBtn.setBackgroundResource(R.drawable.av_pause_to_connect)
        }
    }

    private fun connectingToIdleTransition() {

    }

    private fun showConnectedTransition() {
        if (currentVpnState == ConnectionState.CONNECTING) {
            binding.tvConnectActionBtn.setBackgroundResource(R.drawable.av_pause_to_stop)

            //pause to stop transition
            startVectorAnimationWithEndCallback(
                binding.tvConnectActionBtn.background, viewLifecycleOwner.lifecycle
            ) {
                binding.tvConnectActionBtn.setBackgroundResource(R.drawable.av_stop_connect)
            }

        } else {
            binding.tvConnectActionBtn.setBackgroundResource(R.drawable.av_stop_connect)
        }

    }

    private fun showErrorTransition() {
        if (currentVpnState == ConnectionState.CONNECTING) {
            binding.tvConnectActionBtn.setBackgroundResource(R.drawable.av_pause_to_connect)

            //pause to stop transition
            startVectorAnimationWithEndCallback(
                binding.tvConnectActionBtn.background, viewLifecycleOwner.lifecycle
            ) {
                binding.tvConnectActionBtn.setBackgroundResource(R.drawable.av_connect_to_pause)
            }

        } else {
            binding.tvConnectActionBtn.setBackgroundResource(R.drawable.av_connect_to_pause)
        }

    }

    private fun showDisconnectedTransition() {
        if (currentVpnState == ConnectionState.DISCONNECTING) {
            startVectorAnimationWithEndCallback(
                binding.tvConnectActionBtn.background, viewLifecycleOwner.lifecycle
            ) {
                binding.tvConnectActionBtn.setBackgroundResource(R.drawable.av_connect_to_pause)
            }

        } else {
            binding.tvConnectActionBtn.setBackgroundResource(R.drawable.av_connect_to_pause)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        key?.let {
            when(it) {
                PreferenceHelper.PROTECT_ALL_APPS -> connectFragmentViewModel.updateVPNSettings()
                PreferenceHelper.EXIT_NODE_COUNTRY -> connectFragmentViewModel.updateExitNodeButton()
                PreferenceHelper.AUTOMATIC_EXIT_NODE_SELECTION -> connectFragmentViewModel.updateExitNodeButton()
            }
        }
    }

}

