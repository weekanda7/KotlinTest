---
name: screen-device-list-shell
description: Reference for the Device List shell screen (DeviceListActivity / activity_device_list.xml) - toolbar + bottom navigation host that swaps between the Devices tab and Settings tab fragments. Use when modifying the bottom nav, toolbar title switching, or fragment show/hide logic. For the tab contents themselves see [[screen-device-list-tab]] and [[screen-settings-tab]].
---

# Device List Shell Screen

**Files**: `DeviceListActivity.kt`, `res/layout/activity_device_list.xml`, `res/menu/menu_bottom_nav.xml`, `DeviceListActivityTest.kt`

## Purpose

Host activity for the two-tab section of the app (Devices / Settings). It owns the toolbar and `BottomNavigationView`, and keeps two fragment instances alive simultaneously, toggling visibility rather than replacing them — this preserves each tab's scroll/search state when switching tabs.

## Entry

Started from `HomeActivity` (`button_view_devices`), no extras.

## Key views

| ID | ViewBinding | Type | Notes |
|---|---|---|---|
| `toolbar` | `toolbar` | `MaterialToolbar` | Title switches per tab; back arrow calls `finish()` |
| `fragment_container` | — (`R.id.fragment_container`) | `FragmentContainerView` | Hosts `DeviceListFragment` and `SettingsFragment` |
| `bottom_nav` | `bottomNav` | `BottomNavigationView` | Menu: `res/menu/menu_bottom_nav.xml` (`navigation_devices`, `navigation_settings`) |

## Behavior

- On first create (`savedInstanceState == null`): both fragments are added to `fragment_container` in one transaction — `SettingsFragment` added+`hide()`d, then `DeviceListFragment` added (visible). On re-creation, both are recovered from `supportFragmentManager` by tag (`tag_device_list`, `tag_settings`).
- `bottomNav.setOnItemSelectedListener`: `navigation_devices` shows the devices fragment and hides settings (title → `R.string.title_devices`); `navigation_settings` does the reverse (title → `R.string.title_settings`). Switching is `show()`/`hide()`, not fragment replacement, so `DeviceListFragment`'s in-memory list/search query and `SettingsFragment`'s switches are not reset when you tab away and back.
- Toolbar back arrow: `binding.toolbar.setNavigationOnClickListener { finish() }`.

## Window insets & back-arrow gotcha

Root view `device_list` calls `applySystemBarInsetsPadding()` for the `systemBars()` insets, and the toolbar's `app:navigationIcon` is `@drawable/ic_arrow_back` (a bundled vector, not `?attr/homeAsUpIndicator`). Both were bugs fixed in the same pass — see [[edge-to-edge-insets]] for why a missing insets listener makes the back arrow visually overlap the status bar and stop receiving taps, and why `?attr/homeAsUpIndicator` silently resolves to no icon under `Theme.Material3.DayNight.NoActionBar` (no `setSupportActionBar()` call anywhere in this app).

## Navigation

Device List Shell → `finish()` back to Home. Rows/FAB inside the Devices tab navigate onward to `DeviceDetailActivity` / `AddDeviceActivity` — see [[screen-device-list-tab]].
