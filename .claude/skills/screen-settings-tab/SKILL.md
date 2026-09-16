---
name: screen-settings-tab
description: Reference for the Settings tab content (SettingsFragment / fragment_settings.xml) - notification permission flow and preference persistence. Use when modifying settings switches or the POST_NOTIFICATIONS runtime permission handling. Hosted inside [[screen-device-list-shell]].
---

# Settings Tab (SettingsFragment)

**Files**: `SettingsFragment.kt`, `res/layout/fragment_settings.xml`, `SettingsPreferences.kt`, `SettingsFragmentTest.kt`

## Purpose

Second tab of `DeviceListActivity`. Two toggles backed by `SharedPreferences`.

## Key views

| ID | ViewBinding | Type | Notes |
|---|---|---|---|
| `switch_notifications` | `switchNotifications` | `MaterialSwitch` | Gated behind `POST_NOTIFICATIONS` runtime permission on API 33+ |
| `switch_auto_refresh` | `switchAutoRefresh` | `MaterialSwitch` | Plain preference toggle, no permission involved |

## Persistence

`SettingsPreferences` wraps a single `SharedPreferences` file (`"settings_prefs"`, `MODE_PRIVATE`) with two boolean keys, both defaulting to `false`: `notifications_enabled`, `auto_refresh_enabled`. `onViewCreated()` reads both into the switches before attaching listeners (so setting initial state doesn't re-trigger the listeners' side effects... note: listeners are attached *after* setting `isChecked`, so this is safe).

## Behavior

- `switch_auto_refresh` → directly calls `SettingsPreferences.setAutoRefreshEnabled()`. No permission, no side effects beyond persistence (nothing currently reads this flag to actually drive auto-refresh — it's a stored preference only).
- `switch_notifications` → `onNotificationsToggled(enabled)`:
  - Turning **off**: just persists `false`, no permission check.
  - Turning **on**: only requires a runtime permission check when `Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU` (API 33). If `POST_NOTIFICATIONS` isn't already granted, launches `requestNotificationPermission` (`ActivityResultContracts.RequestPermission()`); otherwise persists `true` immediately.
  - Permission callback: if granted, persists `true`. If denied, reverts the switch to unchecked (`binding.switchNotifications.isChecked = false`) and shows a `text_notification_permission_denied` Snackbar.
  - `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />` is declared in `AndroidManifest.xml`.

## Navigation

No outgoing navigation — this tab is a leaf screen. Switching away and back (via the bottom nav) does not reset switch state because `DeviceListActivity` uses `show()`/`hide()` rather than recreating the fragment.
