package org.torproject.vpn.ui.base.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton.OnCheckedChangeListener
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.coroutines.flow.StateFlow
import org.torproject.vpn.R
import org.torproject.vpn.databinding.IconTextEntryViewBinding

/**
 * Configuration entry used in ConfigureFragment and ConnectionFragment
 */
class IconTextEntryView : ConstraintLayout {

    private val binding by lazy {
        IconTextEntryViewBinding.inflate(LayoutInflater.from(context), this, true)
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
            attrs, R.styleable.IconTextEntryView, defStyle, 0
        )
        binding.tvTitle.text = a.getString(R.styleable.IconTextEntryView_text)
        binding.tvSubtitle.text = a.getString(R.styleable.IconTextEntryView_secondaryText)
        binding.ivIcon.setImageDrawable(a.getDrawable(R.styleable.IconTextEntryView_drawable))
        val hideDrawable = a.getBoolean(R.styleable.IconTextEntryView_hideDrawable, false)
        if (hideDrawable) {
            binding.ivIcon.visibility = View.GONE
            val padding = resources.getDimensionPixelOffset(R.dimen.compact_padding);
            binding.tvTitle.setPadding(padding,0, padding,0)
            binding.tvSubtitle.setPadding(padding, 0, padding, 0)
        } else {
            binding.ivIcon.visibility = View.VISIBLE
        }
        binding.smItemSwitch.visibility = if (a.getBoolean(R.styleable.IconTextEntryView_hideSwitch, true)) View.GONE else View.VISIBLE
        binding.smItemSwitch.isChecked = a.getBoolean(R.styleable.IconTextEntryView_checked, false)
        a.recycle()
    }

    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        binding.smItemSwitch.setOnCheckedChangeListener(listener)
    }

    fun setStateFlowForSecondaryText(flow: StateFlow<String>) {
        binding.tvSubtitle.text = flow.value
    }

    var isChecked: Boolean
        get() = binding.smItemSwitch.isChecked
        set(value) {
            binding.smItemSwitch.isChecked = value
        }

    var subtitle: CharSequence
        get() {
            return binding.tvSubtitle.text
        }
        set(value) {
            binding.tvSubtitle.text = value
        }
}