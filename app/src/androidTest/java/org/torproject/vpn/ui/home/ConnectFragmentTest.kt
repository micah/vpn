package org.torproject.vpn.ui.home

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.torproject.vpn.MainActivity
import org.torproject.vpn.R
import org.torproject.vpn.utils.CustomInteractions.tryResolve
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

    @Test
    fun test01InitialState() {
        onView(withId(R.id.tv_connect_action_btn)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.cl_selection_exit_inner)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isCompletelyDisplayed()))

        onView(withId(R.id.toolbar)).check(matches(hasDescendant(withText(R.string.app_name))))
        onView(withId(R.id.tv_connect_action_btn)).check(matches(withText(R.string.action_connect)))
    }

    @Test
    fun test02Connect() {
        onView(withId(R.id.tv_connect_action_btn)).perform(click())
        onView(withId(R.id.toolbar)).check(matches(hasDescendant(withText(R.string.state_connecting))))
        onView(withId(R.id.tv_connect_action_btn)).check(matches(withText(R.string.action_cancel)))

        tryResolve(onView(withId(R.id.toolbar)), matches(hasDescendant(withText(R.string.state_connected))), 60)
    }

    @Test
    fun test03Disconnect() {
        onView(withId(R.id.tv_connect_action_btn)).perform(click())
        tryResolve(onView(withId(R.id.toolbar)), matches(hasDescendant(withText(R.string.state_disconnected))), 5)
    }


}