package com.example.kotlintest

import android.app.Application

/**
 * Owns the [AppContainer] for the process. `open` so the instrumented test runner can
 * boot a subclass whose container is full of fakes (androidTest `TestApp`) - the same
 * mechanism Hilt's `HiltTestApplication` uses, minus the framework.
 */
open class KotlinTestApp : Application() {

    open val appContainer: AppContainer by lazy { DefaultAppContainer() }
}
