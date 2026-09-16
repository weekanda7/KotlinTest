package com.example.kotlintest.cases.device

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.kotlintest.DeviceCatalog
import com.example.kotlintest.DeviceDetailActivity
import com.example.kotlintest.R
import com.example.kotlintest.ResetDeviceCatalogRule
import com.example.kotlintest.pages.device.DeviceDetailPage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceDetailActivityTest {

    private fun intentFor(deviceId: String): Intent =
        Intent(ApplicationProvider.getApplicationContext(), DeviceDetailActivity::class.java).apply {
            putExtra(DeviceDetailActivity.EXTRA_DEVICE_ID, deviceId)
        }

    @get:Rule(order = 0)
    val resetDeviceCatalogRule = ResetDeviceCatalogRule()

    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule<DeviceDetailActivity>(intentFor("4"))

    @Test
    fun offlineDevice_showsNameStatusAndIp() {
        val device = DeviceCatalog.findById("4")!!

        DeviceDetailPage.assertName(device.name)
        DeviceDetailPage.assertStatus(R.string.text_status_offline)
        DeviceDetailPage.assertIpAddress(device.ipAddress)
    }
}
