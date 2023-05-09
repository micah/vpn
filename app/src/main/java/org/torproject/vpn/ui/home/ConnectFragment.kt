package org.torproject.vpn.ui.home

import android.animation.AnimatorSet
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import org.torproject.vpn.MainActivity
import org.torproject.vpn.MainActivity.Companion.KEY_ACTION
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentConnectBinding
import org.torproject.vpn.ui.home.model.ConnectFragmentViewModel
import org.torproject.vpn.vpn.ConnectionState
import org.torproject.vpn.vpn.VpnServiceCommand
import org.torproject.vpn.vpn.VpnStatusObservable
import kotlinx.coroutines.launch
import org.torproject.vpn.utils.*

class ConnectFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        val TAG: String = ConnectFragment::class.java.simpleName
    }

    private lateinit var binding: FragmentConnectBinding
    private lateinit var connectFragmentViewModel: ConnectFragmentViewModel

    //this is required to store current state that UI is in so we can decide whether to animate to next state
    private lateinit var currentVpnState: ConnectionState

    //connecting progress animation is stopped when state goes from connecting to connected.
    private var progressGradientAnimatorSet: AnimatorSet? = null

    private lateinit var preferenceHelper: PreferenceHelper

    private var startForResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            VpnServiceCommand.startVpn(context)
            context?.let { PreferenceHelper(it).startOnBoot = true }
        } else {
            VpnStatusObservable.update(ConnectionState.CONNECTION_ERROR)
        }
        connectFragmentViewModel.onVpnPrepared()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        currentVpnState = ConnectionState.INIT
        preferenceHelper = PreferenceHelper(requireContext())
        preferenceHelper.registerListener(this)
        connectFragmentViewModel = ViewModelProvider(this)[ConnectFragmentViewModel::class.java]
        binding = FragmentConnectBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = connectFragmentViewModel

        connectFragmentViewModel.prepareVpn.observe(viewLifecycleOwner, Observer<Intent?> {  intent ->
            intent?.let {
                startForResult.launch(it)
            }
        })

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    connectFragmentViewModel.connectionState.collect { vpnState ->
                        setUIState(vpnState)
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
        preferenceHelper.unregisterListener(this)
    }

    private fun setUIState(vpnState: ConnectionState) {
        Log.d(TAG, "setUIState: ${vpnState.name} | current state :${if(::currentVpnState.isInitialized) currentVpnState else "not initiaized"}")
        if (::currentVpnState.isInitialized && currentVpnState == vpnState) {
            return
        }

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
        if (currentVpnState == ConnectionState.INIT || currentVpnState == ConnectionState.DISCONNECTED) {
            binding.tvConnectActionBtn.setBackgroundResource(R.drawable.av_connect_to_pause)

            //connect button vector anim
            startVectorAnimationWithEndCallback(
                binding.tvConnectActionBtn.background, viewLifecycleOwner.lifecycle
            ) {
                binding.tvConnectActionBtn.setBackgroundResource(R.drawable.av_pause_to_connect)
            }

            //progressbar anim (reveal + infinite gradient shift)

            //infinite gradient
            val gradientAnim = createStatusBarAnimation(
                binding.progressSlider.background,
                requireContext(),
                intArrayOf(R.color.connectingRainbowStart, R.color.connectingRainbowEnd)
            )
            // reveal
            val center = binding.progressSlider.center()
            val revealAnim = ViewAnimationUtils.createCircularReveal(
                binding.progressSlider,
                center.x,
                center.y,
                0f,
                binding.progressSlider.width.toFloat()
            )


            progressGradientAnimatorSet = AnimatorSet().apply {
                if (currentVpnState == ConnectionState.INIT) {
                    play(revealAnim).before(gradientAnim)
                } else {
                    play(gradientAnim)
                }
                start()
            }

        } else {
            binding.tvConnectActionBtn.setBackgroundResource(R.drawable.av_pause_to_connect)
        }

        //TODO Animating gradient is possible but removing gradient transition for time being(as the vector buttons in designs are not totally compatible with Android.)
        /*connectionStateGradientAnimation(
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
        )*/


    }

    private fun connectingToIdleTransition() {

    }

    private fun showConnectedTransition() {
        //end infinite gradient animation
        progressGradientAnimatorSet?.end()
        progressGradientAnimatorSet = null

        // transition from gradient to red(connected color)
        createStatusBarConnectedGradientAnimation(
            binding.progressSlider.background, requireContext(), intArrayOf(
                R.color.connectingRainbowEnd,
                R.color.connectingRainbowStart,
                R.color.greenNormal
            ), lifecycle
        ) {}

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
        //end infinite gradient animation
        progressGradientAnimatorSet?.end()
        progressGradientAnimatorSet = null

        if (currentVpnState == ConnectionState.CONNECTING) {

            // transition from gradient to red(connected color)
            createStatusBarConnectedGradientAnimation(
                binding.progressSlider.background, requireContext(), intArrayOf(
                    R.color.connectingRainbowEnd,
                    R.color.connectingRainbowStart,
                    R.color.yellowNormal
                ), lifecycle
            ) {}

        }else if (currentVpnState == ConnectionState.CONNECTED) {

            // transition from gradient to red(connected color)
            createStatusBarConnectedGradientAnimation(
                binding.progressSlider.background, requireContext(), intArrayOf(
                    R.color.redNormal,
                    R.color.redNormal,
                    R.color.yellowNormal
                ), lifecycle
            ) {}

        }

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

            // transition from gradient to red(connected color)
            createStatusBarConnectedGradientAnimation(
                binding.progressSlider.background, requireContext(), intArrayOf(
                    R.color.greenNormal,
                    R.color.greenNormal,
                    R.color.redNormal
                ), lifecycle
            ) {}


        } else {
            binding.tvConnectActionBtn.setBackgroundResource(R.drawable.av_connect_to_pause)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key?.equals(PreferenceHelper.PROTECT_ALL_APPS) == true) {
            connectFragmentViewModel.updateVPNSettings()
        }
    }

}

