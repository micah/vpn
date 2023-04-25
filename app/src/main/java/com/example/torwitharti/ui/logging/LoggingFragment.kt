package com.example.torwitharti.ui.logging

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.torwitharti.databinding.FragmentLoggingBinding
import com.example.torwitharti.ui.logging.data.LoggingListAdapter
import org.torproject.onionmasq.logging.LogObservable

class LoggingFragment : Fragment() {

    private lateinit var binding: FragmentLoggingBinding
    private lateinit var loggingListAdapter: LoggingListAdapter
    private var logObservable = LogObservable.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        loggingListAdapter = LoggingListAdapter()
        binding = FragmentLoggingBinding.inflate(inflater, container, false)
        binding.rvLogList.adapter = loggingListAdapter
        binding.lifecycleOwner = viewLifecycleOwner
        binding.rvLogList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        logObservable.logListData.observe(viewLifecycleOwner, loggingListAdapter::update)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        return binding.root
    }
}