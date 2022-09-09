package com.example.torwitharti.ui.approuting

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.torwitharti.databinding.AppSwitchItemViewBinding
import com.example.torwitharti.utils.lifecycleOwner

class AppSwitchItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding : AppSwitchItemViewBinding
    init {
        binding = AppSwitchItemViewBinding.inflate(LayoutInflater.from(context), this, true)
        binding.lifecycleOwner = this.lifecycleOwner
    }
}