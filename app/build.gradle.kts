import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// Resolves a secret value without ever committing it to the repo:
//   1. Environment variable (e.g. a GitHub Actions `secrets.*` exposed via `env:`)
//   2. secrets.properties at the repo root (gitignored, per-developer local values)
//   3. the given default
// See secrets.defaults.properties for the list of keys this project expects.
fun secretProperty(name: String, default: String = ""): String {
    // A CI env var wired to an undefined secret (e.g. `${{ secrets.X }}` on a fork PR)
    // arrives as an empty string, not as unset - treat blank as "not provided".
    System.getenv(name)?.takeIf { it.isNotBlank() }?.let { return it }
    val secretsFile = rootProject.file("secrets.properties")
    if (secretsFile.exists()) {
        val props = Properties()
        secretsFile.inputStream().use { props.load(it) }
        props.getProperty(name)?.let { return it }
    }
    return default
}

android {
    namespace = "com.example.kotlintest"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.kotlintest"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        // Custom runner (androidTest/InstrumentedTestRunner) that boots the process with
        // TestApp instead of KotlinTestApp, so every Activity under test is served by an
        // AppContainer full of fakes - see .claude/skills/test-architecture.
        testInstrumentationRunner = "com.example.kotlintest.InstrumentedTestRunner"

        // Espresso login test fixture (LoginActivityTest). Handed to the test APK as
        // instrumentation arguments (InstrumentationRegistry.getArguments()) instead of
        // being baked into the app's BuildConfig, so test-only values never ship in the
        // production APK. Swappable via secrets.properties or CI env vars without touching
        // test code. Defaults preserve today's values.
        testInstrumentationRunnerArguments["testLoginEmail"] =
            secretProperty("TEST_LOGIN_EMAIL", "henry@example.com")
        testInstrumentationRunnerArguments["testLoginPassword"] =
            secretProperty("TEST_LOGIN_PASSWORD", "password123")

        // Placeholder demonstrating the secretProperty() wiring - replace/add real
        // keys here once there's an actual backend/API key to inject.
        buildConfigField("String", "EXAMPLE_API_KEY", "\"${secretProperty("EXAMPLE_API_KEY")}\"")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    testOptions {
        managedDevices {
            // AGP 9 defaults testedAbi to x86_64 and warns that AGP 10 flips the default to
            // arm64-v8a. Pin it to the host instead: an x86_64 CI runner boots x86_64 images
            // (the ATD image has no NDK translation, so nothing else could run there), an
            // Apple-silicon Mac boots arm64-v8a ones.
            val hostAbi = if (System.getProperty("os.arch") in setOf("aarch64", "arm64")) "arm64-v8a" else "x86_64"
            localDevices {
                // Same profile/API as the hand-made Pixel_8 AVD: the "does it also pass on GMD" baseline.
                create("pixel8api37") {
                    device = "Pixel 8"
                    apiLevel = 37
                    systemImageSource = "google"
                    testedAbi = hostAbi
                }
                // Automated Test Device: no SystemUI/launcher/IME/background services, ~20% faster.
                // Headless only - fine for these tests, not for anything that inspects real chrome.
                create("pixel8api33atd") {
                    device = "Pixel 8"
                    apiLevel = 33
                    systemImageSource = "aosp-atd"
                    testedAbi = hostAbi
                }
            }
            groups {
                // ManagedDevices no longer exposes `devices` (the container the older docs
                // reference); look devices up in `localDevices` (or `allDevices`) instead.
                create("ci") {
                    targetDevices.add(localDevices["pixel8api33atd"])
                    targetDevices.add(localDevices["pixel8api37"])
                }
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.espresso.contrib) {
        exclude(group = "androidx.recyclerview", module = "recyclerview")
    }
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
}