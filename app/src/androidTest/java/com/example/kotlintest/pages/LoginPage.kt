package com.example.kotlintest.pages

import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.kotlintest.R

object LoginPage {

    fun enterEmail(email: String) {
        onView(withId(R.id.edit_username)).perform(typeText(email), closeSoftKeyboard())
    }

    fun enterPassword(password: String) {
        onView(withId(R.id.edit_password)).perform(typeText(password), closeSoftKeyboard())
    }

    fun toggleRememberMe() {
        onView(withId(R.id.checkbox_remember_me)).perform(click())
    }

    fun tapLogin() {
        onView(withId(R.id.button_login)).perform(click())
    }

    /** Debug-only bypass button - only present in debug builds (see BuildConfig.DEBUG gate in LoginActivity). */
    fun tapSkip() {
        onView(withId(R.id.button_skip)).perform(click())
    }

    fun login(email: String, password: String, rememberMe: Boolean = false) {
        enterEmail(email)
        enterPassword(password)
        if (rememberMe) toggleRememberMe()
        tapLogin()
    }

    fun assertFieldError(@StringRes errorRes: Int) {
        onView(withText(errorRes)).check(matches(isDisplayed()))
    }
}
