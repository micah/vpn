package org.torproject.vpn.ui.bridgesettings

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentBridgesettingsBinding
import org.torproject.vpn.ui.bridgesettings.model.BridgeSettingsFragmentViewModel
import org.torproject.vpn.utils.PreferenceHelper
import org.torproject.vpn.utils.navigateSafe

class BridgeSettingsFragment: Fragment(R.layout.fragment_bridgesettings), ClickHandler, OnSharedPreferenceChangeListener {

    companion object {
        val TAG = BridgeSettingsFragment::class.java.simpleName
    }

    private lateinit var viewModel: BridgeSettingsFragmentViewModel
    private var _binding: FragmentBridgesettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[BridgeSettingsFragmentViewModel::class.java]
        viewModel.load()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBridgesettingsBinding.bind(view)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.handler = this
        viewModel.preferenceHelper.registerListener(this)
        binding.rgBridgeOptions.check(viewModel.getSelectedBridgeTypeId())
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.preferenceHelper.unregisterListener(this)
        _binding = null
    }

    override fun onManualBridgeSelectionClicked(v: View) {
        val dialog = BridgeDialogFragment.create()
        dialog.show(parentFragmentManager, "BRIDGE_DIALOG")
    }

    override fun onTorBridgeBotClicked(v: View) {
        findNavController().navigateSafe(R.id.action_navigation_bridgeSettings_to_bridgeBot)
    }

    override fun onTelegramBridgeBotClicked(v: View) {
        val intent = viewModel.getTelegramIntent()
        startActivity(intent);
    }

    override fun onEmailBridgeBotClicked(v: View) {
        val intent = viewModel.getEmailBotIntent()
        startActivity(intent)
    }

    override fun onWebBridgeBotClicked(v: View) {
        val intent = viewModel.getWebBotIntent()
        startActivity(intent)

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key?.equals(PreferenceHelper.BRIDGE_TYPE) == true) {
            binding.rgBridgeOptions.check(viewModel.getSelectedBridgeTypeId())
        }
    }

}