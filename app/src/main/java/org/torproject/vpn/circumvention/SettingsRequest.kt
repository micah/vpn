package org.torproject.vpn.circumvention


data class SettingsRequest(val country: String? = null, val transports: List<String>? = null)