---
name: test-architecture
description: How this project's androidTest suite is structured (cases/, pages/, shared Rules, secrets-backed fixture data, random test data) and the conventions to follow when adding, changing, or reorganizing Espresso instrumented tests.
---

# Espresso Test Architecture

Root: `app/src/androidTest/java/com/example/kotlintest/`

## 1. Directory layout

```
androidTest/java/com/example/kotlintest/
├── cases/                    # test classes (@Test), grouped by screen/feature
│   ├── ExampleInstrumentedTest.kt   # screen-agnostic smoke test - lives at cases/ root
│   ├── login/
│   │   └── LoginActivityTest.kt
│   ├── device/
│   │   ├── DeviceListActivityTest.kt
│   │   ├── DeviceDetailActivityTest.kt
│   │   ├── AddDeviceFlowTest.kt
│   │   ├── DeleteDeviceFlowTest.kt
│   │   └── DeviceSearchTest.kt
│   └── setting/
│       └── SettingsFragmentTest.kt
├── pages/                     # Page Object Model - all Espresso calls live here, not in cases/
│   ├── LoginPage.kt
│   ├── HomePage.kt
│   ├── SettingsPage.kt
│   ├── device/                  # feature-grouped, mirrors cases/device/
│   │   ├── DeviceListPage.kt       # DeviceListActivity shell (toolbar/bottom nav) + Devices tab
│   │   ├── DeviceDetailPage.kt
│   │   └── AddDevicePage.kt
│   └── common/                  # cross-screen helpers
│       ├── SnackbarAssertions.kt
│       └── DeviceRowActions.kt
├── IdlingResourceRule.kt      # shared JUnit4 TestRule
├── ResetDeviceCatalogRule.kt  # shared JUnit4 TestRule
└── RandomTestData.kt          # cosmetic randomness for test input (see section 5)
```

The three root-level files are cross-cutting test infrastructure, not tied to one screen. They have not been folded into a dedicated `utils/`/`database/` split yet - that split was discussed but only `cases/` and `pages/` were actually done. If that happens later, update this doc's layout diagram.

## 2. Page Object rules

- Every Page Object is a Kotlin `object` (stateless singleton) - Espresso is global/static (`onView(...)` operates on whatever's on screen), there is no per-instance driver session to thread through, unlike e.g. Selenium's `self`-passing style.
- A method wraps exactly one thing: an action (`enterEmail`, `tapLogin`) or an assertion (`assertFieldError`). Don't mix the two in one method.
- Screen boundaries mirror the `.claude/skills/screen-*` docs exactly - one Page Object per skill-doc screen (`screen-login` → `LoginPage`, `screen-device-list-shell` + `screen-device-list-tab` → `DeviceListPage`, `screen-settings-tab` → `SettingsPage`, etc). Adding a new screen means adding both a `screen-*` skill doc and a matching Page Object.
- Shared interactions used by 2+ screens go in `pages/common/` (e.g. `SnackbarAssertions` for toast/Snackbar messages, `DeviceRowActions` for RecyclerView row lookup/click/scroll) instead of being duplicated per Page Object.
- A feature with multiple screens gets its own subfolder under `pages/` (currently just `device/`), mirroring the equivalent `cases/<feature>/` folder.
- `cases/*Test.kt` files never call `onView(...)`/Espresso APIs directly - if a test needs a new interaction, add a method to the relevant Page Object first, then call it from the test.

## 3. Test class / Rule conventions

`@get:Rule(order = N)`, smallest first:

0. Permission rules (`GrantPermissionRule`), if the screen needs one
0/1. `ResetDeviceCatalogRule` - must run before `ActivityScenarioRule` so a previous test's leftover state can't leak into this one
1/2. `IdlingResourceRule` - only needed if the screen touches `DeviceRepository`/`DeviceCatalog` (anything that shows `DeviceListActivity`, directly or via a hosted fragment)
last. `ActivityScenarioRule` - launches the Activity under test, always last so earlier rules' setup is already in place

Test classes live under `cases/{login,device,setting}/` by screen/feature; a screen-agnostic test (no UI, like `ExampleInstrumentedTest`) goes at `cases/` root instead of forcing it into one of the three feature folders.

## 4. Tests skip login entirely

Tests never go through `LoginActivity` - `ActivityScenarioRule(TargetActivity::class.java)` launches the Activity under test directly (or with a custom `Intent`, e.g. `DeviceDetailActivityTest`'s `intentFor(deviceId)`), bypassing the whole navigation chain (Login → Home → DeviceList → ...).

This only works because the app has **no real auth guard** anywhere - no Activity checks a session/token before rendering. If real authentication is ever added, this breaks the moment a guard checks session state in `onCreate()`. The fix then is to seed valid session state before `ActivityScenarioRule` runs (a rule analogous to `ResetDeviceCatalogRule`, but for auth state) or swap in a fake auth dependency via DI - not to keep assuming direct-launch still works. See [[edge-to-edge-insets]] for another case of behavior that depends on exactly how these Activities are wired.

## 5. Test fixture data

- **Login credentials** (`LoginActivityTest`) come from `BuildConfig.TEST_LOGIN_EMAIL`/`TEST_LOGIN_PASSWORD`, never hardcoded strings - sourced via `secretProperty()` in `app/build.gradle.kts` (environment variable → `secrets.properties` → default). `secrets.defaults.properties` documents the key names and current default values. Any new test-only secret/fixture value follows the same pattern.
- **Newly-created entity names** (e.g. `AddDeviceFlowTest`'s device name) use `RandomTestData.deviceName()` for cosmetic variety across runs - this is not collision-avoidance (tests aren't run in parallel, and `ResetDeviceCatalogRule` gives every test a clean `DeviceCatalog`), purely to avoid the same literal every run. Don't randomize a lookup against **existing seeded data** (`server-01`, `router-main`, `nas-backup` etc. from `DeviceCatalog.defaultDevices()`) - only randomize values the test itself creates.

## 6. Adding a new test

1. New screen? Add a `.claude/skills/screen-*` doc first, then a matching Page Object under `pages/` (or `pages/<feature>/` if it belongs to an existing feature group).
2. Add the test class under `cases/<feature>/`, or `cases/` root if it's screen-agnostic.
3. Reuse `ResetDeviceCatalogRule`/`IdlingResourceRule` if the screen touches `DeviceCatalog`; don't invent a parallel reset mechanism.
4. Any new test-only secret/fixture value goes through `secretProperty()` in `app/build.gradle.kts`, documented in `secrets.defaults.properties` - never hardcoded in the test file.
5. **Update this skill doc** whenever the structure itself changes (new top-level folder, new Rule convention, new shared helper) - see [[android-id-naming]] section 7 for why skill docs need to stay in sync with the code they describe.
