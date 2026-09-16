package com.example.kotlintest

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun login_withEmptyFields_showsRequiredErrors() {
        onView(withId(R.id.button_login)).perform(click())

        onView(withText(R.string.error_username_required)).check(matches(isDisplayed()))
        onView(withText(R.string.error_password_required)).check(matches(isDisplayed()))
    }

    @Test
    fun login_withInvalidEmail_showsEmailError() {
        onView(withId(R.id.edit_username)).perform(typeText("not-an-email"), closeSoftKeyboard())
        onView(withId(R.id.edit_password)).perform(typeText("password123"), closeSoftKeyboard())
        onView(withId(R.id.button_login)).perform(click())

        onView(withText(R.string.error_username_invalid)).check(matches(isDisplayed()))
    }

    @Test
    fun login_withShortPassword_showsPasswordError() {
        onView(withId(R.id.edit_username)).perform(typeText("henry@example.com"), closeSoftKeyboard())
        onView(withId(R.id.edit_password)).perform(typeText("123"), closeSoftKeyboard())
        onView(withId(R.id.button_login)).perform(click())

        onView(withText(R.string.error_password_too_short)).check(matches(isDisplayed()))
    }

    @Test
    fun login_withValidCredentials_navigatesToHomeWithUsername() {
        onView(withId(R.id.edit_username)).perform(typeText("henry@example.com"), closeSoftKeyboard())
        onView(withId(R.id.edit_password)).perform(typeText("password123"), closeSoftKeyboard())
        onView(withId(R.id.button_login)).perform(click())

        onView(withId(R.id.text_welcome)).check(
            matches(withText("Welcome, henry@example.com!"))
        )
    }

    @Test
    fun login_withRememberMeChecked_showsRememberedMessageOnHome() {
        onView(withId(R.id.edit_username)).perform(typeText("henry@example.com"), closeSoftKeyboard())
        onView(withId(R.id.edit_password)).perform(typeText("password123"), closeSoftKeyboard())
        onView(withId(R.id.checkbox_remember_me)).perform(click())
        onView(withId(R.id.button_login)).perform(click())

        onView(withId(R.id.text_remembered)).check(matches(isDisplayed()))
    }
}
