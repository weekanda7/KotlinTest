package com.example.kotlintest

import kotlin.random.Random

/**
 * Small helper for test input that shouldn't be the exact same literal on every
 * run (e.g. a newly-created entity's name) - not for anything that needs to be
 * collision-resistant across parallel runs, just cosmetic variety.
 */
object RandomTestData {

    fun deviceName(prefix: String = "testdevice"): String = "$prefix-${Random.nextInt(1000, 9999)}"
}
