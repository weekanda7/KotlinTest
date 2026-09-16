package com.example.kotlintest.pages.device

import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.kotlintest.R

object DeviceDetailPage {

    fun assertName(name: String) {
        onView(withId(R.id.text_name)).check(matches(withText(name)))
    }

    fun assertStatus(@StringRes statusRes: Int) {
        onView(withId(R.id.text_status)).check(matches(withText(statusRes)))
    }

    fun assertIpAddress(ipAddress: String) {
        onView(withId(R.id.text_ip_address)).check(matches(withText(ipAddress)))
    }

    fun tapDelete() {
        onView(withId(R.id.button_delete)).perform(click())
    }

    fun confirmDelete() {
        onView(withText(R.string.dialog_delete_positive)).inRoot(isDialog()).perform(click())
    }

    fun cancelDelete() {
        onView(withText(R.string.dialog_delete_negative)).inRoot(isDialog()).perform(click())
    }
}
