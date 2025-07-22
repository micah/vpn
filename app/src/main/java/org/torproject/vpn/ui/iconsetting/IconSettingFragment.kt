package org.torproject.vpn.ui.iconsetting

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentIconsettingBinding
import org.torproject.vpn.ui.iconsetting.data.LauncherAdapter
import org.torproject.vpn.ui.iconsetting.model.IconSettingFragmentViewModel

class IconSettingFragment: Fragment(R.layout.fragment_iconsetting),
    SharedPreferences.OnSharedPreferenceChangeListener, ClickHandler {
    private lateinit var viewModel: IconSettingFragmentViewModel
    private var _binding: FragmentIconsettingBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[IconSettingFragmentViewModel::class.java]

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentIconsettingBinding.bind(view)
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