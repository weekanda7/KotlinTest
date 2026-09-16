package com.example.kotlintest.cases.device

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.kotlintest.DeviceCatalog
import com.example.kotlintest.DeviceListActivity
import com.example.kotlintest.IdlingResourceRule
import com.example.kotlintest.ResetDeviceCatalogRule
import com.example.kotlintest.pages.common.DeviceRowActions
import com.example.kotlintest.pages.device.DeviceListPage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceSearchTest {

    @get:Rule(order = 0)
    val resetDeviceCatalogRule = ResetDeviceCatalogRule()

    @get:Rule(order = 1)
    val idlingResourceRule = IdlingResourceRule()

    @get:Rule(order = 2)
    val activityRule = ActivityScenarioRule(DeviceListActivity::class.java)

    // search_devices is always expanded (android:iconifiedByDefault="false"), so its query
    // field is present as soon as the idling resource confirms the initial load finished -
    // no click-to-expand step needed.
    //
    // DeviceListPage.search() uses replaceText() to set the query field's content directly
    // instead of simulating keystrokes through the IME - typeText() here was observed to
    // randomly drop characters (e.g. "router" -> "r") due to the emulator keyboard's
    // composing/commit timing, which produced flaky, timing-dependent filter results.

    @Test
    fun searchingByName_filtersToMatchingDevices() {
        DeviceListPage.search("router")

        DeviceRowActions.assertRowVisible("router-main")
        DeviceRowActions.assertRowAbsent("server-01")
    }

    @Test
    fun searchingWithNoMatches_showsEmptyState() {
        DeviceListPage.search("nonexistent-device")

        DeviceListPage.assertEmptyStateVisible()
    }

    @Test
    fun clearingSearch_restoresFullList() {
        DeviceListPage.search("router")
        DeviceListPage.clearSearch()

        DeviceCatalog.all.forEach { device ->
            DeviceRowActions.assertRowVisible(device.name)
        }
    }
}
