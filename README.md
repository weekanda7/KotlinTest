# KotlinTest

[![androidTest](https://github.com/weekanda7/KotlinTest/actions/workflows/androidTest.yml/badge.svg)](https://github.com/weekanda7/KotlinTest/actions/workflows/androidTest.yml)

A small Android "device manager" app whose real purpose is the **Espresso UI-test suite around it**:
Page Objects, a fake-injection seam that keeps test hooks out of the production APK, and a
GitHub Actions pipeline that runs the suite on Gradle-managed emulators. Every non-obvious decision
in the test code and the CI config is written down next to the failure that motivated it.

## The app

Kotlin, XML views, Material 3, ViewBinding. Five screens: **Login** (email/password validation) →
**Home** → **Device list** with a Devices tab (search, list, add) and a Settings tab (preferences,
runtime notification permission) → **Add device** (form, dropdown, date picker) and
**Device detail** (delete with confirmation). Data is an in-memory catalog behind a
`DeviceRepository` interface whose production implementation simulates a slow network call.

## The tests

22 instrumented tests (Espresso 3.7, JUnit 4) covering login validation, list loading and
search, the add/delete flows, detail rendering, tab switching and settings persistence.

- **Page Object Model, strictly layered.** `cases/` holds test classes and never calls `onView()`;
  every Espresso interaction lives in `pages/`, one object per screen, one method per action or
  assertion. Cross-screen helpers (RecyclerView row lookup, Snackbar assertions) sit in
  `pages/common/`.
- **Fakes via a custom runner, no framework.** The app resolves its dependencies from an
  `AppContainer` owned by the `Application` class. `InstrumentedTestRunner` boots the process with
  `TestApp`, whose container serves a `FakeDeviceRepository` that answers on the main looper — the
  same mechanism Hilt's `CustomTestRunner` uses. Result: no `IdlingResource`, no sleeps, and no
  test-only code or dependencies in the production APK.
- **Fixture data outside the code.** Login credentials reach the test APK as instrumentation
  arguments, sourced from an env var → `secrets.properties` → default chain in
  `app/build.gradle.kts`, so a CI account can be swapped in without touching a test file.
- **Explicit rule ordering.** `GrantPermissionRule` → `ResetDeviceCatalogRule` (the in-memory
  store is process-wide and there is no orchestrator) → `ActivityScenarioRule`, always last.
- **Locators by contract.** View IDs follow a documented naming spec
  ([app/android-id-spec.md](app/android-id-spec.md)); text lookups use `R.string`, never literals;
  dialogs, popups and RecyclerView rows are scoped rather than matched globally.

## CI: Gradle-managed devices on GitHub Actions

[`androidTest.yml`](.github/workflows/androidTest.yml) runs the suite on two Automated Test Device
images (Pixel 8 profile, API 30 and API 33), one emulator per runner, on every push. Getting a
hosted runner to boot an emulator took five iterations, each captured in the workflow comments and
in [`app/espresso-pitfalls-spec.md`](app/espresso-pitfalls-spec.md): opening `/dev/kvm` to the
runner user, a missing `libpulse0` on the Ubuntu 24.04 image, ~3 GB of free disk where the emulator
demands 7.4 GB per AVD, two setup tasks racing to install the same SDK package, and a full
Google-APIs image whose keyguard/ANR dialogs never handed window focus to the app — which is why
only ATD images run in CI and full images stay on a local AVD.

## Running it

```bash
make build        # assemble the debug APK
make test         # JVM unit tests
make androidTest  # instrumented tests on the connected device / running AVD
make gmd          # instrumented tests on the Gradle-managed ATD (Pixel 8 / API 33)
make gmdCi        # the same two devices CI uses (API 30 + API 33)
make lint         # Android lint
```

Requirements: Android Studio's SDK (or command-line tools) with an emulator; Gradle provisions its
own JDK through the toolchain resolver. The first `make gmd` downloads the ATD system image and
creates a snapshot, later runs start from it. Optional: copy `secrets.defaults.properties` to
`secrets.properties` to override the login fixture locally.

## Repository map

```
app/src/main/java/com/example/kotlintest/     the app (Activities, Fragments, AppContainer, DeviceRepository)
app/src/androidTest/java/com/example/kotlintest/
  cases/{login,device,setting}/               test classes — no Espresso calls here
  pages/                                      Page Objects — every onView() lives here
  InstrumentedTestRunner.kt, TestApp.kt       fake-injection seam
  FakeDeviceRepository.kt, TestArguments.kt   test double + instrumentation-argument reader
  ResetDeviceCatalogRule.kt                   shared JUnit rule
app/espresso-pitfalls-spec.md                 field notes: nine real failures, root causes, fixes (zh-TW)
app/android-id-spec.md                        view-ID naming and locator rules (zh-TW)
.claude/skills/                               per-screen and architecture docs, kept in sync with the code
.github/workflows/androidTest.yml             the CI pipeline
```
