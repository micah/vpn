package com.example.torwitharti.ui.configure

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.torwitharti.databinding.FragmentConfigureBinding

import com.example.torwitharti.ui.configure.model.ConfigureFragmentViewModel

class ConfigureFragment : Fragment() {
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
        return  binding.root
    }
}