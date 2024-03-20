package org.torproject.vpn.ui.bridgesettings

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.ViewUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.torproject.vpn.R
import org.torproject.vpn.circumvention.CircumventionApiManager
import org.torproject.vpn.circumvention.SettingsRequest
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

    /*
        This functionality is still very alpha. We're requesting bridge lines from the circumvention API,
        but instead of evaluating the assumed device's country location, the country code of RU is hard coded
        in order to enforce to receive actual results. For requested (presumably) uncensored locations an
        empty array gets returned by the circumvention API. Moreover this feature doesn't use any
        circumvention strategy itself for the API communication. Finally the returned bridge lines cannot be
        used in reality to start tor, since PT support is not yet available. Only bridges without obfuscation
        are currently supported.
     */
    private fun askTor () {
        CircumventionApiManager().getSettings(SettingsRequest("ru"), {
            it?.let { response ->
                Log.d("result: ", "${response.settings}")
                val results = mutableListOf<String>()
                response.settings?.let { bridgesList ->
                    for (bridges in bridgesList) {
                        bridges.bridges.bridge_strings?.let { bridgeLines ->
                            results.addAll(bridgeLines)
                        }
                    }
                }
                val dialog = BridgeDialogFragment.create(
                    BridgeDialogFragmentArgs(
                        argBridgeLines = results.toTypedArray()
                    )
                )
                dialog.show(parentFragmentManager, "BRIDGE_DIALOG")
            }
        }, {
            Log.e(TAG, "Ask Tor was not available... $it")
            Toast.makeText(requireContext(),"Ask Tor was not available",Toast.LENGTH_LONG).show()
        })
    }

}