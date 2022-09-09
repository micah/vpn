package com.example.torwitharti.utils

import android.content.Context
import android.content.SharedPreferences

private const val TOR_VPN_SP: String = "tor-vpn"

class PreferenceHelper(context: Context) {

    companion object {
        const val PROTECTED_APPS: String = "protected_apps"
        const val PROTECT_ALL_APPS: String = "protect_all_apps"
        const val SHOULD_SHOW_GUIDE: String = "should_show_guide"
    }

    private val sharedPreference =
        context.applicationContext.getSharedPreferences(TOR_VPN_SP, Context.MODE_PRIVATE)

    /**
     * Whether guide in the connect screen need to be shown.
     *
     * TODO currently passing true as default to make the guide visible every time.
     * */
    var shouldShowGuide
        get() = sharedPreference.getBoolean(SHOULD_SHOW_GUIDE, true)
        set(value) = sharedPreference.edit().putBoolean(SHOULD_SHOW_GUIDE, value).apply()

    var protectAllApps
        get() = sharedPreference.getBoolean(PROTECT_ALL_APPS, true)
        set(value) = sharedPreference.edit().putBoolean(PROTECT_ALL_APPS, value).apply()

    var protectedApps
        get() = sharedPreference.getStringSet(PROTECTED_APPS, mutableSetOf<String>())
        set(value) = sharedPreference.edit().putStringSet(PROTECTED_APPS, value).apply()

    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreference.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreference.unregisterOnSharedPreferenceChangeListener(listener)
    }

}

