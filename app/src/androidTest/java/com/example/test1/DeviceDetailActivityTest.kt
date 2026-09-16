package com.example.test1

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
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

        onView(withId(R.id.text_name)).check(matches(withText(device.name)))
        onView(withId(R.id.text_status)).check(matches(withText(R.string.text_status_offline)))
        onView(withId(R.id.text_ip_address)).check(matches(withText(device.ipAddress)))
    }
}
