package com.example.torwitharti.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.torwitharti.databinding.FragmentAppRoutingBinding

class AppRoutingFragment : Fragment() {

    companion object {
        fun newInstance() = AppRoutingFragment()
    }

    private lateinit var viewModel: AppRoutingViewModel
    private var _binding: FragmentAppRoutingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewModel = ViewModelProvider(this)[AppRoutingViewModel::class.java]
        _binding = FragmentAppRoutingBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}