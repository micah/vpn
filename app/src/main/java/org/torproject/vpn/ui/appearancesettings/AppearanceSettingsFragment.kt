package org.torproject.vpn.ui.appearancesettings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentAppearancesettingsBinding
import org.torproject.vpn.ui.appearancesettings.data.LauncherAdapter
import org.torproject.vpn.ui.appearancesettings.data.WallpaperAdapter
import org.torproject.vpn.ui.appearancesettings.model.AppearanceSettingsFragmentViewModel

class AppearanceSettingsFragment: Fragment(R.layout.fragment_appearancesettings),
    SharedPreferences.OnSharedPreferenceChangeListener, ClickHandler {
    private lateinit var viewModel: AppearanceSettingsFragmentViewModel
    private var _binding: FragmentAppearancesettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[AppearanceSettingsFragmentViewModel::class.java]

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAppearancesettingsBinding.bind(view)
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

        val wallPaperAdapter = WallpaperAdapter(viewModel.wallpaperList.value!!, viewModel::onWallpaperSelected)
        binding.rvWallpapers.adapter = wallPaperAdapter
        viewModel.wallpaperList.observe(viewLifecycleOwner) {
            wallPaperAdapter.update(it)
        }
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