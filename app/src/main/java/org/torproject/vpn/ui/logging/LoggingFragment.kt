package org.torproject.vpn.ui.logging

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.google.android.material.snackbar.Snackbar
import org.torproject.onionmasq.logging.LogItem
import org.torproject.onionmasq.logging.LogObservable
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentLoggingBinding
import org.torproject.vpn.ui.logging.data.LoggingListAdapter

class LoggingFragment : Fragment(R.layout.fragment_logging) {


    private var logObservable = LogObservable.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val loggingListAdapter = LoggingListAdapter()
        val binding = FragmentLoggingBinding.bind(view)
        binding.rvLogList.adapter = loggingListAdapter
        binding.lifecycleOwner = viewLifecycleOwner
        binding.rvLogList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        logObservable.liveData.observe(viewLifecycleOwner) { _ ->
            loggingListAdapter.update(
                logObservable.logList
            )
        }

        loggingListAdapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                binding.rvLogList.scrollToPosition(loggingListAdapter.itemCount.minus(1))

            }
        })
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
                        Snackbar.make(binding.root, getString(R.string.copied_clipboard), Toast.LENGTH_SHORT).show()
                    }
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
    }
}