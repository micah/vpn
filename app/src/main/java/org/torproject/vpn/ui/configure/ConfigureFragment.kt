package org.torproject.vpn.ui.configure

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureFragmentViewModel =
            ViewModelProvider(this)[ConfigureFragmentViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentConfigureBinding.bind(view)
        binding.viewModel = configureFragmentViewModel
        binding.handler = this
        binding.lifecycleOwner = viewLifecycleOwner

        binding.quickstart.isChecked = configureFragmentViewModel.startOnBoot.value
        binding.quickstart.setOnCheckedChangeListener(configureFragmentViewModel::onStartOnBootChanged)

        binding.exitLocation.subtitle = configureFragmentViewModel.exitNodeCountry
        binding.bridges.subtitle = configureFragmentViewModel.selectedBridgeType
    }

    override fun onAppsClicked(v: View) {
        findNavController().navigateSafe(R.id.action_configureFragment_to_appRoutingFragment)
    }

    override fun onAppIconClicked(v: View) {
        findNavController().navigateSafe(R.id.action_configureFragment_to_appearanceFragment)
    }

    override fun onAboutClicked(v: View) {
        findNavController().navigateSafe(R.id.action_configureFragment_to_aboutFragment)
    }

    override fun onPrivacyPolicyClicked(v: View) {
        findNavController().navigateSafe(R.id.action_configureFragment_to_PrivacyPolicy)
    }

    override fun onLicencesClicked(v: View) {
        findNavController().navigateSafe(R.id.action_configureFragment_to_Licenses)
    }

    override fun onTorLogsClicked(v: View) {
        findNavController().navigateSafe(R.id.action_configureFragment_to_LoggingFragment)
    }

    override fun onBridgeSettingsClicked(v: View) {
        findNavController().navigateSafe(R.id.action_configureFragment_to_bridgeSettingsFragment)
    }

    override fun onAlwaysOnClicked(v: View) {
        runCatching {
            startActivity(
                Intent(Settings.ACTION_VPN_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
        }.onFailure {
            it.printStackTrace()
        }
    }

    override fun onExitLocationClicked(v: View) {
        findNavController().navigateSafe(R.id.action_configureFragment_to_ExitNodeSelection)
    }

}