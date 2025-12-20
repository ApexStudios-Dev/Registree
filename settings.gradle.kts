pluginManagement {
    repositories {
        maven("https://maven.apexstudios.dev/proxy")
        maven("https://maven.apexstudios.dev/prs/ApexGradle/pr12")
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "Registree"
