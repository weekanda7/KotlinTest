package com.example.kotlintest

import androidx.test.platform.app.InstrumentationRegistry

/**
 * Reads values from the instrumentation arguments (`am instrument -e key value`), which
 * app/build.gradle.kts populates via testInstrumentationRunnerArguments. Fails loudly when
 * a key is missing so a misconfigured run doesn't quietly test with an empty string.
 */
object TestArguments {

    fun require(key: String): String =
        requireNotNull(InstrumentationRegistry.getArguments().getString(key)) {
            "Missing instrumentation argument '$key'. It is set in app/build.gradle.kts " +
                "(testInstrumentationRunnerArguments) - run the tests through Gradle, and see " +
                "secrets.defaults.properties for the expected keys."
        }
}
