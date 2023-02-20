package com.example.torwitharti

import com.example.torwitharti.utils.DummyConnectionState2

interface StatusBarProgressInterface {
    fun setStatus(vpnStatus: DummyConnectionState2)
}