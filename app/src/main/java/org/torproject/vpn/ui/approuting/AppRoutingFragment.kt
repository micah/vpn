package org.torproject.vpn.ui.approuting

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentAppRoutingBinding
import org.torproject.vpn.ui.approuting.data.AppListAdapter
import org.torproject.vpn.ui.approuting.data.TorAppsAdapter
import org.torproject.vpn.ui.approuting.model.AppRoutingViewModel
import org.torproject.vpn.ui.base.view.BaseDialogFragment
import org.torproject.vpn.utils.PreferenceHelper
import org.torproject.vpn.vpn.VpnStatusObservable

class AppRoutingFragment : Fragment(R.layout.fragment_app_routing) {

    private lateinit var viewModel: AppRoutingViewModel
    private var appListAdapter: AppListAdapter? = null
    private lateinit var preferenceHelper: PreferenceHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferenceHelper = PreferenceHelper(requireContext())
        viewModel = ViewModelProvider(this)[AppRoutingViewModel::class.java]
        val binding = FragmentAppRoutingBinding.bind(view)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // setup vertical list
        appListAdapter = AppListAdapter(
            torAppsAdapter = TorAppsAdapter(),
            preferenceHelper= preferenceHelper
        )
        appListAdapter?.onItemModelChanged = viewModel::onItemModelChanged
        appListAdapter?.onProtectAllAppsChanged = viewModel::onProtectAllAppsPrefsChanged
        binding.rvAppList.adapter = appListAdapter

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.rvAppList.setPadding(
                binding.rvAppList.paddingLeft,
                binding.rvAppList.paddingTop,
                binding.rvAppList.paddingRight,
                insets.bottom
            )
            return@setOnApplyWindowInsetsListener windowInsets
        }

        viewModel.getObservableProgress().observe(viewLifecycleOwner) { isLoading ->
            binding.progressIndicator.visibility = if (isLoading) VISIBLE else GONE
        }
        viewModel.getObservableAppList().observe(viewLifecycleOwner) {
            appListAdapter?.update(it)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.enableAllBridges.collect { enabled ->
                        appListAdapter?.updateProtectAllAppsSwitch(enabled)
                    }
                }
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        if (VpnStatusObservable.isVPNActive()) {
            binding.toolbar.inflateMenu(R.menu.app_routing_menu)
            binding.toolbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.refresh_circuits -> {
                        val dialog = BaseDialogFragment.createRefreshAllCircuitsDialog()
                        dialog.show(parentFragmentManager, "REFRESH_CIRCUITS_DIALOG")
                        return@setOnMenuItemClickListener true
                    }
                    else -> return@setOnMenuItemClickListener false
                }

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        appListAdapter?.onItemModelChanged = null
        appListAdapter?.onProtectAllAppsChanged = null
        appListAdapter = null
    }

}