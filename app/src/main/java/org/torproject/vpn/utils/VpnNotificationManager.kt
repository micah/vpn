package org.torproject.vpn.utils

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.format.Formatter
import androidx.core.app.NotificationCompat
import org.torproject.vpn.MainActivity
import org.torproject.vpn.R
import org.torproject.vpn.vpn.ConnectionState
import org.torproject.vpn.vpn.DataUsage
import org.torproject.vpn.vpn.TorVpnService
import org.torproject.vpn.vpn.TorVpnService.Companion.ACTION_START_VPN
import org.torproject.vpn.vpn.TorVpnService.Companion.ACTION_STOP_VPN

class VpnNotificationManager(val context: Context) {

    companion object {
        val NOTIFICATION_ID = 1533082945
        private val NOTIFICATION_CHANNEL_NEWSTATUS_ID = "TORVPN_NOTIFICATION_CHANNEL_NEWSTATUS_ID"
    }

    private val notificationManager: NotificationManager
    private var startTime: Long = 0
    init {
        notificationManager = initNotificationManager()
    }

    fun buildForegroundServiceNotification(): Notification {
        val notificationBuilder = initNotificationBuilderDefaults()
        notificationBuilder
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setWhen(System.currentTimeMillis())
            .setContentTitle(context.getString(R.string.app_name))
            .setContentIntent(getContentPendingIntent())
            .setPriority(NotificationCompat.PRIORITY_LOW)
        return notificationBuilder.build()
    }

    fun updateNotification(state: ConnectionState, dataUsage: DataUsage, hasConnectivity: Boolean) {
        if (!notificationManager.areNotificationsEnabled()) {
            return
        }
        var action: NotificationCompat.Action? = null
        var stateString: String? = null
        var dataUsageString: String? = null
        var showChronometer = false
        when(state) {
            ConnectionState.CONNECTING -> {
                action = NotificationCompat.Action.Builder(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    context.getString(R.string.action_cancel), getStopIntent()
                ).build()
                stateString = context.getString(if (hasConnectivity) R.string.state_connecting else R.string.no_internet)
                startTime = 0
            }
            ConnectionState.CONNECTED -> {
                action = NotificationCompat.Action.Builder(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    context.getString(R.string.action_disconnect), getStopIntent()
                ).build()
                stateString = context.getString(if (hasConnectivity) R.string.state_connected else R.string.no_internet)
                dataUsageString = getDataUsageText(dataUsage)
                if (startTime == 0L) {
                    startTime = System.currentTimeMillis()
                }
                showChronometer = true
            }
            ConnectionState.CONNECTION_ERROR -> {
                action = NotificationCompat.Action.Builder(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    context.getString(R.string.action_try_again), getStartIntent()
                ).build()
                stateString = context.getString(R.string.error_detail_message)
                startTime = 0
            }

            else -> {
                startTime = 0
            }
        }

        val notificationBuilder = initNotificationBuilderDefaults()
        notificationBuilder
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setWhen(startTime)
            .setUsesChronometer(showChronometer)
            .setShowWhen(showChronometer)
            .setContentTitle(stateString)
            .setContentText(dataUsageString)
            .setTicker(stateString)
            .setContentIntent(getContentPendingIntent())
            .addAction(action)
            .setSound(null)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
        notificationManager.notify(NOTIFICATION_ID,  notificationBuilder.build())
    }

    private fun getDataUsageText(dataUsage: DataUsage): String {
        val received = formatBits(dataUsage.downstreamDataPerSec)
        val sent = formatBits(dataUsage.upstreamDataPerSec)
	// Totals should be shown as bytes, not bits
        val receivedOverall = formatBits(dataUsage.downstreamData * 8 )
        val sentOverall = formatBits(dataUsage.upstreamData * 8 )
        return context.getString(R.string.stats_combined, received, receivedOverall, sent, sentOverall);
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
        notificationBuilder.setAutoCancel(true)
            .setOngoing(true)
            .setLocalOnly(true)
        return notificationBuilder
    }


    fun cancelNotifications() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
