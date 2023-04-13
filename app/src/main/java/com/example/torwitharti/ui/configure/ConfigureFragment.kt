package com.example.torwitharti.ui.configure

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.torwitharti.R
import com.example.torwitharti.databinding.FragmentConfigureBinding
import com.example.torwitharti.ui.configure.model.ConfigureFragmentViewModel

class ConfigureFragment : Fragment(), ClickHandler {
    companion object {
         val TAG: String = ConfigureFragment::class.java.simpleName
    }

    private lateinit var binding: FragmentConfigureBinding
    private lateinit var configureFragmentViewModel: ConfigureFragmentViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        configureFragmentViewModel =
            ViewModelProvider(this)[ConfigureFragmentViewModel::class.java]

        binding = FragmentConfigureBinding.inflate(inflater, container, false)
        binding.viewModel = configureFragmentViewModel
        binding.handler = this
        return  binding.root
    }

    override fun onAppsClicked(v: View) {
        Log.d(TAG, "apps entry clicked")
        findNavController().navigate(R.id.action_configureFragment_to_appRoutingFragment)
    }

    override fun onTorLogsClicked(v: View) {
        Log.d(TAG, "tor entry clicked")
        findNavController().navigate(R.id.action_configureFragment_to_LoggingFragment)
    }
}