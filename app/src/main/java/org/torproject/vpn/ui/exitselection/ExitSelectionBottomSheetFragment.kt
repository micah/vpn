package org.torproject.vpn.ui.exitselection

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentExitSelectionBottomSheetBinding
import org.torproject.vpn.ui.exitselection.data.ExitNodeAdapter
import org.torproject.vpn.ui.exitselection.model.ExitSelectionBottomSheetViewModel
import org.torproject.vpn.utils.getDpInPx

class ExitSelectionBottomSheetFragment : BottomSheetDialogFragment() {

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

    override fun getTheme(): Int = R.style.bottom_sheet_dialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = BottomSheetDialog(requireContext(), theme)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (dialog as? BottomSheetDialog)?.behavior?.let { behavior ->
            behavior.skipCollapsed = true
            behavior.state = STATE_EXPANDED
        }

        viewModel.fitHalfExpandedContent.observe(viewLifecycleOwner) { fitContent ->
            (dialog as? BottomSheetDialog)?.behavior?.let { behavior ->
                behavior.isFitToContents = fitContent
                behavior.expandedOffset = if (fitContent) 0 else 200
                binding.rvExitNodes.updatePadding(0, 0, 0, if (fitContent) getDpInPx(view.context, -2.5f) else 200)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        adapter.onExitNodeSelected = null
        adapter.onAutomaticExitNodeChanged = null
    }

}