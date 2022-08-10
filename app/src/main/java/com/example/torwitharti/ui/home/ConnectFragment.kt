package com.example.torwitharti.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.transition.*
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.torwitharti.databinding.FragmentConnectBinding
import com.example.torwitharti.databinding.FragmentGuideFrameVp2Binding
import com.example.torwitharti.databinding.FragmentSceneGuideStateBinding
import com.example.torwitharti.databinding.FragmentSceneInitialStateBinding
import com.google.android.material.tabs.TabLayoutMediator

const val argShowActionCommands = "arg_show_action_commands"
const val argIndex = "arg_index"

class ConnectFragment : Fragment() {
    private lateinit var binding: FragmentConnectBinding
    private val minScale = 0.75f
    private val totalGuideSliders = 5

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val connectFragmentViewModel =
            ViewModelProvider(this)[ConnectFragmentViewModel::class.java]

        binding = FragmentConnectBinding.inflate(inflater, container, false)
        binding.viewModel = connectFragmentViewModel

        connectFragmentViewModel.showGuideTour.observe(viewLifecycleOwner) { show ->
            if (show) {
                initViewPager()
            } else {
                hideGuide()
            }
        }

        return binding.root
    }

    private fun initViewPager() {
        val guideStateBinding =
            FragmentSceneGuideStateBinding.inflate(layoutInflater, binding.flSceneContainer, false)

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
            FragmentSceneInitialStateBinding.inflate(
                layoutInflater,
                binding.flSceneContainer,
                false
            )

        val guideScene: Scene = Scene(binding.flSceneContainer, initialStateBinding.root)
        TransitionManager.go(guideScene, ChangeBounds())

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