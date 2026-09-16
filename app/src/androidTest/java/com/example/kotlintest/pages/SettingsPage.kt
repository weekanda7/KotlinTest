package com.example.kotlintest.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.example.kotlintest.R

/** Settings tab content only - the bottom nav that switches to this tab lives in DeviceListPage. */
object SettingsPage {

    fun toggleNotifications() {
        onView(withId(R.id.switch_notifications)).perform(click())
    }

    fun toggleAutoRefresh() {
        onView(withId(R.id.switch_auto_refresh)).perform(click())
    }

    fun assertNotificationsChecked() {
        onView(withId(R.id.switch_notifications)).check(matches(isChecked()))
    }

    fun assertAutoRefreshChecked() {
        onView(withId(R.id.switch_auto_refresh)).check(matches(isChecked()))
    }

    fun assertScreenDisplayed() {
        onView(withId(R.id.switch_notifications)).check(matches(isDisplayed()))
    }
}
