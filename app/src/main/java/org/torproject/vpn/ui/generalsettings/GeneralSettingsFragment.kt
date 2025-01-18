package org.torproject.vpn.ui.generalsettings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentGeneralsettingsBinding
import org.torproject.vpn.ui.generalsettings.data.LauncherAdapter
import org.torproject.vpn.ui.generalsettings.model.GeneralSettingsFragmentViewModel

// TODO: Rename to `IconSettingFragment` and remove warning section
class GeneralSettingsFragment: Fragment(R.layout.fragment_generalsettings),
    SharedPreferences.OnSharedPreferenceChangeListener, ClickHandler {
    private lateinit var viewModel: GeneralSettingsFragmentViewModel
    private var _binding: FragmentGeneralsettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[GeneralSettingsFragmentViewModel::class.java]

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGeneralsettingsBinding.bind(view)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.handler = this
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val adapter = LauncherAdapter(viewModel.launcherList.value!!, viewModel::onLauncherSelected)
        binding.layoutAppIcons.rvHorizontalAppIcons.adapter = adapter
        viewModel.launcherList.observe(viewLifecycleOwner) {
            adapter.update(it)
        }

        binding.warningsSettingsEntry.isChecked = viewModel.warningEnabled
        binding.warningsSettingsEntry.setOnCheckedChangeListener(viewModel::onWarningsSettingsChanged)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.layoutAppIcons.rvHorizontalAppIcons.adapter = null
        _binding = null
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        TODO("Not yet implemented")
    }
}