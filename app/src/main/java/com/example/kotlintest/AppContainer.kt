package com.example.kotlintest

import android.content.Context

/**
 * Hand-rolled dependency container (the "manual DI" pattern from the Android docs):
 * one object that decides which implementation of each dependency the app runs with.
 * UI code reaches it through [Context.appContainer] and only ever depends on the
 * interfaces exposed here.
 *
 * Instrumented tests replace the whole container (androidTest `TestApp`), so nothing in
 * this source set needs to know that fakes exist - no IdlingResource, no test-only flags.
 */
interface AppContainer {
    val deviceRepository: DeviceRepository
}

class DefaultAppContainer : AppContainer {

    override val deviceRepository: DeviceRepository by lazy { SimulatedNetworkDeviceRepository() }
}

val Context.appContainer: AppContainer
    get() = (applicationContext as KotlinTestApp).appContainer
