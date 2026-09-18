package com.example.kotlintest

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

/**
 * Registered as `testInstrumentationRunner` in app/build.gradle.kts. Its single job is to
 * boot the app process with [TestApp] instead of [KotlinTestApp], which is how the fakes
 * in [TestAppContainer] get in front of every Activity before it launches - the same
 * trick Hilt's `CustomTestRunner` + `HiltTestApplication` perform.
 */
class InstrumentedTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application =
        super.newApplication(cl, TestApp::class.java.name, context)
}
