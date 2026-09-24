package com.example.kotlintest

import androidx.test.espresso.IdlingRegistry
import org.junit.rules.TestWatcher
import org.junit.runner.Description
/**
 * DeviceCatalog is a process-wide singleton mutated by add/delete flows, and
 * instrumented tests normally share one app process across the whole run (no
 * AndroidTestOrchestrator here). Runs before any ActivityScenarioRule (use order = 0)
 * so a device deleted by a previous test doesn't break a later test that expects it.
 */
class BackgroundThreadRule : TestWatcher() {

    override fun starting(description: Description) {
        IdlingRegistry.getInstance().register(deviceLoad)
    }
    override fun finished(description: Description) {
        IdlingRegistry.getInstance().unregister(deviceLoad)
    }
}
