package com.example.kotlintest.screens

import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.kotlintest.LoginActivity
import com.example.kotlintest.R
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.check.KCheckBox
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.text.KButton

object LoginScreen : KScreen<LoginScreen>() {
    override val layoutId: Int? = R.layout.activity_login
    override val viewClass: Class<*>? = LoginActivity::class.java
    val enterEmail = KEditText { withId(R.id.edit_username) }
    val enterPassword = KEditText { withId(R.id.edit_password) }
    val toggleRememberMe = KCheckBox { withId(R.id.checkbox_remember_me) }
    val tapLogin = KButton { withId(R.id.button_login) }

    /** Debug-only bypass button - only present in debug builds (see BuildConfig.DEBUG gate in LoginActivity). */
    val tapSkip = KButton { withId(R.id.button_skip) }

//    fun login(email: String, password: String, rememberMe: Boolean = false) {
//        enterEmail(email)
//        enterPassword(password)
//        if (rememberMe) toggleRememberMe()
//        tapLogin()
//    }
//
//    fun assertFieldError(@StringRes errorRes: Int) {
//        onView(withText(errorRes)).check(matches(isDisplayed()))
//    }
}
