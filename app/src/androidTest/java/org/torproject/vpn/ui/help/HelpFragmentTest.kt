package org.torproject.vpn.ui.help

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.appbar.CollapsingToolbarLayout
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.torproject.vpn.MainActivity
import org.torproject.vpn.R
import org.torproject.vpn.utils.CustomMatchers
import org.torproject.vpn.utils.PreferenceHelper
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.locale.LocaleTestRule


@RunWith(AndroidJUnit4::class)
@LargeTest
class HelpFragmentTest {

    @get:Rule
    val rule = activityScenarioRule<MainActivity>()
    @get:Rule
    val intentsRule = IntentsRule()


    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @Before
    fun setup() {
        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())

        val context: Context = ApplicationProvider.getApplicationContext()
        val sharedPreference =
            context.applicationContext.getSharedPreferences("tor-vpn", Context.MODE_PRIVATE)
        sharedPreference.edit().putBoolean(PreferenceHelper.SHOULD_SHOW_GUIDE, false).commit()
    }

    @Test
    fun testHelpNavigation() {
        openHelpFragment()

        onView(isAssignableFrom(CollapsingToolbarLayout::class.java)).check(
            matches(
                CustomMatchers.withCollapsibleToolbarTitle(
                    containsString(
                        InstrumentationRegistry.getInstrumentation().targetContext.getString(
                            R.string.help
                        )
                    )
                )
            )
        )

        Screengrab.screenshot("help_fragment")
    }

    @Test
    fun testHelpNavigationFAQ() {
        openHelpFragment()
        onView(withId(R.id.ll_faq_container)).perform(click())

        intended(allOf(
            hasAction(Intent.ACTION_VIEW),
            hasData(Uri.parse("https://support.torproject.org"))));
    }

    @Test
    fun testHelpNavigationReportBug() {
        openReportBugBottomSheet()

        Screengrab.screenshot("help_fragment_report_bug")

        onView(withId(R.id.cl_report_bug_container)).perform(ViewActions.swipeDown())
    }

    @Test
    fun testHelpNavigationReportBugForum() {
        openReportBugBottomSheet()
        onView(withId(R.id.ll_tor_forum)).perform(click())

        val webpage: Uri = Uri.parse("https://forum.torproject.org")
        intended(allOf(
            hasAction(Intent.ACTION_VIEW),
            hasData(webpage)))
    }

    @Test
    fun testHelpNavigationReportBugGitlab() {
        openReportBugBottomSheet()
        onView(withId(R.id.ll_gitlab)).perform(click())

        val webpage: Uri = Uri.parse("https://gitlab.torproject.org/tpo/applications/vpn/-/issues")
        intended(allOf(
            hasAction(Intent.ACTION_VIEW),
            hasData(webpage)))
    }

    @Test
    fun testHelpNavigationContactReport() {
        openContactSupportBottomSheet()
        Screengrab.screenshot("help_fragment_contact_support")
        onView(withId(R.id.cl_contact_support_container)).perform(swipeDown())
    }

    private fun openReportBugBottomSheet() {
        openHelpFragment()
        onView(withId(R.id.ll_reort_bugs_container)).perform(click())


        onView(
            withId(R.id.cl_report_bug_container)
        ).check(matches(isCompletelyDisplayed()))
    }

    private fun openContactSupportBottomSheet() {
        openHelpFragment()
        onView(withId(R.id.ll_contact_support_container)).perform(click())

        onView(withId(R.id.cl_contact_support_container)).check(matches(isCompletelyDisplayed()))
    }

    private fun openHelpFragment() {
        onView(
            allOf(
                ViewMatchers.withText(R.string.help),
                ViewMatchers.withResourceName("navigation_bar_item_small_label_view")
            )
        ).perform(click())

    }
}