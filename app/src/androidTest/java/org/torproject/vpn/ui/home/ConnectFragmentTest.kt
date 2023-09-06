package org.torproject.vpn.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiSelector
import junit.framework.TestCase.*
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.torproject.vpn.MainActivity
import org.torproject.vpn.R
import org.torproject.vpn.utils.CustomInteractions.tryResolve
import org.torproject.vpn.utils.PreferenceHelper
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.locale.LocaleTestRule


@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ConnectFragmentTest {
    @get:Rule
    val rule = activityScenarioRule<MainActivity>()

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    var device: UiDevice? = null

    @Before
    fun setup() {
        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        device = UiDevice.getInstance(instrumentation)
    }

    @Test
    fun test00PrepareTestsClearPreferences() {
        // This is a work-around to ensure the shared preferences have been cleared in the following test
        // before the ConnectFragment has been initialized
        val preferenceHelper = PreferenceHelper(ApplicationProvider.getApplicationContext())
        preferenceHelper.clear()

    }

    @Test
    fun test01ExperimentalDialog() {
        val preferenceHelper = PreferenceHelper(ApplicationProvider.getApplicationContext())

        assertTrue(
            "PreferenceHelper.shouldShowGuide should be true",
            preferenceHelper.shouldShowGuide
        )
        onView(withId(R.id.include_help)).check(matches(isDisplayed()))
        Screengrab.screenshot("connect_fragment_experimental_hint")
        onView(withId(R.id.bt_got_it)).perform(click())
        onView(withId(R.id.tv_connect_action_btn)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.cl_selection_exit_inner)).check(matches(isCompletelyDisplayed()))
        assertFalse(
            "PreferenceHelper.shouldShowGuide should be false",
            preferenceHelper.shouldShowGuide
        )
    }

    @Test
    fun test02InitialState() {
        onView(withId(R.id.tv_connect_action_btn)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.cl_selection_exit_inner)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isCompletelyDisplayed()))

        onView(withId(R.id.toolbar)).check(matches(hasDescendant(withText(R.string.app_name))))
        onView(withId(R.id.tv_connect_action_btn)).check(matches(withText(R.string.action_connect)))
        Screengrab.screenshot("connect_fragment_initial_state")
    }

    @Test
    fun test03Connect() {
        // determine required permissions
        var needsNotificationPermission = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    ApplicationProvider.getApplicationContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                needsNotificationPermission = true
            }
        }
        val needsVpnPermission =
            VpnService.prepare(ApplicationProvider.getApplicationContext()) != null

        // start VPN
        onView(withId(R.id.tv_connect_action_btn)).perform(click())

        if (needsNotificationPermission) {
            // handle Notification permission dialog
            val okButton: UiObject = device!!.findObject(UiSelector().text("Allow"))
            okButton.click()
        }


        if (needsVpnPermission) {
            // handle VPN permission dialog
            val okButton = device!!.findObject(
                UiSelector().packageName("com.android.vpndialogs").resourceId("android:id/button1")
            )
            okButton.click()
        }

        onView(withId(R.id.toolbar)).check(matches(hasDescendant(withText(R.string.state_connecting))))
        onView(withId(R.id.tv_connect_action_btn)).check(matches(withText(R.string.action_cancel)))
        Screengrab.screenshot("connect_fragment_connecting_state")

        tryResolve(
            onView(withId(R.id.toolbar)),
            matches(hasDescendant(withText(R.string.state_connected))),
            60
        )
        Screengrab.screenshot("connect_fragment_connected_state")
    }

    @Test
    fun test04Disconnect() {
        onView(withId(R.id.tv_connect_action_btn)).perform(click())
        tryResolve(
            onView(withId(R.id.toolbar)),
            matches(hasDescendant(withText(R.string.state_disconnected))),
            5
        )
        Screengrab.screenshot("connect_fragment_disconnected_state")
    }


}