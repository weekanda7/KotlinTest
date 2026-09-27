package com.example.kotlintest.cases.login

import LoginScenario
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.kotlintest.LoginActivity
import com.example.kotlintest.R
import com.example.kotlintest.TestArguments
import com.example.kotlintest.pages.HomePage
import com.example.kotlintest.pages.LoginPage
import com.example.kotlintest.screens.LoginScreen
import com.kaspersky.components.kautomator.system.UiSystem.click
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginKaspressoTest : TestCase() {

    private val testEmail = TestArguments.require("testLoginEmail")
    private val testPassword = TestArguments.require("testLoginPassword")

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun login_withEmptyFields_showsRequiredErrors() = run {
        step("不輸入帳密直接按登入") {
            LoginScreen {
                tapLogin { click() }
            }
        }
        step("兩個欄位都顯示必填錯誤") {
            LoginScreen {
                KTextView { withText(R.string.error_username_required) }.isDisplayed()
                KTextView { withText(R.string.error_password_required) }.isDisplayed()
            }
        }
    }

    @Test
    fun login_withValidCredentials_navigatesToHomeWithUsername() = run {
        step("login") {
            scenario(LoginScenario(testEmail, testPassword))
        }
        step("show home") {
            LoginScreen {
                KTextView { withId(R.id.text_welcome) }.hasText("Welcome, $testEmail!")
            }
        }
    }
}
