package org.torproject.vpn.ui.bridgesettings.view

import android.graphics.drawable.Drawable
import android.text.SpannableString
import androidx.databinding.BindingAdapter
import kotlinx.coroutines.flow.StateFlow

object ViewBindingUtils {

    @BindingAdapter("secondaryText")
    @JvmStatic
    fun setSecondaryText(view: BridgeRequestEntryView, spannableString: SpannableString) {
        view.setSpannableForSecondaryText(spannableString)
    }

    @BindingAdapter("drawable")
    @JvmStatic
    fun setDrawable(view: BridgeRequestEntryView, flow: StateFlow<Drawable?>) {
        view.setDrawableStateFlowForIcon(flow)
    }
}