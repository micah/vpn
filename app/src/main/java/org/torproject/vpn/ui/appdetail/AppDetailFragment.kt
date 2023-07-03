package org.torproject.vpn.ui.appdetail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentAppDetailBinding
import org.torproject.vpn.ui.appdetail.model.AppDetailFragmentViewModel
import org.torproject.vpn.ui.base.view.BaseDialogFragment
import org.torproject.vpn.ui.glide.ApplicationInfoModel


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
            viewModel.hasTorSupport.value = arguments.argHasTorSupport        }
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
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.refresh_circuits -> {
                    viewModel.appUID.value?.let { appUID ->
                        val dialog = BaseDialogFragment.createRefreshCircuitsForAppDialog(appUID)
                        dialog.show(parentFragmentManager, "REFRESH_CIRCUITS_DIALOG")
                        return@setOnMenuItemClickListener true
                    }
                    return@setOnMenuItemClickListener false
                }
                else -> return@setOnMenuItemClickListener false
            }

        }
    }
}