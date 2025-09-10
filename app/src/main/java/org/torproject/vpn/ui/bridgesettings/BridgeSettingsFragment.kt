package org.torproject.vpn.ui.bridgesettings

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentBridgesettingsBinding
import org.torproject.vpn.ui.bridgebot.BridgeBotFragment
import org.torproject.vpn.ui.bridgesettings.model.BridgeSettingsFragmentViewModel
import org.torproject.vpn.ui.home.ConnectFragment.Companion.TRIGGER_ACTION_CONNECT
import org.torproject.vpn.utils.PreferenceHelper
import org.torproject.vpn.utils.navigateSafe

class BridgeSettingsFragment : Fragment(R.layout.fragment_bridgesettings), ClickHandler,
    OnSharedPreferenceChangeListener {

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
        
        // Set up radio button custom views
        setupRadioButtonViews()
        updateSelectedRadioButton()
        
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.switchUseBridge.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.onUseBridgeChanged(buttonView, isChecked)
        }

        parentFragmentManager.setFragmentResultListener(
            BridgeBotFragment.REQUEST_KEY_BRIDGE_SAVE_RESULT,
            viewLifecycleOwner
        ) { requestKey, bundle ->
            if (bundle.getBoolean(BridgeBotFragment.BUNDLE_KEY_SAVE_SUCCESSFUL)) {
                Snackbar.make(requireView(), R.string.bridges, Snackbar.LENGTH_LONG)
                    .setAction(R.string.action_connect) {
                        startVPN()
                    }
                    .show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val cutoutInsets = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
            binding.llContentContainer.setPadding(
                cutoutInsets.left,
                0,
                cutoutInsets.right,
                0
            )
            return@setOnApplyWindowInsetsListener windowInsets
        }
    }

    override fun onResume() {
        super.onResume()
        updateSelectedRadioButton()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.preferenceHelper.unregisterListener(this)
        _binding = null
    }

    fun startVPN() {
        // Navigate to ConnectFragment and trigger connect action
        parentFragmentManager.setFragmentResult(TRIGGER_ACTION_CONNECT, Bundle())
        findNavController().navigate(R.id.navigation_connect)
    }

    override fun onManualBridgeSelectionClicked(v: View) {
        findNavController().navigateSafe(R.id.action_navigation_bridgeSettings_to_bridgeLines)
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
            updateSelectedRadioButton()
        }
    }

    private fun setupRadioButtonViews() {
        // Set up click listeners for custom radio button views
        binding.radioObfs4.setOnRadioButtonClickListener {
            clearAllRadioButtons()
            binding.radioObfs4.isChecked = true
            viewModel.selectBuiltInObfs4()
        }

        binding.radioSnowflake.setOnRadioButtonClickListener {
            clearAllRadioButtons()
            binding.radioSnowflake.isChecked = true
            viewModel.selectBuiltInSnowflake()
        }

        binding.radioManual.setOnRadioButtonClickListener {
            clearAllRadioButtons()
            binding.radioManual.isChecked = true
            viewModel.selectManualBridge()
        }

        // Set up edit icon click listener for manual bridge
        binding.radioManual.setOnEditIconClickListener {
            onManualBridgeSelectionClicked(binding.radioManual)
        }
    }

    private fun clearAllRadioButtons() {
        binding.radioObfs4.isChecked = false
        binding.radioSnowflake.isChecked = false
        binding.radioManual.isChecked = false
    }

    private fun updateSelectedRadioButton() {
        clearAllRadioButtons()
        when (viewModel.getSelectedBridgeTypeId()) {
            R.id.radio_obfs4 -> binding.radioObfs4.isChecked = true
            R.id.radio_snowflake -> binding.radioSnowflake.isChecked = true
            R.id.radio_manual -> binding.radioManual.isChecked = true
        }
    }

}

