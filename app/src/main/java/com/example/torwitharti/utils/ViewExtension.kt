package com.example.torwitharti.utils

import android.content.ContextWrapper
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewTreeLifecycleOwner


fun View.getLifeCycleOwner(): LifecycleOwner? {
    var context = this.context

    while (context is ContextWrapper) {
        if (context is LifecycleOwner) {
            return context
        }
        context = context.baseContext
    }
    return null
}

val View.lifecycleOwner get() = ViewTreeLifecycleOwner.get(this)