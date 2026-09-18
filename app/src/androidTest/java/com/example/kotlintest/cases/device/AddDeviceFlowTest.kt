package com.example.kotlintest.cases.device

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.kotlintest.DeviceCatalog
import com.example.kotlintest.DeviceListActivity
import com.example.kotlintest.R
import com.example.kotlintest.RandomTestData
import com.example.kotlintest.ResetDeviceCatalogRule
import com.example.kotlintest.pages.common.DeviceRowActions
import com.example.kotlintest.pages.common.SnackbarAssertions
import com.example.kotlintest.pages.device.AddDevicePage
import com.example.kotlintest.pages.device.DeviceListPage
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddDeviceFlowTest {

    @get:Rule(order = 0)
    val resetDeviceCatalogRule = ResetDeviceCatalogRule()

    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(DeviceListActivity::class.java)

    @Test
    fun addingDevice_appearsInListWithConfirmation() {
        val deviceName = RandomTestData.deviceName()

        DeviceListPage.tapAddDevice()

        AddDevicePage.enterName(deviceName)
        AddDevicePage.enterIpAddress("10.0.0.99")
        AddDevicePage.selectType("Router")
        AddDevicePage.setInstallDate(2026, 1, 15)
        AddDevicePage.tapSave()

        SnackbarAssertions.assertShown(R.string.text_device_added)

        // Confirms the save actually persisted the right name at the data layer,
        // independent of whether the RecyclerView has finished laying out the newly
        // added row yet.
        assertEquals(deviceName, DeviceCatalog.all.last().name)

        // Force the RecyclerView to lay out the just-inserted row before asserting on
        // it - notifyDataSetChanged() growing the item count can otherwise leave the
        // new position unbound at the moment Espresso's idle check passes.
        DeviceRowActions.scrollToPosition(DeviceCatalog.all.size - 1)
        DeviceRowActions.assertRowVisible(deviceName)
    }

    @Test
    fun savingWithoutRequiredFields_showsValidationErrors() {
        DeviceListPage.tapAddDevice()
        AddDevicePage.tapSave()

        AddDevicePage.assertFieldError(R.string.error_name_required)
        AddDevicePage.assertFieldError(R.string.error_ip_required)
    }
}
