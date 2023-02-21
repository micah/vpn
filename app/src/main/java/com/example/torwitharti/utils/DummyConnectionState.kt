package com.example.torwitharti.utils


//TODO we have to finalize one of this at some point, preferably before pre-alpha release
/**
 * Stats according to old design
 */
enum class DummyConnectionState {
    IDLE, CONNECTING, CONNECTED, DISCONNECTED, CONNECTION_ERROR
}

/**
 * Stats according discussed with @Cyberta
 */
enum class DummyConnectionState1 {
    DISCONNECTED, CONNECTING, CONNECTED, DISCONNECTING, CONNECTION_ERROR
}


/**
 * Stats according to according to UX team
 */
enum class DummyConnectionState2 {
    INIT /*shows 'connect'*/, CONNECTING/*shows 'pause'*/, PAUSED, CONNECTED, DISCONNECTED, CONNECTION_ERROR
}
