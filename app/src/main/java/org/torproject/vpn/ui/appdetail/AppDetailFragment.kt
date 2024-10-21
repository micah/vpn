package org.torproject.vpn.ui.appdetail

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import org.torproject.onionmasq.OnionMasq
import org.torproject.onionmasq.errors.ProxyStoppedException
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentAppDetailBinding
import org.torproject.vpn.ui.appdetail.data.CircuitCardAdapter
import org.torproject.vpn.ui.appdetail.model.AppDetailFragmentViewModel
import org.torproject.vpn.ui.base.view.BaseDialogFragment
import org.torproject.vpn.ui.glide.ApplicationInfoModel
import org.torproject.vpn.utils.PreferenceHelper


class AppDetailFragment : Fragment(R.layout.fragment_app_detail) {

    private lateinit var viewModel: AppDetailFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[AppDetailFragmentViewModel::class.java]

        arguments?.let {
            val arguments = AppDetailFragmentArgs.fromBundle(it)
            viewModel.appUID.value = arguments.argAppUID
            viewModel.appId.value = arguments.argAppId
            viewModel.appName.value = arguments.argAppName
            viewModel.isBrowser.value = arguments.argIsBrowser
            viewModel.hasTorSupport.value = arguments.argHasTorSupport
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAppDetailBinding.bind(view)
        arguments?.let {
            Glide.with(binding.root.context)
                .load(ApplicationInfoModel(viewModel.appId.value ?: ""))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(binding.ivAppIcon)
        }
        val preferenceHelper = PreferenceHelper(view.context)
        val adapter = CircuitCardAdapter(viewModel.appName.value!!, preferenceHelper)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.layoutNoTorSupport.rvCircuitCards.adapter = adapter
        binding.layoutNoTorSupport.rvCircuitCards.itemAnimator = CircuitCardItemAnimator()
        viewModel.circuitList.observe(viewLifecycleOwner) { list ->
            adapter.update(list)
            binding.layoutNoTorSupport.tvCircuits.visibility =
                if (list.isNotEmpty()) VISIBLE else GONE;
        }
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        val hasTorSupport = viewModel.hasTorSupport.value ?: false
        if (!hasTorSupport) {
            binding.toolbar.inflateMenu(R.menu.app_detail_menu)
            binding.toolbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.refresh_circuits -> {
                        viewModel.appUID.value?.let { appUID ->
                            if (preferenceHelper.warningsEnabled) {
                                val dialog = BaseDialogFragment.createRefreshCircuitsForAppDialog(appUID)
                                dialog.show(parentFragmentManager, "REFRESH_CIRCUITS_DIALOG")
                            } else {
                                try {
                                    OnionMasq.refreshCircuitsForApp(appUID.toLong())
                                } catch (e: ProxyStoppedException) {
                                    e.printStackTrace()
                                }
                            }

                            return@setOnMenuItemClickListener true
                        }
                        return@setOnMenuItemClickListener false
                    }
                    else -> return@setOnMenuItemClickListener false
                }
            }
        }
    }
}