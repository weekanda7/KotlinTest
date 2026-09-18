package com.example.kotlintest

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Pads this view for the system bars *on top of* whatever padding the layout XML already
 * declares, so edge-to-edge (enforced from targetSdk 35; this app targets 37) does not push
 * content under the status bar or navigation bar.
 *
 * The template code this replaces, `v.setPadding(bars.left, bars.top, bars.right, bars.bottom)`,
 * overwrote the XML padding outright - that is how activity_login / activity_home silently
 * lost their 24dp side margins. Capturing the initial padding once, outside the listener,
 * also keeps repeated inset passes (keyboard, rotation) from compounding.
 * See .claude/skills/edge-to-edge-insets.
 */
fun View.applySystemBarInsetsPadding() {
    val initialLeft = paddingLeft
    val initialTop = paddingTop
    val initialRight = paddingRight
    val initialBottom = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(
            left = initialLeft + systemBars.left,
            top = initialTop + systemBars.top,
            right = initialRight + systemBars.right,
            bottom = initialBottom + systemBars.bottom,
        )
        insets
    }
}
