package org.torproject.vpn.ui.configure

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentConfigureBinding
import org.torproject.vpn.ui.configure.model.ConfigureFragmentViewModel
import org.torproject.vpn.utils.navigateSafe

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
        binding.lifecycleOwner = viewLifecycleOwner
    }
    override fun onHelpClicked(v: View) {
        findNavController().navigateSafe(R.id.action_configureFragment_to_helpFragment)
    }
    override fun onAppsClicked(v: View) {
        Log.d(TAG, "apps entry clicked")
        findNavController().navigateSafe(R.id.action_configureFragment_to_appRoutingFragment)
    }

    override fun onConnectionClicked(v: View) {
        findNavController().navigateSafe(R.id.action_configureFragment_to_connectionFragment)
    }

    override fun onAppearanceClicked(v: View) {
        findNavController().navigate(R.id.action_configureFragment_to_appearanceFragment)
    }

}