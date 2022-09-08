package com.example.torwitharti.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.torwitharti.databinding.FragmentAppRoutingBinding
import com.example.torwitharti.utils.PreferenceHelper

class AppRoutingFragment : Fragment() {

    companion object {
        fun newInstance() = AppRoutingFragment()
    }

    private val TAG = AppRoutingFragment.javaClass.simpleName
    private lateinit var viewModel: AppRoutingViewModel
    private lateinit var appListAdapter: AppListAdapter
    private var _binding: FragmentAppRoutingBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferenceHelper: PreferenceHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        preferenceHelper = PreferenceHelper(requireContext())
        viewModel = ViewModelProvider(this)[AppRoutingViewModel::class.java]
        _binding = FragmentAppRoutingBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // setup Protect all my apps switch
        binding.smProtectAllApps.isChecked = preferenceHelper.protectAllApps
        binding.smProtectAllApps.setOnCheckedChangeListener { compoundButton, state ->
            if (compoundButton.isPressed) {
                preferenceHelper.protectAllApps = state
            }
        }

        // setup vertical list
        appListAdapter = AppListAdapter(viewModel.getAppList(),
            TorAppsAdapter(viewModel.getAppList()),
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false))
        appListAdapter.onItemModelChanged = viewModel::onItemModelChanged
        binding.rvAppList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvAppList.adapter = appListAdapter
        viewModel.getObservableAppList().observe(viewLifecycleOwner, appListAdapter::update)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        appListAdapter.onItemModelChanged = null
    }

}