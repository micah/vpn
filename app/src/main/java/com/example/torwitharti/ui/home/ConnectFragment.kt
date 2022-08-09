package com.example.torwitharti.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.torwitharti.databinding.FragmentConnectBinding
import com.example.torwitharti.databinding.FragmentGuideFrameVp2Binding
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
        val connectFragmentModel =
            ViewModelProvider(this)[ConnectFragmentModel::class.java]

        binding = FragmentConnectBinding.inflate(inflater, container, false)
        binding.viewModel = connectFragmentModel
        connectFragmentModel.showGuideTour.observe(viewLifecycleOwner) { initViewPager() }

        return binding.root
    }

    private fun initViewPager() {
        binding.vp2Guide.adapter = GuideFrameVP2Adapter(this)
        TabLayoutMediator(binding.vp2Indicator, binding.vp2Guide) { _, _ -> }.attach()
        binding.vp2Guide.setPageTransformer(DepthPageTransformer())

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
        val viewBinding = FragmentGuideFrameVp2Binding.inflate(inflater, container, false)
        val viewModel = ViewModelProvider(this)[GuideFrameVP2ViewModel::class.java]
        viewBinding.viewModel = viewModel
        viewModel.setArgs(arguments)

        return viewBinding.root

    }


}