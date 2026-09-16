package com.example.kotlintest.cases.setting

import android.Manifest
import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.example.kotlintest.DeviceListActivity
import com.example.kotlintest.IdlingResourceRule
import com.example.kotlintest.ResetDeviceCatalogRule
import com.example.kotlintest.SettingsPreferences
import com.example.kotlintest.pages.SettingsPage
import com.example.kotlintest.pages.device.DeviceListPage
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
        DeviceListPage.openSettingsTab()
    }

    @After
    fun resetPreferences() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        SettingsPreferences.setAutoRefreshEnabled(context, false)
        SettingsPreferences.setNotificationsEnabled(context, false)
    }

    @Test
    fun togglingAutoRefresh_persistsAcrossRelaunch() {
        SettingsPage.toggleAutoRefresh()

        activityRule.scenario.close()
        ActivityScenario.launch(DeviceListActivity::class.java).use {
            DeviceListPage.openSettingsTab()
            SettingsPage.assertAutoRefreshChecked()
        }
    }

    @Test
    fun togglingNotifications_withPermissionGranted_staysChecked() {
        SettingsPage.toggleNotifications()

        SettingsPage.assertNotificationsChecked()
    }
}
