package com.example.kotlintest

import android.os.Handler
import android.os.Looper

/**
 * Test double for [DeviceRepository]: hands back the current [DeviceCatalog] contents on
 * the next main-looper pass - no background thread, no artificial delay.
 *
 * Espresso already synchronises with the main looper's message queue before every
 * onView(), so a callback posted here is guaranteed to have run by the time a test
 * asserts on the list. That is what makes the old CountingIdlingResource unnecessary:
 * the only thing Espresso could not see was the background thread, and this fake never
 * uses one. Posting (rather than invoking the callback inline) keeps the production
 * contract - "the result arrives asynchronously on the main thread" - intact, so the
 * loading-state code path in DeviceListFragment is still exercised.
 */
class FakeDeviceRepository(
    private val source: () -> List<Device> = { DeviceCatalog.all },
) : DeviceRepository {

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun loadDevices(onResult: (List<Device>) -> Unit) {
        mainHandler.post { onResult(source()) }
    }
}
