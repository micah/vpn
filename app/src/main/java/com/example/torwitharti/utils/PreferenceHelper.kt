package com.example.torwitharti.utils

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import java.util.prefs.Preferences

private const val TOR_VPN_SP: String = "tor-vpn"

class PreferenceHelper(private val application: Application) {
    private val sharedPreference =
        application.getSharedPreferences(TOR_VPN_SP, Context.MODE_PRIVATE)

    /**
     * Whether guide in the connect screen need to be shown.
     *
     * TODO currently passing true as default to make the guide visible every time.
     * */
    var shouldShowGuide
        get() = sharedPreference.getBoolean("should_show_guide", true)
        set(value) = sharedPreference.edit().putBoolean("should_show_guide", value).apply()

}

