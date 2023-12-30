package org.torproject.vpn.ui.configure

import android.view.View

interface ClickHandler {
    fun onHelpClicked(v: View)
    fun onAppsClicked(v: View)
    fun onConnectionClicked(v: View)
}