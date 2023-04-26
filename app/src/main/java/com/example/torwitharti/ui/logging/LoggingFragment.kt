package com.example.torwitharti.ui.logging

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.torwitharti.R
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
        binding.rvLogList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        logObservable.logListData.observe(viewLifecycleOwner, loggingListAdapter::update)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.copy -> {
                    val logs = logObservable.getLogStrings(true)
                    (requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).apply {
                        setPrimaryClip(ClipData.newPlainText(getString(R.string.app_name), logs))
                    }
                    // Android 13+ has its own Toasts to confirm copy to clipboard
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                        Toast.makeText(requireContext(), getString(R.string.copied), Toast.LENGTH_SHORT).show()
                    }
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
        return binding.root
    }
}