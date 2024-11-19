package org.torproject.vpn.ui.exitselection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentExitSelectionBottomSheetBinding
import org.torproject.vpn.ui.exitselection.data.ExitNodeAdapter
import org.torproject.vpn.ui.exitselection.model.ExitSelectionBottomSheetViewModel

class ExitSelectionBottomSheetFragment : Fragment() {

    private lateinit var viewModel: ExitSelectionBottomSheetViewModel
    private lateinit var adapter: ExitNodeAdapter
    private var _binding: FragmentExitSelectionBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[ExitSelectionBottomSheetViewModel::class.java]
        _binding = FragmentExitSelectionBottomSheetBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        adapter = ExitNodeAdapter(viewModel.list, viewLifecycleOwner)
        adapter.onExitNodeSelected = viewModel::onExitNodeSelected
        adapter.onAutomaticExitNodeChanged = viewModel::onAutomaticExitNodeChanged

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val dividerItemDecoration = DividerItemDecoration (
            binding.rvExitNodes.context,
            LinearLayoutManager.VERTICAL
        )
        ContextCompat.getDrawable(binding.root.context, R.drawable.divider)?.let {
            dividerItemDecoration.setDrawable(it)
        }

        binding.rvExitNodes.adapter = adapter
        binding.rvExitNodes.addItemDecoration(dividerItemDecoration)
        viewModel.requestExitNodes()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        adapter.onExitNodeSelected = null
        adapter.onAutomaticExitNodeChanged = null
    }

}