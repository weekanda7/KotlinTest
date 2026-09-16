package com.example.test1

import android.widget.DatePicker
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddDeviceFlowTest {

    @get:Rule(order = 0)
    val resetDeviceCatalogRule = ResetDeviceCatalogRule()

    @get:Rule(order = 1)
    val idlingResourceRule = IdlingResourceRule()

    @get:Rule(order = 2)
    val activityRule = ActivityScenarioRule(DeviceListActivity::class.java)

    @Test
    fun addingDevice_appearsInListWithConfirmation() {
        onView(withId(R.id.fab_add_device)).perform(click())

        // replaceText() sets the EditText's content directly instead of simulating
        // keystrokes through the IME - typeText() here was observed to randomly drop
        // trailing characters due to the emulator keyboard's composing/commit timing.
        onView(withId(R.id.edit_name)).perform(replaceText("testdevice"), closeSoftKeyboard())
        onView(withId(R.id.edit_ip_address)).perform(replaceText("10.0.0.99"), closeSoftKeyboard())

        onView(withId(R.id.dropdown_type)).perform(click())
        onView(withText("Router")).inRoot(isPlatformPopup()).perform(click())

        onView(withId(R.id.edit_install_date)).perform(click())
        onView(isAssignableFrom(DatePicker::class.java)).perform(PickerActions.setDate(2026, 1, 15))
        onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click())

        onView(withId(R.id.button_save)).perform(click())

        onView(withText(R.string.text_device_added)).check(matches(isDisplayed()))

        // Confirms the save actually persisted the right name at the data layer,
        // independent of whether the RecyclerView has finished laying out the newly
        // added row yet.
        assertEquals("testdevice", DeviceCatalog.all.last().name)

        // Force the RecyclerView to lay out the just-inserted row before asserting on
        // it - notifyDataSetChanged() growing the item count can otherwise leave the
        // new position unbound at the moment Espresso's idle check passes.
        onView(withId(R.id.recycler_devices)).perform(
            RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(DeviceCatalog.all.size - 1)
        )
        onView(withId(R.id.recycler_devices)).check(matches(hasDescendant(withText("testdevice"))))
    }

    @Test
    fun savingWithoutRequiredFields_showsValidationErrors() {
        onView(withId(R.id.fab_add_device)).perform(click())
        onView(withId(R.id.button_save)).perform(click())

        onView(withText(R.string.error_name_required)).check(matches(isDisplayed()))
        onView(withText(R.string.error_ip_required)).check(matches(isDisplayed()))
    }
}
