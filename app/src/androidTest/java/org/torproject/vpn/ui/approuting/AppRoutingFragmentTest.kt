package org.torproject.vpn.ui.approuting

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.hamcrest.CoreMatchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.torproject.vpn.MainActivity
import org.torproject.vpn.R
import org.torproject.vpn.utils.CustomInteractions
import org.torproject.vpn.utils.NetworkUtils
import org.torproject.vpn.vpn.VpnStatusObservable
import tools.fastlane.screengrab.locale.LocaleTestRule


@RunWith(AndroidJUnit4::class)
@LargeTest
class AppRoutingFragmentTest {

    @get:Rule
    val rule = activityScenarioRule<MainActivity>()

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @Test
    fun testChangeIPsGlobally() {
        if (!VpnStatusObservable.isVPNActive()) {
            onView(withId(R.id.tv_connect_action_btn)).perform(click())
            CustomInteractions.tryResolve(
                onView(withId(R.id.toolbar)),
                matches(ViewMatchers.hasDescendant(withText(R.string.state_connected))),
                120
            )
        }
        openAppFragment()
        val currentIP = NetworkUtils.getExitIP()
        assertTrue("exit IP remains same", currentIP == NetworkUtils.getExitIP())
        // menu item click
        onView(withContentDescription(R.string.action_refresh_circuits)).perform(click())
        // dialog action click
        onView(withText(R.string.action_refresh)).perform(click())

        val newIP = NetworkUtils.getExitIP();

        assertNotNull(newIP)
        assertNotNull(currentIP)
        assertFalse("IP changed after global IP switch (currentIP $currentIP - new IP $newIP)", newIP == currentIP)
    }

    private fun openAppFragment() {
        onView(
            CoreMatchers.allOf(
                withText(R.string.action_configure),
                ViewMatchers.withResourceName("navigation_bar_item_small_label_view")
            )
        ).perform(click())

        onView(withId(R.id.apps)).perform(click())
    }
}