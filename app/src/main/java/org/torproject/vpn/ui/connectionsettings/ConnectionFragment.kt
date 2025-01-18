package org.torproject.vpn.ui.connectionsettings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentConnectionsettingsBinding
import org.torproject.vpn.ui.connectionsettings.model.ConnectionFragmentViewModel
import org.torproject.vpn.utils.navigateSafe

// TODO: Restructure class. Still required to disable bridges.
//  Moving mode bridge to BridgesFragment will do the trick.
class ConnectionFragment: Fragment(R.layout.fragment_connectionsettings), ClickHandler {

    private lateinit var configureFragmentViewModel: ConnectionFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureFragmentViewModel = ViewModelProvider(this)[ConnectionFragmentViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentConnectionsettingsBinding.bind(view)

        binding.viewModel = configureFragmentViewModel
        binding.handler = this
        binding.lifecycleOwner = viewLifecycleOwner

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.killSwitch.visibility = View.VISIBLE

        binding.quickstart.isChecked = configureFragmentViewModel.startOnBoot
        binding.quickstart.setOnCheckedChangeListener(configureFragmentViewModel::onStartOnBootChanged)

        binding.switchUseBridge.setOnCheckedChangeListener { buttonView, isChecked ->
            configureFragmentViewModel.onUseBridgeChanged(buttonView, isChecked)
            if (binding.switchUseBridge.isPressed) {
                configureFragmentViewModel.animateBridgeSettingsContainerHeight(isChecked)
            } else {
                configureFragmentViewModel.setBridgeSettingsContainerHeight()
            }
        }
        configureFragmentViewModel.setBridgeSettingsContainerHeight()
    }

    override fun onTorLogsClicked(v: View) {
        findNavController().navigateSafe(R.id.action_connectionFragment_to_LoggingFragment)
    }

    override fun onAlwaysOnClicked(v: View) {
        val intent = Intent("android.net.vpn.SETTINGS")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    override fun onBridgeSettingsClicked(v: View) {
        findNavController().navigateSafe(R.id.action_connectionFragment_to_bridgeSettingsFragment)
    }

}