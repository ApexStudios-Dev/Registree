pluginManagement {
    repositories {
        maven("https://maven.apexstudios.dev/proxy")
        gradlePluginPortal()
    }

    resolutionStrategy {
        eachPlugin {
            if(requested.id.namespace == "apex-conventions") {
                useVersion("0.1.91")
            }
        }
    }
}

dependencyResolutionManagement {
    versionCatalogs.create("libs") {
        version("neoforge", "26.1.0.0-alpha.11+snapshot-7")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "Registree"
