package com.example.torwitharti.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.torwitharti.R
import com.example.torwitharti.databinding.FragmentLogTabBinding
import com.example.torwitharti.databinding.FragmentReportBinding
import com.example.torwitharti.databinding.FragmentReportTabBinding
import com.example.torwitharti.ui.home.model.ReportFragmentViewModel
import com.google.android.material.tabs.TabLayoutMediator


/**
 *
 * shows the reports after the connection is made
 */
class ReportFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentReportBinding.inflate(inflater, container, false)
        val viewModel = ViewModelProvider(this)[ReportFragmentViewModel::class.java]
        binding.viewModel = viewModel
        binding.pagerReportFrag.adapter = ReportPagerAdapter(this@ReportFragment)

        TabLayoutMediator(binding.tabLayoutReport, binding.pagerReportFrag) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.frag_report_tab1)
                1 -> getString(R.string.frag_report_tab2)
                else -> ""
            }
        }.attach()

        return binding.root
    }

    class ReportPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount() = 2

        override fun createFragment(position: Int): Fragment {
            return if (position == 0) {
                ReportTabFragment()
            } else {
                LogsTabFragment()
            }
        }
    }
}


class ReportTabFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val reportFragmentViewModel by viewModels<ReportFragmentViewModel>({ requireParentFragment() })
        val binding = FragmentReportTabBinding.inflate(inflater, container, false)
        binding.viewModel = reportFragmentViewModel
        return binding.root
    }
}

class LogsTabFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val reportFragmentViewModel by viewModels<ReportFragmentViewModel>({ requireParentFragment() })
        val binding = FragmentLogTabBinding.inflate(inflater, container, false)
        binding.viewModel = reportFragmentViewModel
        return binding.root
    }
}