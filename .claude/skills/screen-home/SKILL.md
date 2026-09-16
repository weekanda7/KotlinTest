---
name: screen-home
description: Reference for the Home screen (HomeActivity / activity_home.xml) - post-login landing page and the hop-off point to device management. Use when modifying the welcome message, remember-me indicator, or navigation out of Home.
---

# Home Screen

**Files**: `HomeActivity.kt`, `res/layout/activity_home.xml`

## Purpose

Landing screen shown right after a successful login. It has no toolbar/back arrow — it is effectively the app's root screen once past Login (Login calls `finish()` after starting Home, so Home is alone at the bottom of the back stack).

## Entry

Started only from `LoginActivity` (both the real login and the debug skip button), via intent extras:
- `EXTRA_USERNAME` (`String`) — displayed username/email.
- `EXTRA_REMEMBER_ME` (`Boolean`) — controls the "remembered" indicator.

## Key views

| ID | ViewBinding | Type | Notes |
|---|---|---|---|
| `text_welcome` | `textWelcome` | `TextView` | Set from `R.string.text_welcome` (`"Welcome, %1$s!"`) with the username |
| `text_remembered` | `textRemembered` | `TextView` | `VISIBLE` only if `EXTRA_REMEMBER_ME` was true, otherwise `GONE` |
| `button_view_devices` | `buttonViewDevices` | `MaterialButton` | Navigates to `DeviceListActivity` |
| `button_logout` | `buttonLogout` | `MaterialButton` (borderless) | Calls `finish()` |

## Behavior

- `button_view_devices` → `startActivity(Intent(this, DeviceListActivity::class.java))` (no extras, no result expected).
- `button_logout` → `finish()`. Because Login already removed itself from the stack, this closes the app back to the launcher rather than returning to a visible Login screen — there is no re-navigation to `LoginActivity` from here.

## Window insets

Root view `home` has the standard `ViewCompat.setOnApplyWindowInsetsListener` padding for `systemBars()` (see [[edge-to-edge-insets]]).

## Navigation

Home → `DeviceListActivity` (view devices) or app exit (logout). Home does not currently reopen Login.
