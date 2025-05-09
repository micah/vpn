package org.torproject.vpn.ui.configure

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.materialswitch.MaterialSwitch
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentConfigureBinding
import org.torproject.vpn.ui.configure.model.ConfigureFragmentViewModel
import org.torproject.vpn.utils.navigateSafe

class ConfigureFragment : Fragment(), ClickHandler {
    companion object {
        val TAG: String = ConfigureFragment::class.java.simpleName
    }

    private lateinit var binding: FragmentConfigureBinding
    private lateinit var configureFragmentViewModel: ConfigureFragmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentConfigureBinding.inflate(inflater, container, false)
        configureFragmentViewModel =
            ViewModelProvider(this)[ConfigureFragmentViewModel::class.java]
        binding.viewModel = configureFragmentViewModel
        binding.handler = this
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.quickstart.apply {
            isChecked = configureFragmentViewModel.startOnBoot
            setOnCheckedChangeListener(configureFragmentViewModel::onStartOnBootChanged)
        }

        binding.exitLocation.apply {
            findViewById<TextView>(R.id.tvSubtitle).text =
                configureFragmentViewModel.exitNodeCountry
        }

        binding.bridges.apply {
            findViewById<TextView>(R.id.tvSubtitle).text =
                configureFragmentViewModel.selectedBridgeType
        }
    }

    override fun onResume() {
        super.onResume()
        binding.quickstart.apply {
            isChecked = configureFragmentViewModel.startOnBoot
        }
    }

    override fun onHelpClicked(v: View) {
        findNavController().navigateSafe(R.id.action_configureFragment_to_offlineHelpFragment)
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
        // TODO("Not yet implemented")
    }

    override fun onLicencesClicked(v: View) {
        // TODO("Not yet implemented")
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