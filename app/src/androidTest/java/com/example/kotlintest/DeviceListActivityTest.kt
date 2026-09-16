package com.example.kotlintest

import androidx.appcompat.R as AppCompatR
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceListActivityTest {

    // The bottom nav's tab labels ("Devices"/"Settings") happen to read the same as
    // the toolbar title, so a plain withText(...) matches both once they're on screen
    // together - scope to the toolbar to disambiguate.
    private fun toolbarTitle(textRes: Int) = allOf(withText(textRes), isDescendantOfA(withId(R.id.toolbar)))

    @get:Rule(order = 0)
    val resetDeviceCatalogRule = ResetDeviceCatalogRule()

    @get:Rule(order = 1)
    val idlingResourceRule = IdlingResourceRule()

    @get:Rule(order = 2)
    val activityRule = ActivityScenarioRule(DeviceListActivity::class.java)

    @Test
    fun deviceList_hidesProgressBarOnceLoaded() {
        // Espresso blocks here until EspressoIdlingResource is idle, i.e. until
        // DeviceRepository's simulated network delay has finished.
        onView(withId(R.id.progress_loading)).check(matches(not(isDisplayed())))
        onView(withId(R.id.recycler_devices)).check(matches(isDisplayed()))
    }

    @Test
    fun deviceList_showsAllMockDevices() {
        DeviceCatalog.all.forEach { device ->
            onView(withId(R.id.recycler_devices)).check(
                matches(hasDescendant(withText(device.name)))
            )
        }
    }

    @Test
    fun tappingDevice_navigatesToDetailWithMatchingName() {
        val target = DeviceCatalog.all.first()

        onView(withId(R.id.recycler_devices)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(target.name)), click()
            )
        )

        onView(
            allOf(withId(R.id.text_name), withText(target.name))
        ).check(matches(isDisplayed()))
        onView(withId(R.id.text_ip_address)).check(matches(withText(target.ipAddress)))
    }

    @Test
    fun tappingSettingsTab_opensSettingsScreen() {
        onView(withId(R.id.navigation_settings)).perform(click())

        onView(toolbarTitle(R.string.title_settings)).check(matches(isDisplayed()))
    }

    @Test
    fun switchingTabs_togglesFabAndContentVisibility() {
        onView(withId(R.id.fab_add_device)).check(matches(isDisplayed()))

        onView(withId(R.id.navigation_settings)).perform(click())
        onView(toolbarTitle(R.string.title_settings)).check(matches(isDisplayed()))
        onView(withId(R.id.switch_notifications)).check(matches(isDisplayed()))
        onView(withId(R.id.fab_add_device)).check(matches(not(isDisplayed())))

        onView(withId(R.id.navigation_devices)).perform(click())
        onView(toolbarTitle(R.string.title_devices)).check(matches(isDisplayed()))
        onView(withId(R.id.recycler_devices)).check(matches(isDisplayed()))
        onView(withId(R.id.fab_add_device)).check(matches(isDisplayed()))
    }

    @Test
    fun switchingTabsAndBack_preservesSearchQuery() {
        onView(withId(AppCompatR.id.search_src_text)).perform(replaceText("router"))
        onView(withId(R.id.recycler_devices)).check(matches(hasDescendant(withText("router-main"))))

        // show()/hide() keeps the fragment (and its query text) alive instead of
        // recreating it, unlike a plain FragmentTransaction.replace() would.
        onView(withId(R.id.navigation_settings)).perform(click())
        onView(withId(R.id.navigation_devices)).perform(click())

        onView(withId(R.id.recycler_devices)).check(matches(hasDescendant(withText("router-main"))))
        onView(withId(R.id.recycler_devices)).check(matches(not(hasDescendant(withText("server-01")))))
    }
}
