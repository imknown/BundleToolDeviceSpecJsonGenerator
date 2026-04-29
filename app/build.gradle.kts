plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.compose.compiler)
}

private val buildVersion = libs.versions

android {
    namespace = "net.imknown.android.bundletooldevicespecjsongenerator"

    val isPreview = buildVersion.isPreview.get().toBoolean()
    compileSdk {
        version = if (isPreview) {
            preview(buildVersion.compileSdkPreview.get())
        } else {
            release(buildVersion.compileSdk.get().toInt()) {
                minorApiLevel = buildVersion.compileSdkMinor.get().toInt()
                // sdkExtension = buildVersion.compileSdkExtension.get().toInt()
            }
        }
    }
    buildToolsVersion = (if (isPreview) buildVersion.buildToolsPreview else buildVersion.buildTools).get()

    defaultConfig {
        minSdk = buildVersion.minSdk.get().toInt()

        targetSdk = buildVersion.targetSdk.get().toInt()
        if (isPreview) {
            targetSdkPreview = buildVersion.targetSdkPreview.get()
        }

        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        named("debug") {
            storeFile = file("$rootDir/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }

        debug {
            signingConfig = signingConfigs.getByName(name)
        }
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    // Compose
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Core Android dependencies
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.activity.ktx)

    // Local tests: jUnit, coroutines, Android runner
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    // Instrumented tests: jUnit rules and runners
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.ext.junit)

    debugImplementation(libs.leakcanary.android)
}