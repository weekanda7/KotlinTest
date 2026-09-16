package com.example.kotlintest.pages

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.kotlintest.R

object HomePage {

    fun assertWelcomeMessage(username: String) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val expected = context.getString(R.string.text_welcome, username)
        onView(withId(R.id.text_welcome)).check(matches(withText(expected)))
    }

    fun assertRememberedMessageShown() {
        onView(withId(R.id.text_remembered)).check(matches(isDisplayed()))
    }

    fun tapViewDevices() {
        onView(withId(R.id.button_view_devices)).perform(click())
    }

    fun tapLogout() {
        onView(withId(R.id.button_logout)).perform(click())
    }
}
