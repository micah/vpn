package com.example.torwitharti.ui.configure.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.torwitharti.R
import com.example.torwitharti.databinding.ConfigItemViewBinding

/**
 * Configuration entry used in ConfigureFragment
 */
class ConfigItemView : ConstraintLayout {

    private val binding by lazy {
        ConfigItemViewBinding.inflate(LayoutInflater.from(context), this, true)
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
            attrs, R.styleable.ConfigItemView, defStyle, 0
        )
        binding.tvTitle.text = a.getString(R.styleable.ConfigItemView_text)
        binding.tvSubtitle.text = a.getString(R.styleable.ConfigItemView_secondaryText)
        binding.ivIcon.setImageDrawable(a.getDrawable(R.styleable.ConfigItemView_drawable))
        a.recycle()
    }

}