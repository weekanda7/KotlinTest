package com.example.kotlintest

import androidx.test.espresso.IdlingRegistry
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Registers [EspressoIdlingResource] before the test runs so Espresso blocks
 * onView() calls until DeviceRepository's simulated network delay finishes,
 * instead of racing the background thread.
 */
class IdlingResourceRule : TestWatcher() {

    override fun starting(description: Description) {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    override fun finished(description: Description) {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }
}
