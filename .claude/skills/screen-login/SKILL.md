---
name: screen-login
description: Reference for the Login screen (LoginActivity / activity_login.xml) - entry point of the app, credential validation, and the debug skip button. Use when modifying login fields, validation rules, or the app's launch flow.
---

# Login Screen

**Files**: `LoginActivity.kt`, `res/layout/activity_login.xml`, `LoginActivityTest.kt`

## Purpose

This is the app's launcher screen (`android:name=".LoginActivity"` is the only activity with the `MAIN`/`LAUNCHER` intent filter in `AndroidManifest.xml`). It collects an email + password and, on success, hands off to `HomeActivity`.

## Key views

| ID | ViewBinding | Type | Notes |
|---|---|---|---|
| `image_logo` | `imageLogo` | `ImageView` | Static app icon |
| `input_layout_username` / `edit_username` | `inputLayoutUsername` / `editUsername` | `TextInputLayout` / `TextInputEditText` | Email field, `inputType="textEmailAddress"` |
| `input_layout_password` / `edit_password` | `inputLayoutPassword` / `editPassword` | `TextInputLayout` / `TextInputEditText` | Password field with visibility toggle (`app:endIconMode="password_toggle"`) |
| `checkbox_remember_me` | `checkboxRememberMe` | `CheckBox` | Passed through as `EXTRA_REMEMBER_ME` |
| `button_login` | `buttonLogin` | `MaterialButton` | Triggers `attemptLogin()` |
| `button_skip` | `buttonSkip` | `MaterialButton` (text style) | Debug-only bypass, see below |

## Behavior

`attemptLogin()` (`LoginActivity.kt`):
- Username must be non-blank and match `Patterns.EMAIL_ADDRESS`.
- Password must be non-blank and at least 6 characters.
- Errors are shown via `TextInputLayout.error` using string resources `error_username_required`, `error_username_invalid`, `error_password_required`, `error_password_too_short`.
- On success: starts `HomeActivity` with `EXTRA_USERNAME` (the typed email) and `EXTRA_REMEMBER_ME` (checkbox state), then calls `finish()` — Login is removed from the back stack, so `HomeActivity` becomes the stack root.

`button_skip` (debug bypass):
- Visibility is set to `View.VISIBLE`/`View.GONE` based on `BuildConfig.DEBUG` in `onCreate()` — it never renders in release builds.
- Click calls `skipLogin()`, which skips all validation and starts `HomeActivity` with a hardcoded `"debug@example.com"` username and `rememberMe = false`.
- Requires `buildFeatures.buildConfig = true` in `app/build.gradle.kts` (added specifically to support `BuildConfig.DEBUG`).

## Window insets

Root view `main` calls `applySystemBarInsetsPadding()` (`WindowInsetsExtensions.kt`), which adds the `systemBars()` insets on top of the layout's 24dp padding — required because `targetSdk 37` forces edge-to-edge by default. The template listener it replaced wiped that 24dp padding out (see [[edge-to-edge-insets]], Bug 3).

## Navigation

Login → `HomeActivity` (`EXTRA_USERNAME`, `EXTRA_REMEMBER_ME`), always via `finish()` — there is no way back to Login from Home except relaunching the app.
