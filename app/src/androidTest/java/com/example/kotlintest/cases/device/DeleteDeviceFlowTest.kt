package com.example.kotlintest.cases.device

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.kotlintest.DeviceListActivity
import com.example.kotlintest.IdlingResourceRule
import com.example.kotlintest.R
import com.example.kotlintest.ResetDeviceCatalogRule
import com.example.kotlintest.pages.common.DeviceRowActions
import com.example.kotlintest.pages.common.SnackbarAssertions
import com.example.kotlintest.pages.device.DeviceDetailPage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeleteDeviceFlowTest {

    @get:Rule(order = 0)
    val resetDeviceCatalogRule = ResetDeviceCatalogRule()

    @get:Rule(order = 1)
    val idlingResourceRule = IdlingResourceRule()

    @get:Rule(order = 2)
    val activityRule = ActivityScenarioRule(DeviceListActivity::class.java)

    @Test
    fun deletingDevice_removesFromListWithConfirmation() {
        DeviceRowActions.clickRowNamed("nas-backup")

        DeviceDetailPage.tapDelete()
        DeviceDetailPage.confirmDelete()

        SnackbarAssertions.assertShown(R.string.text_device_deleted)
        DeviceRowActions.assertRowAbsent("nas-backup")
    }

    @Test
    fun cancellingDeleteDialog_keepsDeviceInList() {
        DeviceRowActions.clickRowNamed("nas-backup")

        DeviceDetailPage.tapDelete()
        DeviceDetailPage.cancelDelete()

        DeviceDetailPage.assertName("nas-backup")
    }
}
