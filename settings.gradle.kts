pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net")
        maven("https://maven.apexmodder.com/releases")
    }

    if(file("../../ApexGradle").exists()) {
        includeBuild("../../ApexGradle")
    } else {
        resolutionStrategy {
            eachPlugin {
                if(requested.id.namespace == "apex-conventions") {
                    useVersion("0.1.94")
                }
            }
        }
    }
}

dependencyResolutionManagement {
    versionCatalogs.create("libs") {
        version("neoforge", "26.1.0.7-beta")
        version("neoform", "26.1-1")

        library("minecraft", "com.mojang", "minecraft").version("26.1")

        library("fabric-loader", "net.fabricmc", "fabric-loader").version("0.18.4")
        library("fabric-api", "net.fabricmc.fabric-api", "fabric-api").version("0.144.0+26.1")
        bundle("fabric", listOf("fabric-loader", "fabric-api"))

        library("devlogin", "net.covers1624", "DevLogin").version("0.1.0.5")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include("xplat")
include("neoforge")
include("fabric")

rootProject.name = "Registree"
