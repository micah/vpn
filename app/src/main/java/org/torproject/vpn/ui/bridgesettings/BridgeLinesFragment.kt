package org.torproject.vpn.ui.bridgesettings

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import org.torproject.vpn.R
import org.torproject.vpn.databinding.ChipItemBinding
import org.torproject.vpn.databinding.FragmentBridgelinesBinding
import org.torproject.vpn.ui.bridgesettings.model.BridgeLinesFragmentViewModel

class BridgeLinesFragment: Fragment(R.layout.fragment_bridgelines), OnSharedPreferenceChangeListener {

    companion object {
        val TAG = BridgeLinesFragment::class.java.simpleName
    }

    private lateinit var viewModel: BridgeLinesFragmentViewModel
    private var _binding: FragmentBridgelinesBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[BridgeLinesFragmentViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBridgelinesBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.save -> {
                    viewModel.save()
                    findNavController().popBackStack()
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
        binding.textinputBridges.doAfterTextChanged { text ->
            if (text == null || text.isBlank() || !text.contains("\n")) {
                return@doAfterTextChanged
            }
            val trimmed = removeEmptyLines(text)
            val lastLineEndsWithNewLine = text.endsWith("\n")
            val bridgeEntries = trimmed.split("\n")

            for (i in 0 until bridgeEntries.size - 1) {
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
        val imm = binding.root.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.textinputBridges, 0);

        viewLifecycleOwner.lifecycleScope.launch {
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

    private fun removeEmptyLines(myString: CharSequence): CharSequence {
        val regex = Regex("\n\\s*\n")
        return myString.replace(regex, "")

    }

    private fun handleBridgeLine(binding: FragmentBridgelinesBinding, value: String) {
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

    private fun addChipView(rootBinding: FragmentBridgelinesBinding, value: String) {
        val chipBinding = ChipItemBinding.inflate(layoutInflater)
        chipBinding.chip.text = value
        chipBinding.chip.setOnCloseIconClickListener {
            viewModel.removeBridgeLine(chipBinding.chip.text!!.toString())
            rootBinding.chipContainer.removeView(chipBinding.chip)
        }
        context?.let {
            chipBinding.chip.closeIconContentDescription = it.getString(R.string.remove_bridge);
        }
        rootBinding.chipContainer.addView(chipBinding.chip)
        chipBinding.chip.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
    }
}
