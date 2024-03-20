package org.torproject.vpn.ui.base.view

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.databinding.BindingAdapter
import kotlinx.coroutines.flow.StateFlow

object ViewBindingUtils {

    @BindingAdapter("secondaryText")
    @JvmStatic
    fun setSecondaryText(view: IconTextEntryView, flow: StateFlow<String>) {
        view.setStateFlowForSecondaryText(flow)
    }

    @BindingAdapter("layout_constraintVertical_bias")
    @JvmStatic
    fun setLayoutConstraintVerticalBias(view: View, flow: StateFlow<Float>) {
        view.updateLayoutParams<ConstraintLayout.LayoutParams> { verticalBias = flow.value }
    }

    @BindingAdapter("textColor")
    @JvmStatic
    fun setLayoutConstraintVerticalBias(view: TextView, flow: StateFlow<Int>) {
        view.setTextColor(flow.value)
    }
}