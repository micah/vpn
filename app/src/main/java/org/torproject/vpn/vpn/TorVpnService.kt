package org.torproject.vpn.vpn

import android.app.Notification
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.*
import android.system.OsConstants
import android.util.Log
import androidx.lifecycle.Observer
import org.torproject.vpn.utils.PreferenceHelper
import org.torproject.vpn.utils.VpnNotificationManager
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
import java.util.*


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
    private var vpnStatusObserver: Observer<ConnectionState>? = null
    private var vpnDataUsageObserver: Observer<DataUsage>? = null
    private val mainHandler: Handler by lazy {
        Handler(mainLooper)
    }
    private val timer: Timer by lazy {
        Timer()
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
        OnionMasq.bindVPNService(TorVpnService::class.java)
        observer = Observer<OnionmasqEvent> { onionmasqEvent: OnionmasqEvent ->
            if (onionmasqEvent.isReadyForTraffic) {
                VpnStatusObservable.update(ConnectionState.CONNECTED)
            }
        }
        logObservable = LogObservable.getInstance()
        vpnStatusObserver = Observer<ConnectionState> { connectionState: ConnectionState? ->
            notificationManager.updateNotification(connectionState!!, VpnStatusObservable.dataUsage.value!!);
        }
        vpnDataUsageObserver = Observer<DataUsage> { dataUsage: DataUsage? ->
            notificationManager.updateNotification(VpnStatusObservable.statusLiveData.value!!, dataUsage!!)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "service: onStartCommand")

        val notification: Notification? = notificationManager.buildForegroundServiceNotification()
        startForeground(VpnNotificationManager.NOTIFICATION_ID, notification)
        val action = if (intent != null) intent.action else ""
        val isAlwaysOn =  (intent == null || intent.component == null || !intent.component!!.packageName.equals(getPackageName())) && Build.VERSION.SDK_INT >= ALWAYS_ON_MIN_API_LEVEL
        if (isAlwaysOn) {
            VpnStatusObservable.isAlwaysOnBooting.set(true)
            establishVpn()
        } else if (action == ACTION_START_VPN) {
            VpnStatusObservable.isAlwaysOnBooting.set(false)
            establishVpn()
        } else if (action == ACTION_STOP_VPN) {
            Log.d(TAG, "service: stopping vpn...")
            stop(false)
        } else {
            Log.d(TAG, "service unknown action: $action" );
        }
        return START_STICKY
    }

    override fun onRevoke() {
        super.onRevoke()
        Log.d(TAG, "service: onRevoke")
        stop(false)
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

    private fun stop(onError: Boolean) {
        Log.d(TAG, "service: stopping")
        if (onError) {
            VpnStatusObservable.update(ConnectionState.CONNECTION_ERROR)
        } else {
            VpnStatusObservable.update(ConnectionState.DISCONNECTING)
        }
        OnionMasq.stop()
        removeObservers()
        logHelper.stopLog()
        timer.cancel()
        VpnStatusObservable.resetDataUsage()
        closeFd()
        stopForeground(true)
        OnionMasq.unbindVPNService()
        stopSelf()
    }

    private fun removeObservers() {
        observer?.let {
            mainHandler.post { OnionMasq.getProgressEvent().removeObserver(it) }
        }
        vpnStatusObserver?.let {
            mainHandler.post{ VpnStatusObservable.statusLiveData.removeObserver(it) }
        }
        vpnDataUsageObserver?.let {
            mainHandler.post { VpnStatusObservable.dataUsage.removeObserver(it) }
        }
    }

    private fun closeFd() {
        try {
            fd?.close()
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
            Log.d(TAG, "service: starting vpn...")
            val builder = prepareVpnProfile()
            fd = builder.establish();
            logHelper.readLog()

            observer?.let {
                mainHandler.post { OnionMasq.getProgressEvent().observeForever(it) }
            }

            vpnStatusObserver?.let {
                mainHandler.post { VpnStatusObservable.statusLiveData.observeForever(it) }
            }
            vpnDataUsageObserver?.let {
                mainHandler.post{ VpnStatusObservable.dataUsage.observeForever(it) }
            }

            coroutineScope.async {
                OnionMasq.start(fd!!.detachFd())
            }

            timer.schedule(object : TimerTask() {
                override fun run() {
                    VpnStatusObservable.updateDataUsage(
                        OnionMasq.getBytesReceived(),
                        OnionMasq.getBytesSent()
                    )
                }
            }, 0, 1000)
        } catch (e: Exception) {
            // Catch any exception
            e.printStackTrace()
            stop(true)
        }
    }

    /**
     * Adds selected app into 'allowed apps' for current vpn connection. Only selected apps will use VPN.
     * @param builder VPN Builder
     */
    private fun applyAppFilter(builder: Builder) {
        val helper = PreferenceHelper(applicationContext)
        if (helper.protectAllApps) {
            // no filtering, all apps will be routed over the VPN
            return
        }
        val selectedApps: Set<String> = helper.protectedApps ?:
            HashSet<String>()

        val packageManager = packageManager
        if (selectedApps.isNotEmpty()) {
            // filtering, allow a subset of installed apps
            for (appPackage in selectedApps) {
                try {
                    packageManager.getPackageInfo(appPackage, 0)
                    builder.addAllowedApplication(appPackage)
                } catch (e: PackageManager.NameNotFoundException) {
                    // The app is selected but isn't installed anymore.
                }
            }
        } else {
            // no app selected and protect all apps disabled: disallow all installed apps
            val installedPackages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            for (appPackage in installedPackages) {
                try {
                    builder.addDisallowedApplication(appPackage.packageName)
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }
}