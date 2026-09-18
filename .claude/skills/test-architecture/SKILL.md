---
name: test-architecture
description: How this project's androidTest suite is structured (cases/, pages/, shared Rules, the TestApp/AppContainer fake-injection setup, instrumentation-argument fixture data, random test data) and the conventions to follow when adding, changing, or reorganizing Espresso instrumented tests.
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
├── InstrumentedTestRunner.kt  # custom AndroidJUnitRunner: boots TestApp instead of KotlinTestApp
├── TestApp.kt                 # TestApp + TestAppContainer - the fakes every test runs against
├── FakeDeviceRepository.kt    # test double for DeviceRepository (main-looper post, no delay)
├── TestArguments.kt           # reads instrumentation arguments (login fixture) - see section 6
├── ResetDeviceCatalogRule.kt  # shared JUnit4 TestRule
└── RandomTestData.kt          # cosmetic randomness for test input (see section 6)
```

The root-level files are cross-cutting test infrastructure, not tied to one screen. They have not been folded into a dedicated `fakes/`/`utils/` split yet - that split was discussed but only `cases/` and `pages/` were actually done. If that happens later, update this doc's layout diagram.

## 2. Page Object rules

- Every Page Object is a Kotlin `object` (stateless singleton) - Espresso is global/static (`onView(...)` operates on whatever's on screen), there is no per-instance driver session to thread through, unlike e.g. Selenium's `self`-passing style.
- A method wraps exactly one thing: an action (`enterEmail`, `tapLogin`) or an assertion (`assertFieldError`). Don't mix the two in one method.
- Screen boundaries mirror the `.claude/skills/screen-*` docs exactly - one Page Object per skill-doc screen (`screen-login` → `LoginPage`, `screen-device-list-shell` + `screen-device-list-tab` → `DeviceListPage`, `screen-settings-tab` → `SettingsPage`, etc). Adding a new screen means adding both a `screen-*` skill doc and a matching Page Object.
- Shared interactions used by 2+ screens go in `pages/common/` (e.g. `SnackbarAssertions` for toast/Snackbar messages, `DeviceRowActions` for RecyclerView row lookup/click/scroll) instead of being duplicated per Page Object.
- A feature with multiple screens gets its own subfolder under `pages/` (currently just `device/`), mirroring the equivalent `cases/<feature>/` folder.
- `cases/*Test.kt` files never call `onView(...)`/Espresso APIs directly - if a test needs a new interaction, add a method to the relevant Page Object first, then call it from the test.

## 3. Dependency injection: how fakes get in front of the Activity

Production UI code never touches a concrete data source. `DeviceListFragment` asks `requireContext().appContainer.deviceRepository` for a `DeviceRepository` - an interface (`app/src/main/.../DeviceRepository.kt`). `KotlinTestApp` (the manifest's `android:name`) owns the `AppContainer`, and `DefaultAppContainer` wires `SimulatedNetworkDeviceRepository`: background executor + 1200ms delay, the Retrofit-shaped stand-in that Espresso cannot observe.

Instrumented tests swap the whole container instead of trying to wait for that thread:

1. `app/build.gradle.kts` sets `testInstrumentationRunner = "com.example.kotlintest.InstrumentedTestRunner"`.
2. `InstrumentedTestRunner.newApplication()` instantiates `TestApp` (a `KotlinTestApp` subclass) in place of the manifest's application class.
3. `TestApp.appContainer` is a `TestAppContainer`, whose `deviceRepository` is `FakeDeviceRepository` - it posts `DeviceCatalog.all` to the main looper with no background thread and no delay.

Espresso drains the main looper before every `onView()`, so the fake's callback has always run by the time a test asserts. Result: no `IdlingResource`, no `Thread.sleep` in the suite, and nothing test-specific left in the production APK. (The earlier `EspressoIdlingResource` + `IdlingResourceRule` pair, and the `espresso-idling-resource` `implementation` dependency, were removed for exactly this reason - `app/espresso-pitfalls-spec.md` section 4 keeps the history.) This is the same mechanism Hilt's `CustomTestRunner` + `HiltTestApplication` use, without the framework.

Adding a new injectable dependency: declare it on `AppContainer`, wire the real one in `DefaultAppContainer` and the fake in `TestAppContainer`. Never have UI code read a singleton `object` directly when the dependency touches a thread, the network, or the clock - that is what forces IdlingResources back in.

## 4. Test class / Rule conventions

`@get:Rule(order = N)`, smallest first:

0. Permission rules (`GrantPermissionRule`), if the screen needs one
0/1. `ResetDeviceCatalogRule` - must run before `ActivityScenarioRule` so a previous test's leftover state can't leak into this one (`DeviceCatalog` is a process-wide singleton and there is no AndroidTestOrchestrator)
last. `ActivityScenarioRule` - launches the Activity under test, always last so earlier rules' setup is already in place

There is no idling rule any more - the fake repository (section 3) makes it unnecessary, even for `SettingsFragmentTest`, where the hidden `DeviceListFragment` still performs its (now instant) load.

Test classes live under `cases/{login,device,setting}/` by screen/feature; a screen-agnostic test (no UI, like `ExampleInstrumentedTest`) goes at `cases/` root instead of forcing it into one of the three feature folders.

## 5. Tests skip login entirely

Tests never go through `LoginActivity` - `ActivityScenarioRule(TargetActivity::class.java)` launches the Activity under test directly (or with a custom `Intent`, e.g. `DeviceDetailActivityTest`'s `intentFor(deviceId)`), bypassing the whole navigation chain (Login → Home → DeviceList → ...).

This only works because the app has **no real auth guard** anywhere - no Activity checks a session/token before rendering. If real authentication is ever added, this breaks the moment a guard checks session state in `onCreate()`. The fix then is to put the auth/session dependency on `AppContainer` and give `TestAppContainer` a fake that is already signed in (or seed session state in a rule analogous to `ResetDeviceCatalogRule`) - not to keep assuming direct-launch still works. See [[edge-to-edge-insets]] for another case of behavior that depends on exactly how these Activities are wired.

## 6. Test fixture data

- **Login credentials** (`LoginActivityTest`) come from instrumentation arguments - `TestArguments.require("testLoginEmail")` / `TestArguments.require("testLoginPassword")` - never hardcoded strings. `app/build.gradle.kts` sets them in `testInstrumentationRunnerArguments` via `secretProperty()` (environment variable → `secrets.properties` → default), so they never enter the app's `BuildConfig` or the production APK. `secrets.defaults.properties` documents the key names and current default values. Any new test-only secret/fixture value follows the same pattern. The arguments are only passed when tests run through Gradle (`make androidTest` / `connectedDebugAndroidTest`, or Android Studio with its default "run instrumented tests using Gradle" behaviour); `TestArguments.require` fails with a pointed message otherwise.
- **Newly-created entity names** (e.g. `AddDeviceFlowTest`'s device name) use `RandomTestData.deviceName()` for cosmetic variety across runs - this is not collision-avoidance (tests aren't run in parallel, and `ResetDeviceCatalogRule` gives every test a clean `DeviceCatalog`), purely to avoid the same literal every run. Don't randomize a lookup against **existing seeded data** (`server-01`, `router-main`, `nas-backup` etc. from `DeviceCatalog.defaultDevices()`) - only randomize values the test itself creates.

## 7. Adding a new test

1. New screen? Add a `.claude/skills/screen-*` doc first, then a matching Page Object under `pages/` (or `pages/<feature>/` if it belongs to an existing feature group).
2. Add the test class under `cases/<feature>/`, or `cases/` root if it's screen-agnostic.
3. Reuse `ResetDeviceCatalogRule` if the screen touches `DeviceCatalog`; don't invent a parallel reset mechanism. If the screen needs a new async/external dependency, route it through `AppContainer` and fake it in `TestAppContainer` (section 3) rather than reaching for an `IdlingResource`.
4. Any new test-only secret/fixture value goes through `secretProperty()` → `testInstrumentationRunnerArguments` in `app/build.gradle.kts`, documented in `secrets.defaults.properties` - never hardcoded in the test file, never in a `buildConfigField`.
5. **Update this skill doc** whenever the structure itself changes (new top-level folder, new Rule convention, new shared helper, new container dependency) - see [[android-id-naming]] section 7 for why skill docs need to stay in sync with the code they describe.
