package org.torproject.vpn.ui.base.view

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color.TRANSPARENT
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import androidx.fragment.app.DialogFragment
import org.torproject.onionmasq.OnionMasq
import org.torproject.onionmasq.errors.ProxyStoppedException
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentDialogBinding

class BaseDialogFragment : DialogFragment(R.layout.fragment_dialog) {

    companion object {
        const val VIEW_TYPE: String = "viewType"
        const val EXTRA_APP_UID: String = "extra_app_uid"

        const val TYPE_REFRESH_ALL_CIRCUITS = 0
        const val TYPE_REFRESH_APP_CIRCUIT = 1


        fun createRefreshAllCircuitsDialog(): BaseDialogFragment {
            val fragment = BaseDialogFragment()
            val args = Bundle()
            args.putInt(VIEW_TYPE, TYPE_REFRESH_ALL_CIRCUITS)
            fragment.arguments = args
            return fragment
        }

        fun createRefreshCircuitsForAppDialog(appUID: Int): BaseDialogFragment {
            val fragment = BaseDialogFragment()
            val args = Bundle()
            args.putInt(VIEW_TYPE, TYPE_REFRESH_APP_CIRCUIT)
            args.putInt(EXTRA_APP_UID, appUID)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = FragmentDialogBinding.inflate(LayoutInflater.from(requireContext()))

        bind(requireArguments().getInt(VIEW_TYPE), binding)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)
        val dialog = builder.create()
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(TRANSPARENT))
            requestFeature(Window.FEATURE_NO_TITLE)
        }


        return dialog
    }

    private fun bind(type: Int, binding: FragmentDialogBinding) {
        binding.tvActionCancel.setOnClickListener{ _ ->
            dismiss()
        }
        binding.tvAction.setText(R.string.action_refresh_circuits)

        when (type) {
            TYPE_REFRESH_ALL_CIRCUITS -> {
                binding.ivHeader.setImageResource(R.drawable.ic_all_new_circuits)
                binding.tvHeader.setText(R.string.reload_all_circuits)
                binding.tvDescription.setText(R.string.reload_all_circuits_description)
                binding.tvAction.setText(R.string.action_refresh_circuits)
                binding.tvAction.setOnClickListener { _ ->
                    try {
                        OnionMasq.refreshCircuits()
                    } catch (e: ProxyStoppedException) {
                        e.printStackTrace()
                    }
                    dismiss()
                }
            }
            TYPE_REFRESH_APP_CIRCUIT -> {
                binding.ivHeader.setImageResource(R.drawable.ic_new_circuit)
                binding.tvHeader.setText(R.string.reload_app_circuits)
                binding.tvDescription.setText(R.string.reload_app_circuits_description)
                binding.tvAction.setText(R.string.action_refresh)
                binding.tvAction.setOnClickListener { _ ->
                    arguments?.getInt(EXTRA_APP_UID)?.let {
                        try {
                            OnionMasq.refreshCircuitsForApp(it.toLong())
                        } catch (e: ProxyStoppedException) {
                            e.printStackTrace()
                        }
                    }
                    dismiss()
                }
            } else -> {}
        }
    }

}
