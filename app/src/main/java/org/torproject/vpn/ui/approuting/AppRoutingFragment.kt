package org.torproject.vpn.ui.approuting

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.torproject.onionmasq.OnionMasq
import org.torproject.onionmasq.errors.ProxyStoppedException
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentAppRoutingBinding
import org.torproject.vpn.ui.approuting.data.AppListAdapter
import org.torproject.vpn.ui.approuting.data.TorAppsAdapter
import org.torproject.vpn.ui.approuting.model.AppRoutingViewModel
import org.torproject.vpn.ui.base.view.BaseDialogFragment
import org.torproject.vpn.utils.PreferenceHelper
import org.torproject.vpn.utils.PreferenceHelper.Companion.PROTECTED_APPS
import org.torproject.vpn.utils.PreferenceHelper.Companion.PROTECT_ALL_APPS

class AppRoutingFragment : Fragment(R.layout.fragment_app_routing), SharedPreferences.OnSharedPreferenceChangeListener {

    private val TAG = AppRoutingFragment::class.java.simpleName
    private lateinit var viewModel: AppRoutingViewModel
    private var appListAdapter: AppListAdapter? = null
    private lateinit var preferenceHelper: PreferenceHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferenceHelper = PreferenceHelper(requireContext())
        viewModel = ViewModelProvider(this)[AppRoutingViewModel::class.java]
        viewModel.loadApps()
        val binding = FragmentAppRoutingBinding.bind(view)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        preferenceHelper.registerListener(this)

        // setup vertical list
        appListAdapter = AppListAdapter(viewModel.getAppList(),
            TorAppsAdapter(viewModel.getAppList()),
            preferenceHelper)
        appListAdapter?.onItemModelChanged = viewModel::onItemModelChanged
        binding.rvAppList.adapter = appListAdapter
        viewModel.getObservableAppList().observe(viewLifecycleOwner) { appListAdapter?.update(it) }
        viewModel.getObservableProgress().observe(viewLifecycleOwner) { isLoading ->
            binding.progressIndicator.visibility = if (isLoading) VISIBLE else GONE
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.refresh_circuits -> {
                    if (preferenceHelper.warningsEnabled) {
                        val dialog = BaseDialogFragment.createRefreshAllCircuitsDialog()
                        dialog.show(parentFragmentManager, "REFRESH_CIRCUITS_DIALOG")
                    } else {
                        try {
                            OnionMasq.refreshCircuits()
                        } catch (e: ProxyStoppedException) {
                            e.printStackTrace()
                        }
                    }

                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        appListAdapter?.onItemModelChanged = null
        appListAdapter = null
        preferenceHelper.unregisterListener(this)
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences?, key: String?) {
        if (key?.equals(PROTECT_ALL_APPS) == true) {
            viewModel.onProtectAllAppsPrefsChanged(preferenceHelper.protectAllApps)
        }
        if (key?.equals(PROTECTED_APPS) == true) {
            viewModel.updateVPNSettings()
        }
    }

}