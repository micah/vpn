package org.torproject.vpn.ui.bridgesettings

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentBridgesettingsBinding
import org.torproject.vpn.ui.bridgesettings.model.BridgeSettingsFragmentViewModel
import org.torproject.vpn.utils.PreferenceHelper

class BridgeSettingsFragment: Fragment(R.layout.fragment_bridgesettings), ClickHandler, OnSharedPreferenceChangeListener {

    private lateinit var viewModel: BridgeSettingsFragmentViewModel
    private var _binding: FragmentBridgesettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[BridgeSettingsFragmentViewModel::class.java]
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key?.equals(PreferenceHelper.BRIDGE_TYPE) == true) {
            binding.rgBridgeOptions.check(viewModel.getSelectedBridgeTypeId())
        }
    }

}