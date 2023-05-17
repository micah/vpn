package org.torproject.vpn.ui.configure

import android.view.View

interface ClickHandler {
    fun onAppsClicked(v: View)
    fun onConnectionClicked(v: View)
}