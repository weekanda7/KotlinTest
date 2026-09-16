package com.example.kotlintest

import android.Manifest
import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsFragmentTest {

    // Pre-grants the runtime permission so the test exercises the "already granted"
    // fast path deterministically, instead of dealing with the system permission
    // dialog (which lives outside the app process and needs UiAutomator).
    @get:Rule(order = 0)
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    @get:Rule(order = 1)
    val resetDeviceCatalogRule = ResetDeviceCatalogRule()

    // DeviceListFragment is created (just hidden) as soon as DeviceListActivity starts,
    // so its simulated network load still runs even when landing straight on the
    // Settings tab - the idling resource is needed here too.
    @get:Rule(order = 2)
    val idlingResourceRule = IdlingResourceRule()

    @get:Rule(order = 3)
    val activityRule = ActivityScenarioRule(DeviceListActivity::class.java)

    @Before
    fun openSettingsTab() {
        onView(withId(R.id.navigation_settings)).perform(click())
    }

    @After
    fun resetPreferences() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        SettingsPreferences.setAutoRefreshEnabled(context, false)
        SettingsPreferences.setNotificationsEnabled(context, false)
    }

    @Test
    fun togglingAutoRefresh_persistsAcrossRelaunch() {
        onView(withId(R.id.switch_auto_refresh)).perform(click())

        activityRule.scenario.close()
        ActivityScenario.launch(DeviceListActivity::class.java).use {
            onView(withId(R.id.navigation_settings)).perform(click())
            onView(withId(R.id.switch_auto_refresh)).check(matches(isChecked()))
        }
    }

    @Test
    fun togglingNotifications_withPermissionGranted_staysChecked() {
        onView(withId(R.id.switch_notifications)).perform(click())

        onView(withId(R.id.switch_notifications)).check(matches(isChecked()))
    }
}
