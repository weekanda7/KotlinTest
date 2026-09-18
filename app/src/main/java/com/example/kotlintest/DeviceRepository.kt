package com.example.kotlintest

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

/**
 * Async source of the device list - the seam that stands in for a remote API.
 *
 * Production wires [SimulatedNetworkDeviceRepository]; instrumented tests wire a fake
 * (androidTest `FakeDeviceRepository`) through [AppContainer], so this interface is the
 * only thing UI code ever sees. Whatever implements it must deliver [onResult] on the
 * main thread.
 */
fun interface DeviceRepository {
    fun loadDevices(onResult: (List<Device>) -> Unit)
}

/**
 * Stands in for a real network call: work happens on a background thread with an
 * artificial delay, and the result is posted back to the main thread - the same shape
 * as a Retrofit callback. Espresso cannot observe the background thread, which is
 * exactly why tests swap this class out instead of trying to wait for it.
 */
class SimulatedNetworkDeviceRepository(
    private val source: () -> List<Device> = { DeviceCatalog.all },
    private val delayMs: Long = DEFAULT_NETWORK_DELAY_MS,
) : DeviceRepository {

    private val backgroundExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun loadDevices(onResult: (List<Device>) -> Unit) {
        backgroundExecutor.execute {
            Thread.sleep(delayMs)
            mainHandler.post { onResult(source()) }
        }
    }

    companion object {
        const val DEFAULT_NETWORK_DELAY_MS = 1200L
    }
}
