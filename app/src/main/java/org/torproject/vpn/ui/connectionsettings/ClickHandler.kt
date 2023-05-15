package org.torproject.vpn.ui.connectionsettings

import android.view.View

interface ClickHandler {
    fun onTorLogsClicked(v: View)
    fun onAlwaysOnClicked(v: View)
}