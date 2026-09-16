package com.example.kotlintest.pages.common

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.kotlintest.R
import org.hamcrest.Matchers.not

/**
 * Row-level actions on recycler_devices, shared by any test that needs to find a
 * device by name inside the RecyclerView - it has no per-row id, so every lookup goes
 * through hasDescendant(withText(name)) instead.
 */
object DeviceRowActions {

    fun clickRowNamed(deviceName: String) {
        onView(withId(R.id.recycler_devices)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(deviceName)), click()
            )
        )
    }

    fun assertRowVisible(deviceName: String) {
        onView(withId(R.id.recycler_devices)).check(matches(hasDescendant(withText(deviceName))))
    }

    fun assertRowAbsent(deviceName: String) {
        onView(withId(R.id.recycler_devices)).check(matches(not(hasDescendant(withText(deviceName)))))
    }

    fun scrollToPosition(position: Int) {
        onView(withId(R.id.recycler_devices)).perform(
            RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(position)
        )
    }
}
