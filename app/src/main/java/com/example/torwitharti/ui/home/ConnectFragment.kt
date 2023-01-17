package com.example.torwitharti.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import androidx.transition.*
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.torwitharti.R
import com.example.torwitharti.databinding.*
import com.example.torwitharti.ui.home.model.ConnectFragmentViewModel
import com.example.torwitharti.ui.home.model.GuideFrameVP2ViewModel
import com.example.torwitharti.utils.*
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

const val argShowActionCommands = "arg_show_action_commands"
const val argIndex = "arg_index"
private const val APP_LISTING_KEY = "app_listing_key"


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

        connectFragmentViewModel.switchToIdleScene.observe(viewLifecycleOwner) { show ->
            showIdleScene(
                show
            )
        }

        connectFragmentViewModel.showGuideTour.observe(viewLifecycleOwner) { show ->
            if (show) {
                showGuide()
            } else {
                hideGuide()
            }
        }

        connectFragmentViewModel.switchToConnectingScene.observe(viewLifecycleOwner) { show ->
            showConnectingScene(
                show
            )
        }

        lifecycleScope.launch {

            connectFragmentViewModel.onAppsPressed
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collect { uiState ->
                    if (uiState.isSafeToNavigate) {
                        findNavController().navigate(R.id.action_connectFragment_to_appRoutingFragment)
                        connectFragmentViewModel.appNavigationCompleted()
                    }
                }
        }

        connectFragmentViewModel.switchToConnectedScene.observe(viewLifecycleOwner) { show ->
            showConnectedScene(
                show
            )
        }

        connectFragmentViewModel.switchToErrorScene.observe(viewLifecycleOwner) { show ->
            showCollapsedErrorInConnectScreen(
                show
            )
        }

        connectFragmentViewModel.switchToErrorSceneExpanded.observe(viewLifecycleOwner) { show ->
            showExpandedErrorInConnectScreen2(
                show
            )
        }

