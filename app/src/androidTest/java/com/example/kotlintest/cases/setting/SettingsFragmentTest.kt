package com.example.kotlintest.cases.setting

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.example.kotlintest.DeviceListActivity
import com.example.kotlintest.ResetDeviceCatalogRule
import com.example.kotlintest.SettingsPreferences
import com.example.kotlintest.pages.SettingsPage
import com.example.kotlintest.pages.device.DeviceListPage
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsFragmentTest {

    // Pre-grants the runtime permission so the test exercises the "already granted"
    // fast path deterministically, instead of dealing with the system permission
    // dialog (which lives outside the app process and needs UiAutomator).
    //
    // POST_NOTIFICATIONS only exists as a runtime permission from API 33 (Tiramisu). On
    // anything older `pm grant` rejects it and GrantPermissionRule fails the test before
    // the body runs ("Failed to grant permissions, see logcat for details" - seen on the
    // API 30 ATD in CI). Below 33 SettingsFragment skips the permission check entirely,
    // so a no-op rule keeps both tests meaningful on every API level in the matrix.
    @get:Rule(order = 0)
    val permissionRule: TestRule =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            TestRule { base, _ -> base }
        }

    @get:Rule(order = 1)
    val resetDeviceCatalogRule = ResetDeviceCatalogRule()

    @get:Rule(order = 2)
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
