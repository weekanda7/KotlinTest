package com.example.kotlintest

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.IdlingRegistry
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Runs the test against [BackgroundDeviceRepository] instead of the default
 * [FakeDeviceRepository], and registers its IdlingResource so Espresso waits for the
 * background load. Must run before any ActivityScenarioRule (lower order) so the
 * fragment picks up the swapped repository; the default is restored afterwards.
 */
class BackgroundThreadRule : TestWatcher() {

    private val container get() = ApplicationProvider.getApplicationContext<TestApp>().appContainer
    private val repository = BackgroundDeviceRepository()
    private lateinit var previous: DeviceRepository

    override fun starting(description: Description) {
        previous = container.deviceRepository
        container.deviceRepository = repository
        IdlingRegistry.getInstance().register(repository.idlingResource)
    }

    override fun finished(description: Description) {
        IdlingRegistry.getInstance().unregister(repository.idlingResource)
        container.deviceRepository = previous
    }
}
