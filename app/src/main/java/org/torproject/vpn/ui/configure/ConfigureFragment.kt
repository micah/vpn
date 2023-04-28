package org.torproject.vpn.ui.configure

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentConfigureBinding
import org.torproject.vpn.ui.configure.model.ConfigureFragmentViewModel

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.alwaysOnVpn.visibility = VISIBLE
        }
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

    override fun onAlwaysOnClicked(v: View) {
        Log.d(TAG, "on always-on clicked")
        val intent = Intent("android.net.vpn.SETTINGS")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}