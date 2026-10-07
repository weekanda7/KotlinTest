package com.example.kotlintest.cases.device

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.kotlintest.BackgroundThreadRule
import com.example.kotlintest.DeviceCatalog
import com.example.kotlintest.DeviceListActivity
import com.example.kotlintest.ResetDeviceCatalogRule
import com.example.kotlintest.pages.common.DeviceRowActions
import com.example.kotlintest.pages.device.DeviceListPage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceListBackgroundLoadTest {

    @get:Rule(order = 0)
    val resetDeviceCatalogRule = ResetDeviceCatalogRule()

    @get:Rule(order = 1)
    val backgroundThreadRule = BackgroundThreadRule()

    @get:Rule(order = 2)
    val activityRule = ActivityScenarioRule(DeviceListActivity::class.java)

    // The load happens on a background thread with a delay; Espresso only waits for it
    // because BackgroundThreadRule registers the repository's IdlingResource.

    @Test
    fun backgroundLoad_hidesProgressBarOnceLoaded() {
        DeviceListPage.assertProgressBarHidden()
        DeviceListPage.assertListVisible()
    }

    @Test
    fun backgroundLoad_showsAllMockDevices() {
        DeviceCatalog.all.forEach { device ->
            DeviceRowActions.scrollToRow(device.name)
            DeviceRowActions.assertRowVisible(device.name)
        }
    }
}
