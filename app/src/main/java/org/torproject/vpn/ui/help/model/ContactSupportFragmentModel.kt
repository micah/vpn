package org.torproject.vpn.ui.help.model

import android.app.Application
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.torproject.vpn.utils.resolveActivityForUri

class ContactSupportFragmentModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val TELEGRAM_URI = "tg://resolve?domain=TorProjectSupportBot"
        const val SIGNAL_URI = "sgnl://signal.me/#p/+17787431312"
        const val WHATSAPP_URI = "whatsapp://send?phone=+447421000612"
        const val EMAIL_URI = "mailto:frontdesk@torproject.org"
        const val TELEGRAM_HTTP_URI = "https://tg.me/TorSupportBot"
        const val SIGNAL_HTTP_URI = "https://signal.me/#p/+17787431312"
        const val WHATSAPP_HTTP_URI = "https://wa.me/447421000612"
    }

    val telegramActivityInfo: MutableLiveData<ActivityInfo?> = MutableLiveData()
    val whatsAppActivityInfo: MutableLiveData<ActivityInfo?> = MutableLiveData()
    val signalActivityInfo: MutableLiveData<ActivityInfo?> = MutableLiveData()
    val emailActivityInfo: MutableLiveData<ActivityInfo?> = MutableLiveData()

    private val packageManager = application.packageManager
    val context = application

    var emailIntent = Intent(Intent.ACTION_VIEW, Uri.parse(EMAIL_URI))
    var telegramIntent = Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_HTTP_URI))
    var signalIntent = Intent(Intent.ACTION_VIEW, Uri.parse(SIGNAL_HTTP_URI))
    var whatsappIntent = Intent(Intent.ACTION_VIEW, Uri.parse(WHATSAPP_HTTP_URI))

    fun load() {
        viewModelScope.launch(Dispatchers.Default) {
            telegramActivityInfo.postValue(resolveActivityForUri(packageManager, TELEGRAM_URI))
            emailActivityInfo.postValue(resolveActivityForUri(packageManager, EMAIL_URI))
            signalActivityInfo.postValue(resolveActivityForUri(packageManager, SIGNAL_URI))
            whatsAppActivityInfo.postValue(resolveActivityForUri(packageManager, WHATSAPP_URI))
        }
    }

    fun getText(info: ActivityInfo): String {
        return info.applicationInfo.loadLabel(packageManager).toString()
    }

    fun getDrawable(activityInfo: ActivityInfo, default: Int): Drawable? {
        return activityInfo.applicationInfo?.loadIcon(packageManager) ?: AppCompatResources.getDrawable(context, default)
    }

    fun setCustomTelegramURLScheme() {
        telegramIntent = Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_URI))
    }
    fun setCustomSignalURLScheme() {
        signalIntent = Intent(Intent.ACTION_VIEW, Uri.parse(SIGNAL_URI))
    }

    fun setCustomWhatsappURLScheme() {
        whatsappIntent = Intent(Intent.ACTION_VIEW, Uri.parse(WHATSAPP_URI))
    }
}