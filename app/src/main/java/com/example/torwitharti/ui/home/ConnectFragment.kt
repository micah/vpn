package com.example.torwitharti.ui.home

import android.animation.AnimatorSet
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.example.torwitharti.R
import com.example.torwitharti.databinding.*
import com.example.torwitharti.ui.home.model.ConnectFragmentViewModel
import com.example.torwitharti.utils.center
import com.example.torwitharti.utils.createStatusBarAnimation
import com.example.torwitharti.utils.createStatusBarConnectedGradientAnimation
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

    //this is required to store current state that UI is in so we can decide whether to animate to next state
    private lateinit var currentVpnState: ConnectionState

    //connecting progress animation is stopped when state goes from connecting to connected.
    private var progressGradientAnimatorSet: AnimatorSet? = null

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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        connectFragmentViewModel = ViewModelProvider(this)[ConnectFragmentViewModel::class.java]

        binding = FragmentConnectBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = connectFragmentViewModel

        connectFragmentViewModel.prepareVpn.observe(viewLifecycleOwner) { intent ->
            if (intent != null) {
                startForResult.launch(intent)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    connectFragmentViewModel.connectionState.collect { vpnState ->
                        Log.d(TAG, "onCreateView: vpn state ${vpnState}")
                        setUIState(vpnState)
                    }
                }
            }
        }



        return binding.root
    }

    private fun setUIState(vpnState: ConnectionState) {
        Log.d(TAG, "setUIState: ${vpnState.name}")
        if (::currentVpnState.isInitialized && currentVpnState == vpnState) {
            return
        }

        when (vpnState) {
            ConnectionState.INIT -> {

            }
            ConnectionState.CONNECTING -> showConnectingTransition()

            ConnectionState.PAUSED -> {}
            ConnectionState.CONNECTED -> {
                binding.includeStats.chronometer.base = SystemClock.elapsedRealtime()
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


}

