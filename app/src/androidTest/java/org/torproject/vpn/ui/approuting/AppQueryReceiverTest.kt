package org.torproject.vpn.ui.approuting

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.net.Uri
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.torproject.vpn.ui.approuting.data.AppManager
import org.torproject.vpn.utils.PreferenceHelper
import org.torproject.vpn.utils.PreferenceHelper.Companion.PROTECTED_APPS
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class AppQueryReceiverTest {

    private lateinit var receiver: AppQueryReceiver
    private lateinit var appManager: AppManager
    private lateinit var preferenceHelper: PreferenceHelper

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        receiver = AppQueryReceiver()
        appManager = AppManager(context)
        preferenceHelper = PreferenceHelper(context)
        preferenceHelper.clear()
    }

    @Test
    fun testOnReceive_packageAdded() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val packageName = "com.example.testapp"
        val intent = Intent(Intent.ACTION_PACKAGE_ADDED).apply {
            data = Uri.parse("package:$packageName")
        }

        val countDownLatch = CountDownLatch(1);
        var found = false;
        val listener = OnSharedPreferenceChangeListener{ sharedPreference: SharedPreferences, s: String? ->
            if (s == PROTECTED_APPS) {
                val protectedApps = sharedPreference.getStringSet(PROTECTED_APPS, mutableSetOf<String>()).orEmpty().toMutableSet()
                Log.d("TEST", "PROTECTED_APPS changed: $protectedApps")
                if (protectedApps.contains(packageName)) {
                    found = true
                    countDownLatch.countDown()
                }
            }
        }
        preferenceHelper.registerListener(listener)
        CoroutineScope(Dispatchers.IO).launch(Dispatchers.Default) {
            // There's a race condition between the TorApplication instance, which queries all installed apps
            // and the app query call triggered by the broadcast receiver under test (AppManager.queryInstalledApps(protectedApps: MutableSet<String>))
            // In real world scenario this won't be an issue, because
            // 1. normally the TorApplication instance is already running when the broadcast receiver receives any events
            // 2. for the very rare race condition that at the exact same moment tor-vpn was started, while
            // the just registered broadcast receiver receives an ACTION_PACKAGE_ADDED intent, any of both triggered queries
            // will add the new package name to the protectedApps shared preferences. While querying apps
            // any newly installed apps will be added per default to the protected apps.
            //
            // So what's the problem with this test?
            // We're faking in this test that the broadcast receiver was triggered for an ACTION_PACKAGE_ADDED intent,
            // but there's actually no com.example.testapp installed on the device.
            // Whenever TorApplication's app query looses the race, it overwrites our mocked shared preferences and the protectedApps entry contains
            // whatever is actually installed on the device or emulator (and as said, com.example.testapp doesn't belong to it).
            // As a fix, we just sleep a little bit after starting the test, so that TorApplication's app query always wins the race.
            Thread.sleep(1000)
            receiver.onReceive(context, intent)
        }
        countDownLatch.await(2, TimeUnit.SECONDS)
        preferenceHelper.unregisterListener(listener)
        assertTrue("expected $packageName in protectedApps", found)
    }


    @Test
    fun testOnReceive_packageRemoved() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val packageName = "com.example.testapp"
        val intent = Intent(Intent.ACTION_PACKAGE_REMOVED).apply {
            data = Uri.parse("package:$packageName")
        }
        // Add the package first to ensure it can be removed.
        preferenceHelper.protectedApps = mutableSetOf(packageName)
        assertTrue("expected $packageName part of protected apps", preferenceHelper.protectedApps.contains(packageName))
        val countDownLatch = CountDownLatch(1);
        var removed = false;
        val listener = OnSharedPreferenceChangeListener{ sharedPreference: SharedPreferences, s: String? ->
            if (s == PROTECTED_APPS) {
                val protectedApps = sharedPreference.getStringSet(PROTECTED_APPS, mutableSetOf<String>()).orEmpty().toMutableSet()
                if (!protectedApps.contains(packageName)) {
                    removed = true
                    countDownLatch.countDown()
                }
            }
        }
        preferenceHelper.registerListener(listener)
        CoroutineScope(Dispatchers.IO).launch(Dispatchers.Default) {
            receiver.onReceive(context, intent)
        }
        countDownLatch.await(1, TimeUnit.SECONDS)
        preferenceHelper.unregisterListener(listener)

        assertTrue("expected $packageName not in protectedApps", removed)
    }

    @Test
    fun testOnReceive_invalidAction_doesNothing() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val packageName = "com.example.testapp"
        val intent = Intent(Intent.ACTION_PACKAGE_REPLACED).apply {
            data = Uri.parse("package:$packageName")
        }
        val initialProtectedApps = preferenceHelper.protectedApps
        receiver.onReceive(context, intent)
        assert(initialProtectedApps == preferenceHelper.protectedApps)
    }

    @Test
    fun testOnReceive_nullContext_doesNothing() {
        val packageName = "com.example.testapp"
        val intent = Intent(Intent.ACTION_PACKAGE_ADDED).apply {
            data = Uri.parse("package:$packageName")
        }
        val initialProtectedApps = preferenceHelper.protectedApps
        receiver.onReceive(null, intent)
        assert(initialProtectedApps == preferenceHelper.protectedApps)
    }

    @Test
    fun testOnReceive_nullIntent_doesNothing() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val initialProtectedApps = preferenceHelper.protectedApps
        receiver.onReceive(context, null)
        assert(initialProtectedApps == preferenceHelper.protectedApps)
    }

    @Test
    fun testOnReceive_noPackageData_doesNothing() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(Intent.ACTION_PACKAGE_ADDED)
        val initialProtectedApps = preferenceHelper.protectedApps
        receiver.onReceive(context, intent)
        assert(initialProtectedApps == preferenceHelper.protectedApps)
    }
}