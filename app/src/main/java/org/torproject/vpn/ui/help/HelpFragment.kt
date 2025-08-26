package org.torproject.vpn.ui.help

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentHelpBinding
import org.torproject.vpn.utils.navigateSafe

class HelpFragment : Fragment(R.layout.fragment_help), ClickHandler {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentHelpBinding.bind(view)
        binding.lifecycleOwner = viewLifecycleOwner

        binding.handler = this
    }



    override fun onOfflineHelpClicked(v: View) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = "https://support.torproject.org/tor-vpn".toUri()
        }
        startActivity(intent)
        //findNavController().navigateSafe(R.id.action_helpFragment_to_offlineHelpFragment)
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