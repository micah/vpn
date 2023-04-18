package com.example.torwitharti.vpn

import android.app.Notification
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.*
import android.system.OsConstants
import android.util.Log
import androidx.lifecycle.Observer
import com.example.torwitharti.utils.PreferenceHelper
import com.example.torwitharti.utils.VpnNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import org.torproject.onionmasq.ISocketProtect
import org.torproject.onionmasq.OnionMasq
import org.torproject.onionmasq.OnionmasqEvent
import org.torproject.onionmasq.logging.LogHelper
import org.torproject.onionmasq.logging.LogObservable
import java.io.IOException


class TorVpnService : VpnService() {

    companion object {
        private val TAG: String = TorVpnService::class.java.getSimpleName()
        private const val ALWAYS_ON_MIN_API_LEVEL = Build.VERSION_CODES.N
        val ACTION_START_VPN = "$TAG.start"
        val ACTION_STOP_VPN = "$TAG.stop"
    }

    private var fd: ParcelFileDescriptor? = null
    private lateinit var notificationManager: VpnNotificationManager
    private lateinit var logHelper: LogHelper
    private var logObservable: LogObservable? = null

    private val binder: IBinder = TorVpnServiceBinder()

    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)
    private var observer: Observer<OnionmasqEvent>? = null
    private val mainHandler: Handler by lazy {
        Handler(mainLooper)
    }

    inner class TorVpnServiceBinder : Binder(), ISocketProtect {
        override fun protect(socket: Int): Boolean {
            return this@TorVpnService.protect(socket)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = VpnNotificationManager(this)
        logHelper = LogHelper()
        // TODO: observe LogObservable, set VpnStatusObservable to Connected if progress events completed
        // TODO 2: move VpnStatusObservable to onionmasq lib, handling the onionmasq states Connected, Disconnected and Failed should be encapsulated,
        //  Connecting and Disconnecting can be triggered by the UI to receive immediate state changes on user interaction
        //  LogObservable.getInstance().logListData.observe()
        OnionMasq.bindVPNService(TorVpnService::class.java)
        observer = Observer<OnionmasqEvent> { onionmasqEvent: OnionmasqEvent ->
            if (onionmasqEvent.isReadyForTraffic) {
                VpnStatusObservable.update(ConnectionState.CONNECTED)
            }
        }
        logObservable = LogObservable.getInstance()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "service: onStartCommand")

        val notification: Notification? = notificationManager.buildForegroundServiceNotification()
        startForeground(VpnNotificationManager.NOTIFICATION_ID, notification)
        val action = if (intent != null) intent.action else ""
        if (action == ACTION_START_VPN ||
            action == "android.net.VpnService" && Build.VERSION.SDK_INT >= ALWAYS_ON_MIN_API_LEVEL
        //only always-on feature triggers this
        ) {
            Log.d(TAG, "service: starting vpn...")
            establishVpn()
        } else if (action == ACTION_STOP_VPN) {
            Log.d(TAG, "service: stopping vpn...")
            stop()
        } else {
            Log.d(TAG, "service unknown action: $action" );
        }
        return START_STICKY
    }

    override fun onRevoke() {
        super.onRevoke()
        Log.d(TAG, "service: onRevoke")
        stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        Log.d(TAG, "service: onDestroy")
        notificationManager.cancelNotifications()
        if (VpnStatusObservable.statusLiveData.value !== ConnectionState.CONNECTION_ERROR) {
            VpnStatusObservable.update(ConnectionState.DISCONNECTED)
        }
        logObservable = null
    }

    private fun stop() {
        Log.d(TAG, "service: stopping")
        VpnStatusObservable.update(ConnectionState.DISCONNECTING)
        OnionMasq.stop()
        observer?.let {
            mainHandler.post { OnionMasq.getProgressEvent().removeObserver(it) }
        }

        logHelper.stopLog()
        closeFd()
        stopForeground(true)
        OnionMasq.unbindVPNService()
        stopSelf()
    }

    private fun closeFd() {
        try {
            if (fd != null) fd!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun prepareVpnProfile(): Builder {
        val builder = Builder()
        applyAppFilter(builder)
        builder.setSession("TorVPN session")
        builder.addRoute("0.0.0.0", 0)
        builder.addRoute("::", 0)
        builder.addAddress("10.42.0.8", 16)
        builder.addAddress("fc00::", 7)
        builder.allowFamily(OsConstants.AF_INET)
        builder.allowFamily(OsConstants.AF_INET6)
        builder.setMtu(1500)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false)
        }
        return builder
    }

    private fun establishVpn() {
        try {
            val builder = prepareVpnProfile()
            fd = builder.establish();
            logHelper.readLog()

            observer?.let {
                Log.d(TAG, "observer created!")
                mainHandler.post { OnionMasq.getProgressEvent().observeForever(it) }
            }

            coroutineScope.async {
                OnionMasq.start(fd!!.detachFd())
            }
        } catch (e: Exception) {
            // Catch any exception
            OnionMasq.stop()
            e.printStackTrace()
            VpnStatusObservable.update(ConnectionState.CONNECTION_ERROR)
            stopSelf()
        }
    }

    /**
     * Adds selected app into 'allowed apps' for current vpn connection. Only selected apps will use VPN.
     * @param builder VPN Builder
     */
    private fun applyAppFilter(builder: Builder) {
        val helper = PreferenceHelper(applicationContext)
        val selectedApps: Set<String> = helper.protectedApps
            ?: //No selection done, so we allow all apps.
            return
        val packageManager = packageManager
        for (appPackage in selectedApps) {
            try {
                packageManager.getPackageInfo(appPackage, 0)
                builder.addAllowedApplication(appPackage)
            } catch (e: PackageManager.NameNotFoundException) {
                // The app is selected but isn't installed anymore.
            }
        }
    }
}