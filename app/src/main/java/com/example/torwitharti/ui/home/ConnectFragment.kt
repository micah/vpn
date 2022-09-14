package com.example.torwitharti.ui.home

import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.transition.*
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.torwitharti.R
import com.example.torwitharti.databinding.*
import com.google.android.material.tabs.TabLayoutMediator

const val argShowActionCommands = "arg_show_action_commands"
const val argIndex = "arg_index"

class ConnectFragment : Fragment() {
    private lateinit var binding: FragmentConnectBinding
    private val minScale = 0.75f
    private val totalGuideSliders = 5
    private lateinit var connectFragmentViewModel: ConnectFragmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        connectFragmentViewModel = ViewModelProvider(this)[ConnectFragmentViewModel::class.java]

        binding = FragmentConnectBinding.inflate(inflater, container, false)
        binding.viewModel = connectFragmentViewModel

        connectFragmentViewModel.showGuideTour.observe(viewLifecycleOwner) { show ->
            if (show) {
                showGuide()
            } else {
                hideGuide()
            }
        }

        connectFragmentViewModel.onAppsPressed.observe(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_connectFragment_to_appRoutingFragment)
        }

        connectFragmentViewModel.switchToConnectingScene.observe(viewLifecycleOwner) { show -> showConnectiveScene(show) }

        connectFragmentViewModel.switchToConnectedScene.observe(viewLifecycleOwner) { show -> showConnectedScene(show) }
        return binding.root
    }

    private fun showGuide() {
        val guideStateBinding =
            FragmentConnectSceneGuideStateBinding.inflate(
                layoutInflater,
                binding.flSceneContainer,
                false
            )
        guideStateBinding.viewModel = connectFragmentViewModel
        val guideScene: Scene = Scene(binding.flSceneContainer, guideStateBinding.root)
        val changeBoundTransition = ChangeBounds()
        changeBoundTransition.addListener(object : TransitionListenerAdapter() {
            override fun onTransitionEnd(transition: Transition) {
                super.onTransitionEnd(transition)
                guideStateBinding.vp2Guide.adapter = GuideFrameVP2Adapter(this@ConnectFragment)
                TabLayoutMediator(
                    guideStateBinding.vp2Indicator,
                    guideStateBinding.vp2Guide
                ) { _, _ -> }.attach()
                guideStateBinding.vp2Guide.setPageTransformer(DepthPageTransformer())

            }
        })
        TransitionManager.go(guideScene, changeBoundTransition)

    }

    private fun hideGuide() {
        val initialStateBinding =
            FragmentConnectSceneInitialStateBinding.inflate(
                layoutInflater,
                binding.flSceneContainer,
                false
            )


        initialStateBinding.viewModel = connectFragmentViewModel
        val guideScene: Scene = Scene(binding.flSceneContainer, initialStateBinding.root)
        TransitionManager.go(guideScene, ChangeBounds())

    }

    private fun showConnectiveScene(show: Boolean) {
        if (show) {

            val connectingStateBinding =
                FragmentConnectSceneConnectingStateBinding.inflate(
                    layoutInflater,
                    binding.flSceneContainer,
                    false
                )
            connectingStateBinding.viewModel = connectFragmentViewModel

            val connectingScene: Scene =
                Scene(binding.flSceneContainer, connectingStateBinding.root)
            val changeBoundTransition = ChangeBounds()
            TransitionManager.go(connectingScene, changeBoundTransition)

            val animatedDrawable =
                (connectingStateBinding.imageView.drawable as AnimatedVectorDrawable)

            animatedDrawable.registerAnimationCallback(object : Animatable2.AnimationCallback() {
                override fun onAnimationEnd(drawable: Drawable?) {
                    connectingStateBinding.imageView.post { animatedDrawable.start() }
                }
            })

            animatedDrawable.start()

        }
    }

    private fun showConnectedScene(show: Boolean){

    }

    private inner class GuideFrameVP2Adapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = totalGuideSliders

        override fun createFragment(position: Int): Fragment {
            // Return a NEW fragment instance in createFragment(int)
            val fragment = GuideFrameVP2Fragment()
            fragment.arguments = Bundle().apply {
                putBoolean(argShowActionCommands, position == totalGuideSliders - 1)
                putInt(argIndex, position)
            }
            return fragment
        }
    }

    private inner class DepthPageTransformer : ViewPager2.PageTransformer {

        override fun transformPage(view: View, position: Float) {
            view.apply {
                val pageWidth = width
                when {
                    position < -1 -> { // [-Infinity,-1)
                        // This page is way off-screen to the left.
                        alpha = 0f
                    }
                    position <= 0 -> { // [-1,0]
                        // Use the default slide transition when moving to the left page
                        alpha = 1f
                        translationX = 0f
                        translationZ = 0f
                        scaleX = 1f
                        scaleY = 1f
                    }
                    position <= 1 -> { // (0,1]
                        // Fade the page out.
                        alpha = 1 - position

                        // Counteract the default slide transition
                        translationX = pageWidth * -position
                        // Move it behind the left page
                        translationZ = -1f

                        // Scale the page down (between MIN_SCALE and 1)
                        val scaleFactor = (minScale + (1 - minScale) * (1 - Math.abs(position)))
                        scaleX = scaleFactor
                        scaleY = scaleFactor
                    }
                    else -> { // (1,+Infinity]
                        // This page is way off-screen to the right.
                        alpha = 0f
                    }
                }
            }
        }
    }
}

/**
 * Connect fragment slider items
 */
class GuideFrameVP2Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val connectFragmentViewModel by viewModels<ConnectFragmentViewModel>({ requireParentFragment() })

        val viewModel = ViewModelProvider(this)[GuideFrameVP2ViewModel::class.java]
        viewModel.setArgs(arguments)
        viewModel.setConnectFragmentViewModel(connectFragmentViewModel)
        val viewBinding = FragmentGuideFrameVp2Binding.inflate(inflater, container, false)
        viewBinding.viewModel = viewModel

        return viewBinding.root


    }


}