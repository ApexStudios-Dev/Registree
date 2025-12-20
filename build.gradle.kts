import java.text.SimpleDateFormat
import java.util.*

plugins {
    `java-library`
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.9"
    id("net.neoforged.moddev") version "2.0.131"
}

val IS_CI = providers.environmentVariable("CI").map(String::toBoolean).getOrElse(false)

group = "dev.apexstudios"
version = providers.environmentVariable("VERSION").getOrElse("9.9.999")
base.archivesName = project.name.lowercase()

idea.module {
    if(!IS_CI) {
        isDownloadSources = true
        isDownloadJavadoc = true
    }

    excludeDirs.addAll(files(
        ".gradle",
        ".idea",
        "gradle",
    ))
}

java {
    withSourcesJar()
}

neoForge {
    version = "26.1.0-alpha.26.1-snapshot-1.20251219.121649"
}

tasks.withType(Jar::class.java) {
    manifest {
        attributes.putAll(mutableMapOf(
            "Specification-Title" to project.name,
            "Specification-Vendor" to "ApexStudios",
            "Specification-Version" to "1",

            "Implementation-Title" to project.name,
            "Implementation-Vendor" to "ApexStudios",
            "Implementation-Version" to project.version,
            "Implementation-Timestamp" to SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ssZ").format(Date()),

            "FMLModType" to "GAMELIBRARY",
            "Automatic-Module-Name" to "registree"
        ))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

repositories {
    maven("https://maven.apexstudios.dev/proxy")

    maven("https://prmaven.neoforged.net/NeoForge/pr2879") {
        name = "NeoForge - PR #2879"

        content {
            includeModule("net.neoforged", "testframework")
            includeModule("net.neoforged", "neoforge")
        }
    }
}