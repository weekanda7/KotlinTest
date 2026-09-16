package com.example.kotlintest.pages.device

import android.widget.DatePicker
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.kotlintest.R

object AddDevicePage {

    // replaceText() sets the EditText's content directly instead of simulating
    // keystrokes through the IME - typeText() here was observed to randomly drop
    // trailing characters due to the emulator keyboard's composing/commit timing.
    fun enterName(name: String) {
        onView(withId(R.id.edit_name)).perform(replaceText(name), closeSoftKeyboard())
    }

    fun enterIpAddress(ipAddress: String) {
        onView(withId(R.id.edit_ip_address)).perform(replaceText(ipAddress), closeSoftKeyboard())
    }

    fun selectType(typeName: String) {
        onView(withId(R.id.dropdown_type)).perform(click())
        onView(withText(typeName)).inRoot(isPlatformPopup()).perform(click())
    }

    fun setInstallDate(year: Int, month: Int, dayOfMonth: Int) {
        onView(withId(R.id.edit_install_date)).perform(click())
        onView(isAssignableFrom(DatePicker::class.java)).perform(PickerActions.setDate(year, month, dayOfMonth))
        onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click())
    }

    fun tapSave() {
        onView(withId(R.id.button_save)).perform(click())
    }

    fun assertFieldError(@StringRes errorRes: Int) {
        onView(withText(errorRes)).check(matches(isDisplayed()))
    }
}
