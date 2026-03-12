// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
}

// https://developer.android.com/build/releases/agp-9-0-0-release-notes#runtime-dependency-on-kotlin-gradle-plugin-upgrade
buildscript {
    dependencies {
        classpath(libs.kotlin.gradle.plugin)
    }
}