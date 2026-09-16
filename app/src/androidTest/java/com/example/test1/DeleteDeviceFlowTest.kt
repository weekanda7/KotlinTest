package com.example.test1

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.RootMatchers.isDialog
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
class DeleteDeviceFlowTest {

    @get:Rule(order = 0)
    val resetDeviceCatalogRule = ResetDeviceCatalogRule()

    @get:Rule(order = 1)
    val idlingResourceRule = IdlingResourceRule()

    @get:Rule(order = 2)
    val activityRule = ActivityScenarioRule(DeviceListActivity::class.java)

    @Test
    fun deletingDevice_removesFromListWithConfirmation() {
        onView(withId(R.id.recycler_devices)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("nas-backup")), click()
            )
        )

        onView(withId(R.id.button_delete)).perform(click())
        onView(withText(R.string.dialog_delete_positive)).inRoot(isDialog()).perform(click())

        onView(withText(R.string.text_device_deleted)).check(matches(isDisplayed()))
        onView(withId(R.id.recycler_devices)).check(matches(not(hasDescendant(withText("nas-backup")))))
    }

    @Test
    fun cancellingDeleteDialog_keepsDeviceInList() {
        onView(withId(R.id.recycler_devices)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("nas-backup")), click()
            )
        )

        onView(withId(R.id.button_delete)).perform(click())
        onView(withText(R.string.dialog_delete_negative)).inRoot(isDialog()).perform(click())

        onView(withId(R.id.text_name)).check(matches(withText("nas-backup")))
    }
}
