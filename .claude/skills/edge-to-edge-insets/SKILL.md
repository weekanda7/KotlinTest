---
name: edge-to-edge-insets
description: Why toolbars/back arrows can render under the status bar and stop receiving taps on this project, and the two-part fix (window insets padding + a real navigationIcon drawable). Use when a screen's top content overlaps the status bar/cutout, or a toolbar navigation icon is invisible or unresponsive.
---

# Edge-to-Edge Insets & Toolbar Navigation Icon

This project targets `targetSdk = 37` (`app/build.gradle.kts`), which forces edge-to-edge by default — screen content draws behind the status bar and navigation bar unless an activity explicitly pads for it. This bit two independent things at once, on three screens.

## Bug 1: missing insets padding → toolbar overlaps the status bar

`LoginActivity` and `HomeActivity` were written with a `ViewCompat.setOnApplyWindowInsetsListener` on their root view:

```kotlin
ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
    insets
}
```

`DeviceDetailActivity`, `DeviceListActivity`, and `AddDeviceActivity` originally did **not** have this. Their toolbars sat at the very top of the window, under the status bar/camera cutout. Visually the back arrow appeared squeezed next to the status bar icons; taps in that area landed on system UI, not the app, so the back arrow looked broken ("visible but unresponsive"). All three now have the same insets listener applied to their root view (`binding.deviceDetail`, `binding.deviceList`, `binding.addDevice` respectively).

**When to reapply this pattern**: any *new* Activity added to this app needs the same insets listener on its root view in `onCreate()`, right after `setContentView()` — it is not automatic just because other screens have it.

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

## Affected screens

[[screen-device-detail]], [[screen-device-list-shell]], [[screen-add-device]] — all three needed both fixes. [[screen-login]] and [[screen-home]] only ever needed Bug 1's fix (they have no toolbar/back arrow).
