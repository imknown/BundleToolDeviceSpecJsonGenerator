pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

include(":app")
rootProject.name = "BundleToolDeviceSpecJsonGenerator"

plugins {
    // https://docs.gradle.org/current/userguide/gradle_daemon.html#sec:configuring_daemon_jvm
    // ./gradlew -q javaToolchains
    // ./gradlew updateDaemonJvm --jvm-version=25 --jvm-vendor=adoptium
    // https://plugins.gradle.org/plugin/org.gradle.toolchains.foojay-resolver-convention
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}