package com.example.test1

import androidx.test.espresso.idling.CountingIdlingResource

/**
 * Only ever touched from production code (increment/decrement around async work);
 * androidTest registers [countingIdlingResource] with IdlingRegistry so Espresso
 * waits for pending background work before interacting with views.
 */
object EspressoIdlingResource {

    private const val RESOURCE = "GLOBAL"

    val countingIdlingResource = CountingIdlingResource(RESOURCE)

    fun increment() {
        countingIdlingResource.increment()
    }

    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }
}
