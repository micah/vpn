package org.torproject.vpn.ui.configure

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentConfigureBinding
import org.torproject.vpn.ui.configure.model.ConfigureFragmentViewModel

class ConfigureFragment : Fragment(R.layout.fragment_configure), ClickHandler {
    companion object {
         val TAG: String = ConfigureFragment::class.java.simpleName
    }

    private lateinit var configureFragmentViewModel: ConfigureFragmentViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureFragmentViewModel =
            ViewModelProvider(this)[ConfigureFragmentViewModel::class.java]
        val binding = FragmentConfigureBinding.bind(view)
        binding.viewModel = configureFragmentViewModel
        binding.handler = this
    }

    override fun onAppsClicked(v: View) {
        Log.d(TAG, "apps entry clicked")
        findNavController().navigate(R.id.action_configureFragment_to_appRoutingFragment)
    }

    override fun onConnectionClicked(v: View) {
        findNavController().navigate(R.id.action_configureFragment_to_connectionFragment)
    }

}