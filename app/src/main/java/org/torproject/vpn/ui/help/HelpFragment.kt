package org.torproject.vpn.ui.help

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentHelpBinding

class HelpFragment : Fragment(R.layout.fragment_help), ClickHandler {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentHelpBinding.bind(view)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.handler = this
    }



    override fun onFAQClicked(v: View) {
        val webpage: Uri = Uri.parse("https://support.torproject.org")
        val intent = Intent(ACTION_VIEW, webpage)
        this.context?.let {
            if (intent.resolveActivity(it.packageManager) != null) {
                startActivity(intent)
            }
        }
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