package org.torproject.vpn.ui.bridgesettings.model

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.net.Uri
import android.text.SpannableString
import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.torproject.vpn.R
import org.torproject.vpn.TorApplication
import org.torproject.vpn.utils.PreferenceHelper
import org.torproject.vpn.utils.PreferenceHelper.Companion.BridgeType
import org.torproject.vpn.utils.resolveActivityForUri
import org.torproject.vpn.vpn.VpnServiceCommand
import org.torproject.vpn.vpn.VpnStatusObservable

class BridgeSettingsFragmentViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val TELEGRAM_BOT_URI = "tg://resolve?domain=GetBridgesBot"
        const val EMAIL_BOT_URI = "mailto:bridges@torproject.org"
        const val WEB_BOT_URI = "https://bridges.torproject.org"
    }

    private val TAG: String = BridgeSettingsFragmentViewModel::class.java.simpleName
    val preferenceHelper = PreferenceHelper(application)
    private val telegramActivityInfo: MutableLiveData<ActivityInfo?> = MutableLiveData()
    private val emailActivityInfo: MutableLiveData<ActivityInfo?> = MutableLiveData()
    private val webActivityInfo: MutableLiveData<ActivityInfo?> = MutableLiveData()
    val telegramVisibility = telegramActivityInfo.asFlow().map { info ->
        if (info != null) {
            return@map View.VISIBLE
        } else {
            return@map View.GONE
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = View.VISIBLE
    )
    fun onUseBridgeChanged(compoundButton: CompoundButton, isChecked: Boolean) {
        preferenceHelper.useBridge = isChecked
        updateVPNSettings()
    }

    val useBridge = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (PreferenceHelper.USE_BRIDGE == changedKey) {
                trySend(preferenceHelper.useBridge)
            }
        }
        preferenceHelper.registerListener(listener)
        awaitClose { preferenceHelper.unregisterListener(listener) }
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        preferenceHelper.useBridge
    )

    val telegramDrawable = telegramActivityInfo.asFlow().map { info ->
        return@map info?.applicationInfo?.loadIcon(application.packageManager)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppCompatResources.getDrawable(application, R.drawable.ic_dummy_app)
    )
    val webbrowserDrawable = webActivityInfo.asFlow().map { info ->
        return@map info?.applicationInfo?.loadIcon(application.packageManager)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppCompatResources.getDrawable(application, R.drawable.ic_dummy_app)
    )

    val emailDrawable = emailActivityInfo.asFlow().map { info ->
        return@map info?.applicationInfo?.loadIcon(application.packageManager)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppCompatResources.getDrawable(application, R.drawable.ic_dummy_app)
    )

    val formattedTelegramBotSubtitle: SpannableString
        get() = getFormattedTelegramText(getApplication())

    val formattedEmailBotSubtitle: SpannableString
        get() = getFormattedEmailBotSubtext(getApplication())

    val formattedWebBotSubtitle: SpannableString
        get() = getFormattedWebBotSubtitle(getApplication())

    val snowflakeAccessibilityDescription: String
        get() = "${getApplication<TorApplication>().getString(R.string.snowflake)}; ${getApplication<TorApplication>().getString(R.string.snowflake_description)}"

    val obfs4AccessibilityDescription: String
        get() = "${getApplication<TorApplication>().getString(R.string.obfs4)}; ${getApplication<TorApplication>().getString(R.string.obfs4_description)}"

    private val bridgeLines = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (PreferenceHelper.BRIDGE_LINES == changedKey) {
                trySend(preferenceHelper.bridgeLines.toList())
            }
        }
        preferenceHelper.registerListener(listener)
        awaitClose { preferenceHelper.unregisterListener(listener) }
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        preferenceHelper.bridgeLines.toList()
    )

    val manualAccessibilityDescription: StateFlow<String> = bridgeLines.map { bridgeLines ->
        if (bridgeLines.isEmpty()) {
            return@map application.resources.getString(R.string.action_paste_bridges)
        }
        return@map application.resources.getQuantityString(R.plurals.n_bridges, bridgeLines.size, bridgeLines.size)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = ""
    )

    val bridgeType = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (PreferenceHelper.BRIDGE_TYPE == changedKey) {
                trySend(preferenceHelper.bridgeType)
            }
        }
        preferenceHelper.registerListener(listener)
        awaitClose { preferenceHelper.unregisterListener(listener) }
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        preferenceHelper.bridgeType
    )

    val bridgeWeightSum = bridgeType.map {
        return@map if (it == BridgeType.Manual  || bridgeLines.value.isNotEmpty()) {
            3f
        } else {
            2f
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = 2f
    )

    val manualSelectionVisibility = bridgeType.map {
        return@map if (it == BridgeType.Manual || bridgeLines.value.isNotEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = View.GONE
    )

    val addManualSelectionVisibility = bridgeType.map {
        return@map if (it == BridgeType.Manual || bridgeLines.value.isNotEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = View.VISIBLE
    )

    fun load() {
        viewModelScope.launch(Dispatchers.Default) {
            telegramActivityInfo.postValue(queryTelegramActivityInfo(getApplication()))
            emailActivityInfo.postValue(queryEmailActivityInfo(getApplication()))
            webActivityInfo.postValue(queryWebActivityInfo(getApplication()))
        }
    }
    fun getSelectedBridgeTypeId(): Int {
        return when(preferenceHelper.bridgeType) {
            BridgeType.Obfs4 -> R.id.rb_obfs4
            BridgeType.Snowflake -> R.id.rb_snowflake
            BridgeType.Manual -> R.id.rb_manual
            else -> R.id.rb_none
        }
    }

    fun selectBuiltInObfs4() {
        preferenceHelper.bridgeType = BridgeType.Obfs4
        updateVPNSettings()
    }

    fun selectBuiltInSnowflake() {
        preferenceHelper.bridgeType = BridgeType.Snowflake
        updateVPNSettings()
    }

    private fun getFormattedWebBotSubtitle(context: Context): SpannableString {
        return SpannableString("bridges.torproject.org")
    }

    private fun getFormattedEmailBotSubtext(context: Context): SpannableString {
        return SpannableString("bridges@torproject.org")
    }

    private fun getFormattedTelegramText(context: Context): SpannableString {
        val botString = "@GetBridgesBot";
        val subtitleString = context.getString(R.string.telegram_bot_subtitle, botString)
        val spannable = SpannableString(subtitleString)
        return spannable
    }

    private fun queryTelegramActivityInfo(context: Context): ActivityInfo? {
        return resolveActivityForUri(context.packageManager, TELEGRAM_BOT_URI)
    }

    private fun queryEmailActivityInfo(context: Context): ActivityInfo? {
        return resolveActivityForUri(context.packageManager, EMAIL_BOT_URI)
    }

    private fun queryWebActivityInfo(context: Context): ActivityInfo? {
        return resolveActivityForUri(context.packageManager, WEB_BOT_URI)
    }

    fun getTelegramIntent(): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_BOT_URI))
    }

    fun getEmailBotIntent(): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(EMAIL_BOT_URI))
    }

    fun getWebBotIntent(): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(WEB_BOT_URI))
    }

    private fun updateVPNSettings() {
        if (VpnStatusObservable.isVPNActive()) {
            VpnServiceCommand.startVpn(getApplication())
        }
    }

}