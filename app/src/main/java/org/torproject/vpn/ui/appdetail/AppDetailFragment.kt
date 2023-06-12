package org.torproject.vpn.ui.appdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentAppDetailBinding
import org.torproject.vpn.ui.appdetail.model.AppDetailFragmentViewModel
import org.torproject.vpn.ui.glide.ApplicationInfoModel


class AppDetailFragment : Fragment(R.layout.fragment_app_detail) {

    companion object {
        val ARG_APP_ID = "argAppId"
        val ARG_APP_NAME = "argAppName"
    }

    private lateinit var appDetailFragmentViewModel: AppDetailFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appDetailFragmentViewModel = ViewModelProvider(this)[AppDetailFragmentViewModel::class.java]

        arguments?.let {
            val arguments = AppDetailFragmentArgs.fromBundle(it)
            appDetailFragmentViewModel.appId.value = arguments.argAppId
            appDetailFragmentViewModel.appName.value = arguments.argAppName
            appDetailFragmentViewModel.isBrowser.value = arguments.argIsBrowser
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAppDetailBinding.bind(view)
        arguments?.let {
            Glide.with(binding.root.context)
                .load(ApplicationInfoModel(appDetailFragmentViewModel.appId.value!!))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(binding.ivAppIcon)
        }
        binding.viewModel = appDetailFragmentViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }
}