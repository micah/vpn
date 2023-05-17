package org.torproject.vpn.ui.connectionsettings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentConnectionsettingsBinding
import org.torproject.vpn.ui.connectionsettings.model.ConnectionFragmentViewModel

class ConnectionFragment: Fragment(), ClickHandler {

    private lateinit var binding: FragmentConnectionsettingsBinding
    private lateinit var configureFragmentViewModel: ConnectionFragmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        configureFragmentViewModel = ViewModelProvider(this)[ConnectionFragmentViewModel::class.java]
        binding = FragmentConnectionsettingsBinding.inflate(inflater, container, false)

        binding.viewModel = configureFragmentViewModel
        binding.handler = this

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.killSwitch.visibility = View.VISIBLE
        }

        binding.quickstart.isChecked = configureFragmentViewModel.startOnBoot
        binding.quickstart.setOnCheckedChangeListener(configureFragmentViewModel::onStartOnBootChanged)

        return binding.root
    }

    override fun onTorLogsClicked(v: View) {
        findNavController().navigate(R.id.action_connectionFragment_to_LoggingFragment)
    }

    override fun onAlwaysOnClicked(v: View) {
        val intent = Intent("android.net.vpn.SETTINGS")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

}