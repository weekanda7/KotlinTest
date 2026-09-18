package com.example.kotlintest.cases.device

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.kotlintest.DeviceCatalog
import com.example.kotlintest.DeviceListActivity
import com.example.kotlintest.R
import com.example.kotlintest.ResetDeviceCatalogRule
import com.example.kotlintest.pages.SettingsPage
import com.example.kotlintest.pages.common.DeviceRowActions
import com.example.kotlintest.pages.device.DeviceDetailPage
import com.example.kotlintest.pages.device.DeviceListPage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceListActivityTest {

    @get:Rule(order = 0)
    val resetDeviceCatalogRule = ResetDeviceCatalogRule()

    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(DeviceListActivity::class.java)

    @Test
    fun deviceList_hidesProgressBarOnceLoaded() {
        // FakeDeviceRepository (wired in by TestApp) posts its result to the main looper,
        // and Espresso drains that queue before every onView() - so the load callback has
        // already fired by the time this assertion runs. No IdlingResource, no sleep.
        DeviceListPage.assertProgressBarHidden()
        DeviceListPage.assertListVisible()
    }

    @Test
    fun deviceList_showsAllMockDevices() {
        DeviceCatalog.all.forEach { device ->
            DeviceRowActions.scrollToRow(device.name)
            DeviceRowActions.assertRowVisible(device.name)
        }
    }

    @Test
    fun tappingDevice_navigatesToDetailWithMatchingName() {
        val target = DeviceCatalog.all.first()

        DeviceRowActions.clickRowNamed(target.name)

        DeviceDetailPage.assertName(target.name)
        DeviceDetailPage.assertIpAddress(target.ipAddress)
    }

    @Test
    fun tappingSettingsTab_opensSettingsScreen() {
        DeviceListPage.openSettingsTab()

        DeviceListPage.assertToolbarTitle(R.string.title_settings)
    }

    @Test
    fun switchingTabs_togglesFabAndContentVisibility() {
        DeviceListPage.assertFabVisible()

        DeviceListPage.openSettingsTab()
        DeviceListPage.assertToolbarTitle(R.string.title_settings)
        SettingsPage.assertScreenDisplayed()
        DeviceListPage.assertFabHidden()

        DeviceListPage.openDevicesTab()
        DeviceListPage.assertToolbarTitle(R.string.title_devices)
        DeviceListPage.assertListVisible()
        DeviceListPage.assertFabVisible()
    }

    @Test
    fun switchingTabsAndBack_preservesSearchQuery() {
        DeviceListPage.search("router")
        DeviceRowActions.assertRowVisible("router-main")

        // show()/hide() keeps the fragment (and its query text) alive instead of
        // recreating it, unlike a plain FragmentTransaction.replace() would.
        DeviceListPage.openSettingsTab()
        DeviceListPage.openDevicesTab()

        DeviceRowActions.assertRowVisible("router-main")
        DeviceRowActions.assertRowAbsent("server-01")
    }
}
