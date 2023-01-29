package com.example.torwitharti

import com.example.torwitharti.vpn.ConnectionState

interface StatusBarProgressInterface {
    fun setStatus(vpnStatus: ConnectionState)
}