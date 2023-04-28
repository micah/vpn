package org.torproject.vpn.vpn

class DataUsage {
    var upstreamData: Long = 0
    var downstreamData: Long = 0
    var upstreamDataPerSec: Long = 0
    var downstreamDataPerSec: Long = 0
    var timeStamp: Long = System.currentTimeMillis()

}