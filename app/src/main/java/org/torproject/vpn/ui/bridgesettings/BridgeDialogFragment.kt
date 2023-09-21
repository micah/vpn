package org.torproject.vpn.ui.bridgesettings

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color.TRANSPARENT
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.Window
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import org.torproject.vpn.R
import org.torproject.vpn.databinding.ChipItemBinding
import org.torproject.vpn.databinding.FragmentBridgesDialogBinding
import org.torproject.vpn.ui.bridgesettings.model.BridgeDialogFragmentViewModel


class BridgeDialogFragment : DialogFragment(R.layout.fragment_bridges_dialog) {

    companion object {
        fun create(): BridgeDialogFragment {
            return BridgeDialogFragment()
        }
    }

    private lateinit var viewModel: BridgeDialogFragmentViewModel
    private var _binding: FragmentBridgesDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[BridgeDialogFragmentViewModel::class.java]

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentBridgesDialogBinding.inflate(LayoutInflater.from(requireContext()))
        bind(binding)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)
        val dialog = builder.create()
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(TRANSPARENT))
            requestFeature(Window.FEATURE_NO_TITLE)
        }

        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.helperText.collect { helperText ->
                    binding.tvHelpline.text = helperText
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun bind(binding: FragmentBridgesDialogBinding) {
        binding.tvActionCancel.setOnClickListener{ _ ->
            dismiss()
        }

        binding.tvAction.setOnClickListener{ _ ->
            viewModel.save()
            dismiss()
        }

        binding.textinputBridges.doAfterTextChanged { text ->
            if (text == null || text.isBlank() || !text.contains("\n")) {
                return@doAfterTextChanged
            }
            val trimmed = removeEmptyLines(text)
            val lastLineEndsWithNewLine = text.endsWith("\n")
            val bridgeEntries = trimmed.split("\n")

            for (i in 0 until bridgeEntries.size-1) {
                handleBridgeLine(binding, bridgeEntries[i])
            }

            if (lastLineEndsWithNewLine) {
                handleBridgeLine(binding, bridgeEntries.last())
                binding.textinputBridges.text = null
            } else {
                binding.textinputBridges.setText(bridgeEntries.last())
                binding.textinputBridges.text?.let { binding.textinputBridges.setSelection(it.length) }
            }

        }

        viewModel.bridgeLines.value?.let { initialBridgeLines ->
            for (line in initialBridgeLines) {
                addChipView(binding, line)
            }
        }

        binding.textinputBridges.requestFocus()
    }

    private fun removeEmptyLines(myString: CharSequence): CharSequence {
        val regex = Regex("\n\\s*\n")
        return myString.replace(regex, "")

    }

    private fun handleBridgeLine(binding: FragmentBridgesDialogBinding, value: String) {
        val trimmedLine = value.trim { it <= ' ' }
        if (isValidBridgeLine(trimmedLine)) {
            viewModel.addBridgeLine(trimmedLine)
            addChipView(binding, trimmedLine)
        }
    }

    private fun isValidBridgeLine(value: String): Boolean {
        // TODO: evaluate input
        return value.isNotBlank()
    }

    private fun addChipView(rootBinding: FragmentBridgesDialogBinding, value: String) {
        val chipBinding = ChipItemBinding.inflate(layoutInflater)
        chipBinding.chip.text = value
        chipBinding.chip.setOnCloseIconClickListener {
            viewModel.removeBridgeLine(chipBinding.chip.text!!.toString())
            rootBinding.chipContainer.removeView(chipBinding.chip)
        }
        rootBinding.chipContainer.addView(chipBinding.chip)
        chipBinding.chip.layoutParams.width = MATCH_PARENT
    }

}
