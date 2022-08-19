package com.example.torwitharti.utils

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import java.util.prefs.Preferences

private const val TOR_VPN_SP: String = "tor-vpn"

class PreferenceHelper(context: Context) {
    private val sharedPreference =
        context.applicationContext.getSharedPreferences(TOR_VPN_SP, Context.MODE_PRIVATE)

    /**
     * Whether guide in the connect screen need to be shown.
     *
     * TODO currently passing true as default to make the guide visible every time.
     * */
    var shouldShowGuide
        get() = sharedPreference.getBoolean("should_show_guide", true)
        set(value) = sharedPreference.edit().putBoolean("should_show_guide", value).apply()

    var protectAllApps
        get() = sharedPreference.getBoolean("protect_all_apps", true)
        set(value) = sharedPreference.edit().putBoolean("protect_all_apps", value).apply()

    var protectedApps
        get() = sharedPreference.getStringSet("protected_apps", mutableSetOf<String>())
        set(value) = sharedPreference.edit().putStringSet("protected_apps", value).apply()
}

