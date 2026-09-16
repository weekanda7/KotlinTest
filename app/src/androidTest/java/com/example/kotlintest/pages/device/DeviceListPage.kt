package com.example.kotlintest.pages.device

import androidx.appcompat.R as AppCompatR
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.kotlintest.R
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not

/** DeviceListActivity shell (toolbar + bottom nav) and the Devices tab content. */
object DeviceListPage {

    // The bottom nav's tab labels ("Devices"/"Settings") happen to read the same as
    // the toolbar title, so a plain withText(...) matches both once they're on screen
    // together - scope to the toolbar to disambiguate.
    private fun toolbarTitle(textRes: Int) = allOf(withText(textRes), isDescendantOfA(withId(R.id.toolbar)))

    fun openDevicesTab() {
        onView(withId(R.id.navigation_devices)).perform(click())
    }

    fun openSettingsTab() {
        onView(withId(R.id.navigation_settings)).perform(click())
    }

    fun assertToolbarTitle(textRes: Int) {
        onView(toolbarTitle(textRes)).check(matches(isDisplayed()))
    }

    fun search(query: String) {
        onView(withId(AppCompatR.id.search_src_text)).perform(replaceText(query))
    }

    fun clearSearch() {
        onView(withId(AppCompatR.id.search_src_text)).perform(clearText())
    }

    fun tapAddDevice() {
        onView(withId(R.id.fab_add_device)).perform(click())
    }

    fun assertProgressBarHidden() {
        onView(withId(R.id.progress_loading)).check(matches(not(isDisplayed())))
    }

    fun assertListVisible() {
        onView(withId(R.id.recycler_devices)).check(matches(isDisplayed()))
    }

    fun assertFabVisible() {
        onView(withId(R.id.fab_add_device)).check(matches(isDisplayed()))
    }

    fun assertFabHidden() {
        onView(withId(R.id.fab_add_device)).check(matches(not(isDisplayed())))
    }

    fun assertEmptyStateVisible() {
        onView(withId(R.id.text_empty_state)).check(matches(isDisplayed()))
    }
}
