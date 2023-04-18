package com.example.torwitharti.utils

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.torwitharti.MainActivity
import com.example.torwitharti.R
import com.example.torwitharti.vpn.ConnectionState
import com.example.torwitharti.vpn.TorVpnService
import com.example.torwitharti.vpn.TorVpnService.Companion.ACTION_START_VPN
import com.example.torwitharti.vpn.TorVpnService.Companion.ACTION_STOP_VPN

class VpnNotificationManager(val context: Context) {

    companion object {
        val NOTIFICATION_ID = 1533082945
        private val NOTIFICATION_CHANNEL_NEWSTATUS_ID = "TORVPN_NOTIFICATION_CHANNEL_NEWSTATUS_ID"
    }

    private val notificationManager: NotificationManager
    init {
        notificationManager = initNotificationManager()
    }

    fun buildForegroundServiceNotification(): Notification? {
        val notificationBuilder = initNotificationBuilderDefaults()
        notificationBuilder
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setWhen(System.currentTimeMillis())
            .setContentTitle(context.getString(R.string.app_name))
            .setContentIntent(getContentPendingIntent())
        return notificationBuilder.build()
    }

    fun updateNotification(state: ConnectionState) {
        var action: NotificationCompat.Action? = null
        var stateString: String? = null
        when(state) {
            ConnectionState.CONNECTING -> {
                action = NotificationCompat.Action.Builder(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    context.getString(R.string.frag_connect_cancel), getStopIntent()
                ).build()
                stateString = context.getString(R.string.frag_connect_connecting)
            }
            ConnectionState.PAUSED -> {
                action = NotificationCompat.Action.Builder(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    context.getString(R.string.frag_connect_reconnect), getStartIntent()
                ).build()
                stateString = context.getString(R.string.frag_connect_paused)
            }
            ConnectionState.CONNECTED -> {
                action = NotificationCompat.Action.Builder(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    context.getString(R.string.frag_connect_disconnect), getStopIntent()
                ).build()
                stateString = context.getString(R.string.frag_connect_connected)
            }
            ConnectionState.CONNECTION_ERROR -> {
                action = NotificationCompat.Action.Builder(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    context.getString(R.string.frag_connect_try_again), getStartIntent()
                ).build()
                stateString = context.getString(R.string.error_detail_message)
            }

            else -> {}
        }

        val notificationBuilder = initNotificationBuilderDefaults()
        notificationBuilder
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setWhen(System.currentTimeMillis())
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(stateString)
            .setTicker(stateString)
            .setContentIntent(getContentPendingIntent())
            .addAction(action)
        notificationManager.notify(NOTIFICATION_ID,  notificationBuilder.build())
    }
    private fun getContentPendingIntent(): PendingIntent? {
        val mainActivityIntent = Intent(context, MainActivity::class.java)
        mainActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        return PendingIntent.getActivity(context, 0, mainActivityIntent, getDefaultFlags())
    }

    private fun getStopIntent(): PendingIntent? {
        val stopVpnIntent = Intent(context, TorVpnService::class.java)
        stopVpnIntent.action = ACTION_STOP_VPN
        return PendingIntent.getService(context, 0, stopVpnIntent, getDefaultFlags())
    }

    private fun getStartIntent(): PendingIntent? {
        val startVpnIntent = Intent(context, TorVpnService::class.java)
        startVpnIntent.action = ACTION_START_VPN
        return PendingIntent.getService(context, 0, startVpnIntent, getDefaultFlags())
    }

    private fun getDefaultFlags(): Int {
        var flag = PendingIntent.FLAG_CANCEL_CURRENT
        if (Build.VERSION.SDK_INT >= 23) {
            flag = flag or PendingIntent.FLAG_IMMUTABLE
        }
        return flag
    }

    private fun initNotificationManager(): NotificationManager {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        return notificationManager
    }

    @TargetApi(26)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val name: CharSequence = context.getString(R.string.notification_channel_name)
        val description = context.getString(R.string.notification_channel_description)
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_NEWSTATUS_ID,
            name,
            NotificationManager.IMPORTANCE_LOW
        )
        channel.setSound(null, null)
        channel.description = description
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        notificationManager.createNotificationChannel(channel)
    }

    private fun initNotificationBuilderDefaults(): NotificationCompat.Builder {
        val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_NEWSTATUS_ID)
        notificationBuilder.setDefaults(Notification.DEFAULT_ALL).setAutoCancel(true)
        return notificationBuilder
    }


    fun cancelNotifications() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}