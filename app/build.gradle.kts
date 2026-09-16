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
    System.getenv(name)?.let { return it }
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

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Placeholder demonstrating the secretProperty() wiring - replace/add real
        // keys here once there's an actual backend/API key to inject.
        buildConfigField("String", "EXAMPLE_API_KEY", "\"${secretProperty("EXAMPLE_API_KEY")}\"")

        // Espresso login test fixture - kept out of the test source so it can be
        // swapped (e.g. for a real QA account) via secrets.properties or CI env
        // vars without touching test code. Defaults preserve today's values.
        buildConfigField(
            "String", "TEST_LOGIN_EMAIL", "\"${secretProperty("TEST_LOGIN_EMAIL", "henry@example.com")}\""
        )
        buildConfigField(
            "String", "TEST_LOGIN_PASSWORD", "\"${secretProperty("TEST_LOGIN_PASSWORD", "password123")}\""
        )
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
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.espresso.idling.resource)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.espresso.contrib) {
        exclude(group = "androidx.recyclerview", module = "recyclerview")
    }
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.rules)
}