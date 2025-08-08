package org.torproject.vpn.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.torproject.vpn.ui.approuting.data.AppListAdapter.Companion.CELL
import org.torproject.vpn.ui.approuting.model.AppItemModel
import org.torproject.vpn.ui.iconsetting.model.LauncherDefault
import java.lang.reflect.Type

private const val TOR_VPN_SP: String = "tor-vpn"

class PreferenceHelper(context: Context) {

    companion object {
        const val PROTECTED_APPS: String = "protected_apps"
        const val PROTECT_ALL_APPS: String = "protect_all_apps"
        const val SHOULD_SHOW_GUIDE: String = "should_show_guide"
        const val CACHED_APPS: String = "cached_apps"
        const val START_TIME: String = "start_time"
        const val START_ON_BOOT: String = "start_on_boot"
        const val WARNINGS_ENABLED: String = "warnings_enabled"
        const val USE_BRIDGE: String = "use_bridge"
        const val EXIT_NODE_COUNTRY: String = "exit_node_country"
        const val AUTOMATIC_EXIT_NODE_SELECTION: String = "automatic_exit_node_selection"
        const val BRIDGE_TYPE = "bridge_type"
        const val BRIDGE_LINES = "bridge_lines"
        const val LAUNCHER_CLASS = "launcher_class"
        const val EXIT_NODE_COUNTRIES = "exit_node_countries"

        enum class BridgeType {
            Obfs4,
            Snowflake,
            Manual,
            None
        }
    }

    private val sharedPreference =
        context.applicationContext.getSharedPreferences(TOR_VPN_SP, Context.MODE_PRIVATE)

    /**
     * Whether guide in the connect screen need to be shown.
     *
     * */
    var shouldShowGuide
        get() = sharedPreference.getBoolean(SHOULD_SHOW_GUIDE, true)
        set(value) = sharedPreference.edit().putBoolean(SHOULD_SHOW_GUIDE, value).apply()

    var launcherClass: String
        get() = sharedPreference.getString(LAUNCHER_CLASS, LauncherDefault::class.java.name)!!
        set(value) = sharedPreference.edit().putString(LAUNCHER_CLASS, value).apply()

    var protectAllApps
        get() = sharedPreference.getBoolean(PROTECT_ALL_APPS, true)
        set(value) = sharedPreference.edit().putBoolean(PROTECT_ALL_APPS, value).apply()

    var protectedApps: MutableSet<String>
        get() = sharedPreference.getStringSet(PROTECTED_APPS, mutableSetOf<String>()).orEmpty().toMutableSet()
        set(value) {
            if (isRunningOnMainThread()) {
                sharedPreference.edit().putStringSet(PROTECTED_APPS, value).apply()
            } else {
                sharedPreference.edit().putStringSet(PROTECTED_APPS, value).commit()
            }
        }

    // cached apps returns a json as string
    var cachedApps
        get() = sharedPreference.getString(CACHED_APPS, "[]")
        set(value) = sharedPreference.edit().putString(CACHED_APPS, value).apply()

    var startOnBoot
        get() = sharedPreference.getBoolean(START_ON_BOOT, false)
        set(value) = sharedPreference.edit().putBoolean(START_ON_BOOT, value).apply()

    var useBridge
        get() = sharedPreference.getBoolean(USE_BRIDGE, false)
        set(value) = sharedPreference.edit().putBoolean(USE_BRIDGE, value).apply()

    var exitNodeCountry
        get() = sharedPreference.getString(EXIT_NODE_COUNTRY, null)
        set(value) = sharedPreference.edit().putString(EXIT_NODE_COUNTRY, value).apply()

    var automaticExitNodeSelection
        get() = sharedPreference.getBoolean(AUTOMATIC_EXIT_NODE_SELECTION, true)
        set(value) = sharedPreference.edit().putBoolean(AUTOMATIC_EXIT_NODE_SELECTION, value).apply()

    var bridgeType: BridgeType
        get() = BridgeType.valueOf(sharedPreference.getString(BRIDGE_TYPE, BridgeType.None.name)!!)
        set(bridgeType) = sharedPreference.edit().putString(BRIDGE_TYPE, bridgeType.name).apply()


    var bridgeLines
        get() = sharedPreference.getStringSet(BRIDGE_LINES, mutableSetOf<String>()) as Set<String>
        set(value) = sharedPreference.edit().putStringSet(BRIDGE_LINES, value).apply()

    var relayCountries
        get() = sharedPreference.getStringSet(EXIT_NODE_COUNTRIES, mutableSetOf<String>()) as Set<String>
        set(value) = sharedPreference.edit().putStringSet(EXIT_NODE_COUNTRIES, value).apply()

    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreference.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreference.unregisterOnSharedPreferenceChangeListener(listener)
    }

    @VisibleForTesting
    fun clear() {
        sharedPreference.edit().clear().commit()
    }

}
