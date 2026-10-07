package com.example.kotlintest

import android.os.Handler
import android.os.Looper
import androidx.test.espresso.idling.CountingIdlingResource
import java.util.concurrent.Executors

/**
 * Test double for [DeviceRepository] that keeps the production shape - work on a
 * background thread, result posted back to the main looper - instead of the
 * main-looper-only [FakeDeviceRepository].
 *
 * Espresso cannot see the background thread, so every in-flight load is counted on
 * [idlingResource]; [BackgroundThreadRule] swaps this repository in and registers that
 * resource for the tests that opt in.
 */
class BackgroundDeviceRepository(
    private val source: () -> List<Device> = { DeviceCatalog.all },
    private val delayMs: Long = DEFAULT_DELAY_MS
) : DeviceRepository {

    val idlingResource = CountingIdlingResource("DeviceLoad")

    private val backgroundExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun loadDevices(onResult: (List<Device>) -> Unit) {
        idlingResource.increment()
        backgroundExecutor.execute {
            Thread.sleep(delayMs)
            mainHandler.post {
                onResult(source())
                idlingResource.decrement()
            }
        }
    }

    companion object {
        const val DEFAULT_DELAY_MS = 2000L
    }
}
