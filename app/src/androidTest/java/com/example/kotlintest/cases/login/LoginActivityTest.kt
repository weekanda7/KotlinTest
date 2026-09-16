package com.example.kotlintest.cases.login

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.kotlintest.BuildConfig
import com.example.kotlintest.LoginActivity
import com.example.kotlintest.R
import com.example.kotlintest.pages.HomePage
import com.example.kotlintest.pages.LoginPage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    // Sourced from BuildConfig (secrets.properties/env var, see secretProperty()
    // in app/build.gradle.kts) instead of hardcoded here, so the test account can
    // be swapped without touching test code.
    private val testEmail = BuildConfig.TEST_LOGIN_EMAIL
    private val testPassword = BuildConfig.TEST_LOGIN_PASSWORD

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun login_withEmptyFields_showsRequiredErrors() {
        LoginPage.tapLogin()

        LoginPage.assertFieldError(R.string.error_username_required)
        LoginPage.assertFieldError(R.string.error_password_required)
    }

    @Test
    fun login_withInvalidEmail_showsEmailError() {
        LoginPage.login(email = "not-an-email", password = testPassword)

        LoginPage.assertFieldError(R.string.error_username_invalid)
    }

    @Test
    fun login_withShortPassword_showsPasswordError() {
        LoginPage.login(email = testEmail, password = "123")

        LoginPage.assertFieldError(R.string.error_password_too_short)
    }

    @Test
    fun login_withValidCredentials_navigatesToHomeWithUsername() {
        LoginPage.login(email = testEmail, password = testPassword)

        HomePage.assertWelcomeMessage(testEmail)
    }

    @Test
    fun login_withRememberMeChecked_showsRememberedMessageOnHome() {
        LoginPage.login(email = testEmail, password = testPassword, rememberMe = true)

        HomePage.assertRememberedMessageShown()
    }
}
