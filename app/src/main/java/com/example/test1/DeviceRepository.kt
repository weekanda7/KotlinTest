package com.example.test1

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

/**
 * Stands in for a real network call: work happens on a background thread with an
 * artificial delay, and the result is posted back to the main thread - the same
 * shape as a Retrofit callback, which Espresso cannot see without an IdlingResource.
 */
object DeviceRepository {

    private const val SIMULATED_NETWORK_DELAY_MS = 1200L

    private val backgroundExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun loadDevices(onResult: (List<Device>) -> Unit) {
        EspressoIdlingResource.increment()
        backgroundExecutor.execute {
            Thread.sleep(SIMULATED_NETWORK_DELAY_MS)
            mainHandler.post {
                onResult(DeviceCatalog.all)
                EspressoIdlingResource.decrement()
            }
        }
    }
}
