package com.example.torwitharti.ui.settings

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.torwitharti.databinding.AppItemViewBinding
import com.example.torwitharti.utils.lifecycleOwner

class AppItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding : AppItemViewBinding
    init {
        binding = AppItemViewBinding.inflate(LayoutInflater.from(context), this, true)
        binding.lifecycleOwner = this.lifecycleOwner
    }

}