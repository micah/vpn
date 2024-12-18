package org.torproject.vpn.ui.bridgesettings.model

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.torproject.vpn.R
import org.torproject.vpn.TorApplication
import org.torproject.vpn.utils.PreferenceHelper
import org.torproject.vpn.utils.PreferenceHelper.Companion.BridgeType
import org.torproject.vpn.utils.resolveActivityForUri

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
            else -> -1
        }
    }

    fun selectBuiltInObfs4() {
        preferenceHelper.bridgeType = BridgeType.Obfs4
    }

    fun selectBuiltInSnowflake() {
        preferenceHelper.bridgeType = BridgeType.Snowflake
    }

    private fun getFormattedWebBotSubtitle(context: Context): SpannableString {
        val botString = "bridges.torproject.org";
        val subtitleString = context.getString(R.string.web_bot_subtitle, botString)
        val spannable = SpannableString(subtitleString)
        spannable.setSpan(ForegroundColorSpan(context.getColor(R.color.tertiary)), subtitleString.indexOf(botString), subtitleString.indexOf(botString) + botString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }

    private fun getFormattedEmailBotSubtext(context: Context): SpannableString {
        val botString = "bridges@torproject.org";
        val subtitleString = context.getString(R.string.email_bot_subtitle, botString)
        val spannable = SpannableString(subtitleString)
        spannable.setSpan(ForegroundColorSpan(context.getColor(R.color.tertiary)), subtitleString.indexOf(botString), subtitleString.indexOf(botString) + botString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }

    private fun getFormattedTelegramText(context: Context): SpannableString {
        val botString = "GetBridgesBot";
        val subtitleString = context.getString(R.string.telegram_bot_subtitle, botString)
        val spannable = SpannableString(subtitleString)
        spannable.setSpan(ForegroundColorSpan(context.getColor(R.color.tertiary)), subtitleString.indexOf(botString), subtitleString.indexOf(botString) + botString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
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
}