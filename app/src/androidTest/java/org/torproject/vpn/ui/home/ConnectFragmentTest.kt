package org.torproject.vpn.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiSelector
import junit.framework.TestCase.*
import org.hamcrest.*
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.torproject.vpn.MainActivity
import org.torproject.vpn.R
import org.torproject.vpn.ui.exitselection.data.ExitNodeAdapter
import org.torproject.vpn.utils.CustomInteractions.tryResolve
import org.torproject.vpn.utils.CustomViewActions
import org.torproject.vpn.utils.NetworkUtils
import org.torproject.vpn.utils.PreferenceHelper
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.locale.LocaleTestRule
import java.io.*
import java.util.*
import org.hamcrest.CoreMatchers.`is` as Is

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
            120
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

    @Test
    fun test05CountrySelection() {
        onView(withId(R.id.cl_selection_exit_inner)).perform(click())
        onView(withId(R.id.rv_exit_nodes)).check(matches(isDisplayed()))
        Screengrab.screenshot("exit_selection_all_apps_selected")

        onView(withId(R.id.rv_exit_nodes)).perform(swipeDown())
        tryResolve(onView(withId(R.id.tv_connect_action_btn)), matches(isCompletelyDisplayed()), 3)

        onView(withId(R.id.cl_selection_exit_inner)).perform(click())

        val viewAction = CustomViewActions.actionOnItemView(withId(R.id.smProtectAllApps), click())
        onView(withId(R.id.rv_exit_nodes)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, viewAction))

        onView(withId(R.id.rv_exit_nodes)).perform(
            RecyclerViewActions.actionOnHolderItem(Matchers.allOf(
                Is(instanceOf(ExitNodeAdapter.ExitNodeCellViewHolder::class.java)),
                containsCountryName(Locale.US)),
            click()))

        Screengrab.screenshot("exit_selection_US_selected")

        onView(withId(R.id.rv_exit_nodes)).perform(pressBack())
        tryResolve(onView(withId(R.id.cl_selection_exit_inner)), matches(isCompletelyDisplayed()), 3)
        onView(withId(R.id.imageView6)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.imageView6)).check(matches(withContentDescription("us")))

        Screengrab.screenshot("connect_fragment_US_selected")
    }

    @Test
    fun test06CountrySelection() {
        onView(withId(R.id.imageView6)).check(matches(withContentDescription("us")))

        // start VPN
        onView(withId(R.id.tv_connect_action_btn)).perform(click())
        tryResolve(
            onView(withId(R.id.toolbar)),
            matches(hasDescendant(withText(R.string.state_connected))),
            60
        )

        val geoIPLocale =  NetworkUtils.getGeoIPLocale()
        Log.d("TEST", "geoIP locale: $geoIPLocale")
        assertEquals("expected geoIP location is USA","United States", geoIPLocale)
    }

    private fun containsCountryName(locale: Locale): Matcher<ExitNodeAdapter.ExitNodeCellViewHolder> {
        return object : TypeSafeMatcher<ExitNodeAdapter.ExitNodeCellViewHolder>() {
            override fun matchesSafely(customHolder: ExitNodeAdapter.ExitNodeCellViewHolder): Boolean {
                return locale.displayCountry == customHolder.binding.tvTitle.text
            }

            override fun describeTo(description: Description) {
                description.appendText("item with Locale ${locale.displayCountry} found")
            }
        }
    }
}