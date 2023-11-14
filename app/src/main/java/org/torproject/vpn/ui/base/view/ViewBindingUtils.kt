package org.torproject.vpn.ui.base.view

import android.text.SpannableString
import androidx.databinding.BindingAdapter
import kotlinx.coroutines.flow.StateFlow

object ViewBindingUtils {

    @BindingAdapter("secondaryText")
    @JvmStatic
    fun setSecondaryText(view: IconTextEntryView, flow: StateFlow<String>) {
        view.setStateFlowForSecondaryText(flow)
    }

}