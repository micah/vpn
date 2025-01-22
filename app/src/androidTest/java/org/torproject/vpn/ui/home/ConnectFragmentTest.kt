package org.torproject.vpn.ui.home

import android.util.Log
import androidx.appcompat.widget.AppCompatImageButton
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withResourceName
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import com.google.android.material.appbar.CollapsingToolbarLayout
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.torproject.vpn.MainActivity
import org.torproject.vpn.R
import org.torproject.vpn.ui.exitselection.data.ExitNodeAdapter
import org.torproject.vpn.utils.ConnectHelper
import org.torproject.vpn.utils.CustomInteractions.tryResolve
import org.torproject.vpn.utils.CustomMatchers.withCollapsibleToolbarTitle
import org.torproject.vpn.utils.NetworkUtils
import org.torproject.vpn.utils.PreferenceHelper
import org.torproject.vpn.vpn.ConnectionState
import org.torproject.vpn.vpn.VpnStatusObservable
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.locale.LocaleTestRule
import java.util.Locale
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

    private var device: UiDevice? = null

    @Before
    fun setup() {
        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())
        val instrumentation = getInstrumentation()
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
        preferenceHelper.clear()
        assertTrue(
            "PreferenceHelper.shouldShowGuide should be true",
            preferenceHelper.shouldShowGuide
        )
        onView(withId(R.id.include_help)).check(matches(isDisplayed()))
        Screengrab.screenshot("connect_fragment_experimental_hint")
        onView(withId(R.id.bt_got_it)).perform(click())
        onView(withId(R.id.tv_connect_action_btn)).check(matches(isCompletelyDisplayed()))
        assertFalse(
            "PreferenceHelper.shouldShowGuide should be false",
            preferenceHelper.shouldShowGuide
        )
    }

    @Test
    fun test02InitialState() {
        val preferenceHelper = PreferenceHelper(ApplicationProvider.getApplicationContext())
        preferenceHelper.shouldShowGuide = false
        onView(withId(R.id.tv_connect_action_btn)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isCompletelyDisplayed()))

        onView(withId(R.id.toolbar)).check(matches(hasDescendant(withText(R.string.app_name))))
        onView(withId(R.id.tv_connect_action_btn)).check(matches(withText(R.string.action_connect)))
        onView(withId(R.id.cl_apps_card)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.cl_connection_card)).check(matches(isCompletelyDisplayed()))
        Screengrab.screenshot("connect_fragment_initial_state")
    }

    @Test
    fun test03ClickAppsCardAndBack() {
        val preferenceHelper = PreferenceHelper(ApplicationProvider.getApplicationContext())
        preferenceHelper.shouldShowGuide = false
        onView(withId(R.id.cl_apps_card)).perform(click())
        onView(isAssignableFrom(CollapsingToolbarLayout::class.java)).check(
            matches(withCollapsibleToolbarTitle(
                containsString(getInstrumentation().targetContext.getString(R.string.apps))
            ))
        )

        onView(allOf(instanceOf(AppCompatImageButton::class.java), withParent(withId(R.id.toolbar)))).perform(click())
        onView(withId(R.id.tv_connect_action_btn)).check(matches(isCompletelyDisplayed()))
    }

    @Test
    fun test04ClickConnectionCardAndBack() {
        val preferenceHelper = PreferenceHelper(ApplicationProvider.getApplicationContext())
        preferenceHelper.shouldShowGuide = false
        VpnStatusObservable.update(ConnectionState.INIT)
        onView(withId(R.id.cl_connection_card)).perform(click())
        onView(isAssignableFrom(CollapsingToolbarLayout::class.java)).check(
            matches(withCollapsibleToolbarTitle(
                containsString(getInstrumentation().targetContext.getString(R.string.connection))
            ))
        )

        onView(allOf(instanceOf(AppCompatImageButton::class.java), withParent(withId(R.id.toolbar)))).perform(click())
        onView(withId(R.id.tv_connect_action_btn)).check(matches(isCompletelyDisplayed()))
    }

    @Test
    fun test05Connect() {
        ConnectHelper.connect(device, true)
    }

    @Test
    fun test06ReinitializeConnectedUIState() {
        val preferenceHelper = PreferenceHelper(ApplicationProvider.getApplicationContext())
        preferenceHelper.shouldShowGuide = false
        if (!VpnStatusObservable.isVPNActive()) {
            onView(withId(R.id.tv_connect_action_btn)).perform(click())
            tryResolve(
                onView(withId(R.id.toolbar)),
                matches(hasDescendant(withText(R.string.state_connected))),
                120
            )
        }
        onView(allOf(withText(R.string.action_configure), withResourceName("navigation_bar_item_small_label_view"))).perform(click())
        onView(allOf(withText(R.string.action_connect), withResourceName("navigation_bar_item_small_label_view"))).perform(click())
    }

    @Test
    fun test07Disconnect() {
        val preferenceHelper = PreferenceHelper(ApplicationProvider.getApplicationContext())
        preferenceHelper.shouldShowGuide = false
        if (!VpnStatusObservable.isVPNActive()) {
            ConnectHelper.connect(device, false)
        }
        onView(withId(R.id.tv_connect_action_btn)).perform(click())
        tryResolve(
            onView(withId(R.id.toolbar)),
            matches(hasDescendant(withText(R.string.state_disconnected))),
            5
        )
        // in disconnected state the apps and connection card are hidden
        onView(withId(R.id.cl_apps_card)).check(matches(not(isDisplayed())))
        onView(withId(R.id.cl_connection_card)).check(matches(not(isDisplayed())))
        Screengrab.screenshot("connect_fragment_disconnected_state")
    }

    @Test
    fun test08ReinitializeDisconnectedState() {
        val preferenceHelper = PreferenceHelper(ApplicationProvider.getApplicationContext())
        preferenceHelper.shouldShowGuide = false
        // initialize disconnected state by first connecting, then disconnecting (in contrast to initial state)
        if (!VpnStatusObservable.isVPNActive()) {
            ConnectHelper.connect(device, false)
        }
        if (VpnStatusObservable.isVPNActive()) {
            onView(withId(R.id.tv_connect_action_btn)).perform(click())
            tryResolve(
                onView(withId(R.id.toolbar)),
                matches(hasDescendant(withText(R.string.state_disconnected))),
                5
            )
        }
        onView(allOf(withText(R.string.action_configure), withResourceName("navigation_bar_item_small_label_view"))).perform(click())
        onView(allOf(withText(R.string.action_connect), withResourceName("navigation_bar_item_small_label_view"))).perform(click())
        onView(withId(R.id.cl_apps_card)).check(matches(not(isDisplayed())))
        onView(withId(R.id.cl_connection_card)).check(matches(not(isDisplayed())))
    }

    @Test
    fun test09CountrySelection() {
        ConnectHelper.connect(device, false)
        onView(withId(R.id.cl_selection_exit_inner)).perform(click())
        onView(withId(R.id.rv_exit_nodes)).check(matches(isDisplayed()))
        Screengrab.screenshot("exit_selection_all_apps_selected")

        onView(withId(R.id.rv_exit_nodes)).perform(
            RecyclerViewActions.actionOnHolderItem(Matchers.allOf(
                Is(instanceOf(ExitNodeAdapter.ExitNodeCellViewHolder::class.java)),
                containsCountryName(Locale.US)),
                click()))

        Screengrab.screenshot("exit_selection_US_selected")

        pressBack()
        tryResolve(onView(withId(R.id.cl_selection_exit_inner)), matches(isCompletelyDisplayed()), 3)
        // FIXME: currently there might be a bug during that the app occasionally doesn't update the flag icon
        // I've only seen this before under conditions of the instrumentation test not in real life
        // onView(withId(R.id.imageView6)).check(matches(isCompletelyDisplayed()))
        // onView(withId(R.id.imageView6)).check(matches(withContentDescription("us")))

        Screengrab.screenshot("connect_fragment_US_selected")

        val geoIPLocale =  NetworkUtils.getGeoIPLocale()
        Log.d("TEST", "geoIP locale: $geoIPLocale")
        assertEquals("expected geoIP location is USA","United States", geoIPLocale)
        Screengrab.screenshot("connect_fragment_US_selected")
    }

    private fun containsCountryName(locale: Locale): Matcher<ExitNodeAdapter.ExitNodeCellViewHolder> {
        return object: TypeSafeMatcher<ExitNodeAdapter.ExitNodeCellViewHolder>() {
            override fun matchesSafely(customHolder: ExitNodeAdapter.ExitNodeCellViewHolder): Boolean {
                return locale.displayCountry == customHolder.binding.tvTitle.text
            }

            override fun describeTo(description: Description) {
                description.appendText("item with Locale ${locale.displayCountry} found")
            }
        }
    }
}