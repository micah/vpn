package org.torproject.vpn.ui.help

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.torproject.vpn.R
import org.torproject.vpn.databinding.FragmentContactSupportBottomSheetBinding
import org.torproject.vpn.ui.help.model.ContactSupportFragmentModel

class ContactSupportBottomSheetFragment : BottomSheetDialogFragment(R.layout.fragment_contact_support_bottom_sheet), ContactSupportClickHandler {

    var viewModel: ContactSupportFragmentModel? = null
    override fun getTheme(): Int = R.style.bottom_sheet_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[ContactSupportFragmentModel::class.java]

    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = BottomSheetDialog(requireContext(), theme)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentContactSupportBottomSheetBinding.bind(view)
        binding.handler = this
        (dialog as? BottomSheetDialog)?.behavior?.let { behavior ->
            behavior.skipCollapsed = true
        }
        // the following subscriptions are a workaround since data binding within
        // the layout file and the view model failed
        // TODO: refactor this
        viewModel?.let { model ->
            model.load()
            model.emailActivityInfo.observe(viewLifecycleOwner) { info ->
                if (info != null) {
                    binding.ivAppImageEmail.setImageDrawable(model.getDrawable(info, R.drawable.ic_dummy_app))
                } else {
                    context?.let {
                        binding.ivAppImageEmail.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.ic_dummy_app))
                    }
                }
            }
            model.telegramActivityInfo?.observe(viewLifecycleOwner) { info ->
                if (info != null) {
                    binding.ivAppImageTelegram.setImageDrawable(model.getDrawable(info, R.drawable.ic_telegram_default))
                    binding.tvAppTitleTelegram.text = model.getText(info)
                    model.setCustomTelegramURLScheme()
                } else {
                    context?.let {
                        binding.ivAppImageTelegram.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.ic_telegram_default))
                        binding.tvAppTitleTelegram.text = ContextCompat.getString(it, R.string.telegram)
                    }
                }
            }
            model.whatsAppActivityInfo.observe(viewLifecycleOwner) { info ->
                if (info != null) {
                    binding.ivAppImageWhatsApp.setImageDrawable(model.getDrawable(info, R.drawable.ic_whatsapp_default))
                    binding.tvAppTitleWhatsApp.text = model.getText(info)
                    model.setCustomWhatsappURLScheme()
                } else {
                    context?.let {
                        binding.ivAppImageWhatsApp.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.ic_whatsapp_default))
                        binding.tvAppTitleWhatsApp.text = ContextCompat.getString(it, R.string.whatsapp)
                    }
                }
            }
            model.signalActivityInfo.observe(viewLifecycleOwner) { info ->
                if (info != null) {
                    binding.ivAppImageSignal.setImageDrawable(model.getDrawable(info, R.drawable.ic_signal_default))
                    binding.tvAppTitleSignal.text = model.getText(info)
                    model.setCustomSignalURLScheme()
                } else {
                    context?.let {
                        binding.ivAppImageSignal.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.ic_signal_default))
                        binding.tvAppTitleSignal.text = ContextCompat.getString(it, R.string.signal)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel = null
    }

    override fun onWhatsappClicked(v: View) {
        viewModel?.whatsappIntent?.let { intent ->
            startActivity(intent)
        }
    }

    override fun onSignalClicked(v: View) {
        viewModel?.signalIntent?.let { intent ->
            startActivity(intent)
        }
    }

    override fun onTelegramClicked(v: View) {
        viewModel?.telegramIntent?.let { intent ->
            startActivity(intent)
        }
    }

    override fun onEmailClicked(v: View) {
        viewModel?.emailIntent?.let { intent ->
            startActivity(intent)
        }
    }

}
