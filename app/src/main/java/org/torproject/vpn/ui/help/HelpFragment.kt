package org.torproject.vpn.ui.help

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import org.torproject.vpn.MainActivityViewModel
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentHelpBinding
import org.torproject.vpn.utils.navigateSafe

class HelpFragment : Fragment(R.layout.fragment_help), ClickHandler {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentHelpBinding.bind(view)
        binding.lifecycleOwner = viewLifecycleOwner

        // set collapsing app bar scroll behavior for portrait layouts
        binding.portraitHelpScrollContainer?.let { container ->
            (binding.toolbarLayout?.layoutParams as? AppBarLayout.LayoutParams)?.let { params ->
                if (container.canScrollVertically(1)) {
                    // allow collapsing of the app bar if the content of the scroll container is not completely visible
                    params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS)
                } else {
                    // do not allow collapsing of the app bar in case everything fits on the screen
                    params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL)
                }
            }
        }

        // set bottom padding for scroll container in landscape mode
        binding.landscapeHelpScrollContainer?.let { container ->
            val activityViewModel = ViewModelProvider(requireActivity())[MainActivityViewModel::class.java]
            container.setPadding(
                container.paddingStart,
                container.paddingTop,
                container.paddingEnd,
                container.paddingBottom + (activityViewModel.bottomNavBarHeight.value ?: 0)
            )
        }

        binding.handler = this
    }



    override fun onOfflineHelpClicked(v: View) {
        findNavController().navigateSafe(R.id.action_helpFragment_to_offlineHelpFragment)
    }

    override fun onReportBugClicked(v: View) {
        if (isAdded) {
            ReportBugBottomSheetFragment().show(parentFragmentManager, "ReportBugBottomSheet")
        }
    }

    override fun onContactSupportClicked(v: View) {
        if (isAdded) {
            ContactSupportBottomSheetFragment().show(parentFragmentManager, "ContactSupportBottomSheet")
        }
    }
}