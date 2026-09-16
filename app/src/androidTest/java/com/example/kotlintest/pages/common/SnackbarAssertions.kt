package com.example.kotlintest.pages.common

import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText

/**
 * Espresso has no dedicated Snackbar matcher - a Snackbar's message is just a TextView
 * on screen, so this is a plain withText(...) check. Kept as its own object (rather than
 * inlined per test) so every screen asserts a toast/Snackbar message the same way.
 */
object SnackbarAssertions {

    fun assertShown(@StringRes messageRes: Int) {
        onView(withText(messageRes)).check(matches(isDisplayed()))
    }
}