//        connectFragmentViewModel.switchToReportFrag.observe(viewLifecycleOwner) {
//            findNavController().navigate(
//                R.id.action_connectFragment_to_reportFragment
//            )
//        }

        lifecycleScope.launch {

            connectFragmentViewModel.switchToReportFrag
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collect { uiState ->
                    if (uiState.isSafeToNavigate) {
                        findNavController().navigate(
                            R.id.action_connectFragment_to_reportFragment
                        )
                        connectFragmentViewModel.reportNavigationCompleted()
                    }
                }
        }

        return binding.root
    }

    /*
    * Scene transitions
    *
    * ************
     */
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

    private fun showConnectingScene(show: Boolean) {
        if (show) {
            val connectingStateBinding =
                FragmentConnectSceneConnectingStateBinding.inflate(
                    layoutInflater,
                    binding.flSceneContainer,
                    false
                )
            connectingStateBinding.viewModel = connectFragmentViewModel

            val connectingScene = Scene(binding.flSceneContainer, connectingStateBinding.root)
            val medAnimationDuration = resources.getInteger(android.R.integer.config_mediumAnimTime)

            with(AutoTransition()) {
                addTarget(R.id.tv_connect_status_title)
                addTarget(R.id.progress_connecting)
                addTarget(R.id.iv_connect_status_icon)

                connectingScene.setEnterAction {
                    connectingStateBinding.includeActions.ivApps
                        .animate()
                        .alpha(0.4f)
                        .duration = medAnimationDuration.toLong()

                    connectingStateBinding.includeActions.ivGlobe
                        .animate()
                        .alpha(0.4f)
                        .duration =
                        medAnimationDuration.toLong()
                }

                TransitionManager.go(connectingScene, this)
            }

            repeatVectorAnimation(connectingStateBinding.ivConnectStatusIcon.drawable, lifecycle)

        }
    }

    private fun showConnectedScene(show: Boolean) {
        if (show) {
            val connectedStateBinding =
                FragmentConnectSceneConnectedStateBinding.inflate(
                    layoutInflater,
                    binding.flSceneContainer,
                    false
                )
            connectedStateBinding.viewModel = connectFragmentViewModel

            val medAnimationDuration = resources.getInteger(android.R.integer.config_mediumAnimTime)
            val connectedScene = Scene(binding.flSceneContainer, connectedStateBinding.root)

            with(AutoTransition()) {
                addTarget(R.id.tv_connect_status_title)
                addTarget(R.id.progress_connecting)
                addTarget(R.id.iv_connect_status_icon)

                connectedScene.setEnterAction {
                    connectedStateBinding.includeActions.ivApps.alpha = 0.4f
                    connectedStateBinding.includeActions.ivGlobe.alpha = 0.4f
                    connectedStateBinding.includeActions.ivApps.animate().alpha(1f).duration =
                        medAnimationDuration.toLong()
                    connectedStateBinding.includeActions.ivGlobe.animate().alpha(1f).duration =
                        medAnimationDuration.toLong()
                }

                TransitionManager.go(connectedScene, this)
            }

            startVectorAnimationWithEndCallback(
                connectedStateBinding.ivConnectStatusIcon.drawable,
                lifecycle
            ) {
                with(connectedStateBinding.ivConnectStatusIcon) {
                    val concealCenter = center()
                    val concealAnim = ViewAnimationUtils.createCircularReveal(
                        connectedStateBinding.ivConnectStatusIcon,
                        concealCenter.x,
                        concealCenter.y,
                        connectedStateBinding.ivConnectStatusIcon.width.toFloat(),
                        0f
                    )

                    val revealCenter = centerInParent()
                    val revealAnim = ViewAnimationUtils.createCircularReveal(
                        connectedStateBinding.clGraphFrame, revealCenter.x, revealCenter.y, 0f,
                        (connectedStateBinding.root.height).toFloat()
                    )

                    concealAnim.animateWithEndCallback(lifecycle) {
                        connectedStateBinding.groupConnected.visibility = View.GONE
                        connectedStateBinding.clGraphFrame.visibility = View.VISIBLE
                        revealAnim.start()
                    }

                }
            }
        }
    }

    private fun showIdleScene(show: Boolean) {
        if (show) {
            val initialStateBinding =
                FragmentConnectSceneInitialStateBinding.inflate(
                    layoutInflater,
                    binding.flSceneContainer,
                    false
                )
            initialStateBinding.viewModel = connectFragmentViewModel

            val idleScene = Scene(binding.flSceneContainer, initialStateBinding.root)

            with(AutoTransition()) {
                addTarget(R.id.tv_connect_status_title)
                addTarget(R.id.progress_connecting)
                addTarget(R.id.iv_connect_status_icon)

                idleScene.setEnterAction {
                    val medAnimationDuration =
                        resources.getInteger(android.R.integer.config_mediumAnimTime)
                    initialStateBinding.includeActions.ivApps.alpha = 0.4f
                    initialStateBinding.includeActions.ivGlobe.alpha = 0.4f
                    initialStateBinding.includeActions.ivApps.animate().alpha(1f).duration =
                        medAnimationDuration.toLong()
                    initialStateBinding.includeActions.ivGlobe.animate().alpha(1f).duration =
                        medAnimationDuration.toLong()
                }

                TransitionManager.go(idleScene, this)
            }
        }

    }

    private fun showCollapsedErrorInConnectScreen(show: Boolean) {
        if (show) {
            val errorStateBinding =
                FragmentConnectSceneErrorBinding.inflate(
                    layoutInflater,
                    binding.flSceneContainer,
                    false
                )
            errorStateBinding.viewModel = connectFragmentViewModel

            val collapsedErrorScene = Scene(binding.flSceneContainer, errorStateBinding.root)
            val medAnimationDuration = resources.getInteger(android.R.integer.config_mediumAnimTime)
            val floaterCornerRad = resources.getDimension(R.dimen.connect_error_collapsed_bf_radius)

            with(AutoTransition()) {
                addTarget(R.id.tv_connect_status_title)
                addTarget(R.id.progress_connecting)
                addTarget(R.id.iv_connect_status_icon)

                TransitionManager.go(collapsedErrorScene, this)

                //errorStateBinding.imageView.animate().translationZ(0f).setStartDelay(3000).start()
                //errorStateBinding.clErrorCollapsed.animate().translationZ(0f).setStartDelay(3000).start()

            }
        }
    }

    private fun showExpandedErrorInConnectScreen(show: Boolean) {
        if (show) {
            val expandedErrorStateBinding =
                FragmentConnectErrorExpandedBinding.inflate(
                    layoutInflater,
                    binding.flSceneContainer,
                    false
                )
            expandedErrorStateBinding.viewModel = connectFragmentViewModel

            val connectingScene = Scene(binding.flSceneContainer, expandedErrorStateBinding.root)
            val medAnimationDuration = resources.getInteger(android.R.integer.config_mediumAnimTime)
            val floaterCornerRad = resources.getDimension(R.dimen.connect_error_collapsed_bf_radius)

            with(ChangeBounds()) {
                TransitionManager.go(connectingScene, this)

                //errorStateBinding.imageView.animate().translationZ(0f).setStartDelay(3000).start()
                //errorStateBinding.clErrorCollapsed.animate().translationZ(0f).setStartDelay(3000).start()
                val startSize = resources.getDimension(R.dimen.connect_error_collapsed_title_size)
                val endSize = resources.getDimension(R.dimen.connect_error_expanded_title_size)
                animateTextSizeChange(
                    expandedErrorStateBinding.tvConnectStatusTitle, startSize, endSize, lifecycle
                ) {

                }
            }
        }
    }

    private fun showExpandedErrorInConnectScreen2(show: Boolean) {
        if (show) {
            val expandedErrorStateBinding =
                FragmentConnectErrorExpandedBinding.inflate(
                    layoutInflater,
                    binding.flSceneContainer,
                    false
                )
            expandedErrorStateBinding.viewModel = connectFragmentViewModel

            val connectingScene = Scene(binding.flSceneContainer, expandedErrorStateBinding.root)
            val medAnimationDuration = resources.getInteger(android.R.integer.config_mediumAnimTime)
            val floaterCornerRad = resources.getDimension(R.dimen.connect_error_collapsed_bf_radius)

            with(ChangeBounds()) {
                TransitionManager.go(connectingScene, this)

                //errorStateBinding.imageView.animate().translationZ(0f).setStartDelay(3000).start()
                //errorStateBinding.clErrorCollapsed.animate().translationZ(0f).setStartDelay(3000).start()
                val startSize = resources.getDimension(R.dimen.connect_error_collapsed_title_size)
                val endSize = resources.getDimension(R.dimen.connect_error_expanded_title_size)
                animateTextSizeChange(
                    expandedErrorStateBinding.tvConnectStatusTitle, startSize, endSize, lifecycle
                ) {

                }
            }
        }
    }

    /**
     * Guide Fragment adapter
     */
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

    /**
     * VP2 slide animation
     */
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