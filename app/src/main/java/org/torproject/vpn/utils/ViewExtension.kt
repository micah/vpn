package org.torproject.vpn.utils

import android.content.ContextWrapper
import android.view.View
import androidx.lifecycle.LifecycleOwner


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
