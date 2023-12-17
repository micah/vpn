package org.torproject.vpn.vpn

import android.app.Notification
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo.*
import android.net.VpnService
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.system.OsConstants
import android.util.Log
import androidx.core.app.ServiceCompat
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import org.torproject.onionmasq.OnionMasq
import org.torproject.onionmasq.events.*
import org.torproject.onionmasq.logging.LogHelper
import org.torproject.onionmasq.logging.LogObservable
import org.torproject.vpn.R
import org.torproject.vpn.utils.PreferenceHelper
import org.torproject.vpn.utils.PreferenceHelper.Companion.BridgeType
import org.torproject.vpn.utils.VpnNotificationManager
import org.torproject.vpn.utils.readAsset
import java.io.IOException
import java.lang.ref.WeakReference
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
    private lateinit var preferenceHelper: PreferenceHelper
    private var logObservable: LogObservable? = null


    private val binder: IBinder = TorVpnServiceBinder(WeakReference(this))

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

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = VpnNotificationManager(this)
        logHelper = LogHelper()
        preferenceHelper = PreferenceHelper(this)
        logObservable = LogObservable.getInstance()
        OnionMasq.bindVPNService(TorVpnService::class.java)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "service: onStartCommand")

        val notification: Notification = notificationManager.buildForegroundServiceNotification()
        ServiceCompat.startForeground(this, VpnNotificationManager.NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED)
        val action = if (intent != null) intent.action else ""
        val isAlwaysOn =  (intent == null || intent.component == null || intent.component!!.packageName != packageName) && Build.VERSION.SDK_INT >= ALWAYS_ON_MIN_API_LEVEL
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
        VpnStatusObservable.reset()
        closeFd()
        OnionMasq.unbindVPNService()
        stopForeground(true)
        stopSelf()
    }

    private fun createObservers() {
        observer = Observer<OnionmasqEvent> { event: OnionmasqEvent ->
            when (event) {
                is BootstrapEvent -> {
                    if (event.isReadyForTraffic) {
                        VpnStatusObservable.update(ConnectionState.CONNECTED)
                    }
                    LogObservable.getInstance()
                        .addLog(getString(R.string.bootstrap_at, event.bootstrapStatus))
                }
                is NewConnectionEvent -> {
                    LogObservable.getInstance().addLog(
                        getString(
                            R.string.new_connection,
                            event.proxySrc,
                            event.proxyDst,
                            event.torDst,
                            event.appId
                        )
                    )
                    for ((i, relay) in event.circuit.withIndex()) {
                        val identity = relay.ed_identity ?: relay.rsa_identity ?: "unknown"
                        val address =
                            if (relay.addresses.size > 0) relay.addresses[0] else "unknown"
                        LogObservable.getInstance().addLog(
                            getString(
                                R.string.new_connection_hop,
                                event.proxySrc,
                                event.proxyDst,
                                i,
                                address,
                                identity
                            )
                        )
                    }
                    VpnStatusObservable.handleConnectionEvent(event)
                }
                is FailedConnectionEvent -> {
                    LogObservable.getInstance().addLog(
                        getString(
                            R.string.failed_connection,
                            event.proxySrc,
                            event.proxyDst,
                            event.torDst,
                            event.error,
                            event.appId
                        )
                    )
                    VpnStatusObservable.handleConnectionEvent(event)
                }
                is ClosedConnectionEvent -> {
                    event.error?.let {
                        LogObservable.getInstance().addLog(
                            getString(
                                R.string.failed_connection_closed,
                                event.proxySrc,
                                event.proxyDst,
                                event.error
                            )
                        )
                    } ?: run {
                        LogObservable.getInstance().addLog(
                            getString(
                                R.string.closed_connection,
                                event.proxySrc,
                                event.proxyDst
                            )
                        )
                    }
                    VpnStatusObservable.handleConnectionEvent(event)
                }
            }
        }

        vpnStatusObserver = Observer<ConnectionState> { connectionState: ConnectionState? ->
            notificationManager.updateNotification(
                connectionState!!,
                VpnStatusObservable.dataUsage.value!!
            );
        }
        vpnDataUsageObserver = Observer<DataUsage> { dataUsage: DataUsage? ->
            notificationManager.updateNotification(
                VpnStatusObservable.statusLiveData.value!!,
                dataUsage!!
            )
        }
    }

    private fun startListeningObservers() {
        observer?.let {
            mainHandler.post { OnionMasq.getEventObservable().observeForever(it) }
        }

        vpnStatusObserver?.let {
            mainHandler.post { VpnStatusObservable.statusLiveData.observeForever(it) }
        }
        vpnDataUsageObserver?.let {
            mainHandler.post { VpnStatusObservable.dataUsage.observeForever(it) }
        }
    }

    private fun removeObservers() {
        observer?.let {
            mainHandler.post { OnionMasq.getEventObservable().removeObserver(it) }
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
        builder.addAddress("169.254.42.1", 16)
        builder.addAddress("fc00::", 7)
        builder.addDnsServer("169.254.42.53")
        builder.addDnsServer("fe80::53")
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
            fd?.let { fd ->
                Log.d(TAG, "service: stopping previous Tor session...")
                OnionMasq.stop()
                fd.close()
            } ?: run {
                logHelper.readLog()
                createObservers()
                startListeningObservers()
                timer.schedule(object: TimerTask() {
                    override fun run() {
                        VpnStatusObservable.updateDataUsage(
                            OnionMasq.getBytesReceived(),
                            OnionMasq.getBytesSent()
                        )
                    }
                }, 1000, 1000)
            }

            fd = builder.establish();
            coroutineScope.async {
                OnionMasq.start(fd!!.detachFd(), getBridgeLines())
            }
        } catch (e: Exception) {
            // Catch any exception
            e.printStackTrace()
            stop(true)
        }
    }

    private fun getBridgeLines(): String? {
        if (!preferenceHelper.useBridge) {
            return null
        }

        return when (preferenceHelper.bridgeType) {
            BridgeType.None -> null
            BridgeType.Manual -> getManualBridgeLines()
            BridgeType.Snowflake -> readAsset(this, "snowflake.txt")
            BridgeType.Obfs4 -> readAsset(this, "obfs4.txt")
        }
    }

    private fun getManualBridgeLines(): String? {
        val bridgeSet = preferenceHelper.bridgeLines
        if (bridgeSet.isEmpty()) {
            return null
        }
        var bridgeLines = "";
        for (line in bridgeSet) {
            bridgeLines += "$line\n"
        }

        return bridgeLines;
    }


    /**
     * Adds selected app into 'allowed apps' for current vpn connection. Only selected apps will use VPN.
     * @param builder VPN Builder
     */
    private fun applyAppFilter(builder: Builder) {
        if (preferenceHelper.protectAllApps) {
            // no filtering, all apps will be routed over the VPN
            return
        }
        val selectedApps: Set<String> = preferenceHelper.protectedApps ?:
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