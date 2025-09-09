package org.torproject.vpn.ui.exitselection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentExitSelectionBinding
import org.torproject.vpn.ui.exitselection.data.ExitNodeAdapter
import org.torproject.vpn.ui.exitselection.model.ExitSelectionFragmentViewModel

class ExitSelectionFragment : Fragment() {
    companion object {
            val REQUEST_KEY = ExitSelectionFragment::class.java.simpleName
    }

    private lateinit var viewModel: ExitSelectionFragmentViewModel
    private lateinit var adapter: ExitNodeAdapter
    private var _binding: FragmentExitSelectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[ExitSelectionFragmentViewModel::class.java]
        _binding = FragmentExitSelectionBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        adapter = ExitNodeAdapter(viewModel.list, viewLifecycleOwner)
        adapter.onExitNodeSelected = viewModel::onExitNodeSelected

        binding.toolbar.setNavigationOnClickListener {
            setFragmentResult(REQUEST_KEY, bundleOf(REQUEST_KEY to viewModel.preferenceChanged.value))
            findNavController().popBackStack()
        }

        val dividerItemDecoration = DividerItemDecoration (
            binding.rvExitNodes.context,
            LinearLayoutManager.VERTICAL
        )
        ContextCompat.getDrawable(binding.root.context, R.drawable.divider)?.let {
            dividerItemDecoration.setDrawable(it)
        }

        viewModel.automaticExitNodeSelected.observe(viewLifecycleOwner) {
            binding.smProtectAllApps.isChecked = it
        }
        binding.automaticContainer.setOnClickListener {
            viewModel.onAutomaticExitNodeChanged(true)
        }

        binding.rvExitNodes.adapter = adapter
        binding.rvExitNodes.addItemDecoration(dividerItemDecoration)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutoutInsets = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
            binding.rvExitNodes.setPadding(
                cutoutInsets.left,
                0,
                cutoutInsets.right,
                insets.bottom
            )
            return@setOnApplyWindowInsetsListener windowInsets
        }

        viewModel.requestExitNodes()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        adapter.onExitNodeSelected = null
    }

}