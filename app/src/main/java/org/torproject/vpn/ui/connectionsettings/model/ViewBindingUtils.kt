package org.torproject.vpn.ui.connectionsettings.model

import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData


object ViewBindingUtils {

    @BindingAdapter("layout_height")
    @JvmStatic
    fun setLayoutHeight(view: ConstraintLayout, liveData: LiveData<Int>) {
        liveData.value?.let {
            val layoutParams = view.layoutParams
            layoutParams.height = it
            view.layoutParams = layoutParams
        }
    }

    @BindingAdapter("visibility")
    @JvmStatic
    fun setVisibility(view: ConstraintLayout, liveData: LiveData<Int>) {
        liveData.value?.let {
            if (it != View.VISIBLE && it != View.INVISIBLE && it != View.GONE) {
                return
            }
            view.visibility = it
        }
    }

    @BindingAdapter("alpha")
    @JvmStatic
    fun setAlpha(view: ConstraintLayout, liveData: LiveData<Float>) {
        liveData.value?.let {
            view.alpha = it
        }
    }

}