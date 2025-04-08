package org.torproject.vpn.ui.bridgesettings.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.torproject.vpn.R
import org.torproject.vpn.databinding.BridgeRequestEntryViewBinding
import org.torproject.vpn.utils.getLifeCycleOwner

/**
 * BridgeRequestEntry used in Bridges settings screen
 */
class BridgeRequestEntryView : ConstraintLayout {

    private val binding by lazy {
        BridgeRequestEntryViewBinding.inflate(LayoutInflater.from(context), this, true)
    }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.BridgeRequestEntryView, defStyle, 0
        )
        binding.tvTitle.text = a.getString(R.styleable.BridgeRequestEntryView_text)
        binding.tvSubtitle.text = a.getString(R.styleable.BridgeRequestEntryView_secondaryText)
        binding.ivArrow.isVisible = a.getBoolean(R.styleable.BridgeRequestEntryView_external, true)
        binding.ivIcon.setImageDrawable(a.getDrawable(R.styleable.BridgeRequestEntryView_drawable))
        a.recycle()
    }

    fun setSpannableForSecondaryText(spannable: Spannable) {
        binding.tvSubtitle.text = spannable
    }

    fun setDrawableStateFlowForIcon(flow: StateFlow<Drawable?>) {
        binding.ivIcon.setImageDrawable(flow.value)
    }
}