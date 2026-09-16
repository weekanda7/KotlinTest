package com.example.kotlintest

import androidx.appcompat.R as AppCompatR
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceSearchTest {

    @get:Rule(order = 0)
    val resetDeviceCatalogRule = ResetDeviceCatalogRule()

    @get:Rule(order = 1)
    val idlingResourceRule = IdlingResourceRule()

    @get:Rule(order = 2)
    val activityRule = ActivityScenarioRule(DeviceListActivity::class.java)

    // search_devices is always expanded (android:iconifiedByDefault="false"), so its query
    // field is present as soon as the idling resource confirms the initial load finished -
    // no click-to-expand step needed.
    //
    // replaceText() sets the query field's content directly instead of simulating
    // keystrokes through the IME - typeText() here was observed to randomly drop
    // characters (e.g. "router" -> "r") due to the emulator keyboard's composing/commit
    // timing, which produced flaky, timing-dependent filter results.

    @Test
    fun searchingByName_filtersToMatchingDevices() {
        onView(withId(AppCompatR.id.search_src_text)).perform(replaceText("router"))

        onView(withId(R.id.recycler_devices)).check(matches(hasDescendant(withText("router-main"))))
        onView(withId(R.id.recycler_devices)).check(matches(not(hasDescendant(withText("server-01")))))
    }

    @Test
    fun searchingWithNoMatches_showsEmptyState() {
        onView(withId(AppCompatR.id.search_src_text)).perform(replaceText("nonexistent-device"))

        onView(withId(R.id.text_empty_state)).check(matches(isDisplayed()))
    }

    @Test
    fun clearingSearch_restoresFullList() {
        onView(withId(AppCompatR.id.search_src_text)).perform(replaceText("router"))
        onView(withId(AppCompatR.id.search_src_text)).perform(clearText())

        DeviceCatalog.all.forEach { device ->
            onView(withId(R.id.recycler_devices)).check(matches(hasDescendant(withText(device.name))))
        }
    }
}
