---
name: edge-to-edge-insets
description: Why toolbars/back arrows can render under the status bar and stop receiving taps on this project, why activity_login/activity_home lost their side margins, and the fixes (View.applySystemBarInsetsPadding() + a real navigationIcon drawable). Use when a screen's top content overlaps the status bar/cutout, a toolbar navigation icon is invisible or unresponsive, or a root view's XML padding seems to be ignored.
---

# Edge-to-Edge Insets & Toolbar Navigation Icon

This project targets `targetSdk = 37` (`app/build.gradle.kts`); edge-to-edge is enforced from targetSdk 35 — screen content draws behind the status bar and navigation bar unless an activity explicitly pads for it. This bit three independent things across the five screens.

## Bug 1: missing insets padding → toolbar overlaps the status bar

`LoginActivity` and `HomeActivity` were written with the Android Studio template's insets listener on their root view. `DeviceDetailActivity`, `DeviceListActivity`, and `AddDeviceActivity` originally did **not** have one. Their toolbars sat at the very top of the window, under the status bar/camera cutout. Visually the back arrow appeared squeezed next to the status bar icons; taps in that area landed on system UI, not the app, so the back arrow looked broken ("visible but unresponsive").

All five activities now share one helper, `View.applySystemBarInsetsPadding()` in `WindowInsetsExtensions.kt`, called on the root view right after `setContentView()`:

```kotlin
binding.deviceList.applySystemBarInsetsPadding()
```

**When to reapply this pattern**: any *new* Activity added to this app needs that one line in `onCreate()`, right after `setContentView()` — it is not automatic just because other screens have it.

## Bug 2: `?attr/homeAsUpIndicator` resolves to nothing

All toolbars originally used:

```xml
app:navigationIcon="?attr/homeAsUpIndicator"
```

`homeAsUpIndicator` is an ActionBar-system theme attribute that only gets a real value when `setSupportActionBar(toolbar)` + `supportActionBar.setDisplayHomeAsUpEnabled(true)` are called. This app's theme is `Theme.Material3.DayNight.NoActionBar` (`res/values/themes.xml`), and no activity calls `setSupportActionBar()` — the toolbars are plain `MaterialToolbar` widgets with a manual `setNavigationOnClickListener { finish() }`. So the attribute never resolved to a drawable: no icon was ever drawn, independent of the insets bug above.

**Fix**: a real drawable was added at `res/drawable/ic_arrow_back.xml` (a 24dp Material "arrow_back" vector, tinted with `?attr/colorControlNormal` so it adapts to light/dark). All toolbars now use:

```xml
app:navigationIcon="@drawable/ic_arrow_back"
app:navigationContentDescription="@string/content_description_back"
```

**When to reapply this pattern**: any new toolbar in this app should reference `@drawable/ic_arrow_back` directly — do not use `?attr/homeAsUpIndicator` unless the activity actually adopts `setSupportActionBar()`.

## Bug 3: the template listener wiped out the XML padding

The listener every activity used to carry (copied from the Android Studio template) was:

```kotlin
ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
    insets
}
```

`setPadding()` replaces all four values, so the `android:padding="24dp"` on `activity_login.xml` / `activity_home.xml` survived only until the first insets pass — in portrait the left/right system-bar insets are 0, and the login form stretched edge to edge. The other three screens have no XML padding on their root view, so they were unaffected, which is why it went unnoticed.

`View.applySystemBarInsetsPadding()` captures the view's initial padding *once*, before registering the listener, and applies `initial + insets` through `updatePadding()`. Capturing it outside the listener matters: reading `paddingLeft` inside the lambda would compound the insets on every pass (keyboard show/hide, rotation).

**When to reapply this pattern**: never write `setPadding(insets...)` on a root view again; call the helper. If a screen needs IME insets as well (a form whose Save button the keyboard can cover), extend the helper rather than registering a second listener on the same view — `setOnApplyWindowInsetsListener` replaces, it does not stack.

## Affected screens

[[screen-device-detail]], [[screen-device-list-shell]], [[screen-add-device]] — needed Bug 1 and Bug 2. [[screen-login]] and [[screen-home]] — Bug 3 (they had a listener; it was the wrong one). All five now share the helper.
