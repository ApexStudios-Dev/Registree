import org.gradle.internal.extensions.stdlib.capitalized
import org.slf4j.event.Level

plugins {
    `java-library`
    `maven-publish`

    id("net.neoforged.moddev")
    id("apex-conventions.jspecify")
}

val xplat = evaluationDependsOn(":xplat")

group = "dev.apexstudios"
base.archivesName = "registree-neoforge"
version = providers.environmentVariable("VERSION").getOrElse("0.0NONE")

neoForge {
    version = libs.versions.neoforge.get()
    addModdingDependenciesTo(sourceSets[SourceSet.TEST_SOURCE_SET_NAME])

    mods {
        create(SourceSet.MAIN_SOURCE_SET_NAME) {
            sourceSet(sourceSets[SourceSet.MAIN_SOURCE_SET_NAME])
            sourceSet(xplat.sourceSets[SourceSet.MAIN_SOURCE_SET_NAME])
        }

        create(SourceSet.TEST_SOURCE_SET_NAME) {
            sourceSet(sourceSets[SourceSet.TEST_SOURCE_SET_NAME])
            sourceSet(xplat.sourceSets[SourceSet.TEST_SOURCE_SET_NAME])
        }
    }

    runs {
        listOf(true, false).forEach { isClient ->
            val id = if(isClient) "client" else "server"

            create(id) {
                if(isClient) {
                    client()
                } else {
                    server()
                }

                ideName.set("${id.capitalized()} (:neoforge)")
                logLevel.set(Level.DEBUG)
                gameDirectory.set(layout.projectDirectory.dir("run/$id"))
                sourceSet.set(sourceSets[SourceSet.TEST_SOURCE_SET_NAME])
                loadedMods.set(listOf(mods[SourceSet.MAIN_SOURCE_SET_NAME], mods[SourceSet.TEST_SOURCE_SET_NAME]))
                systemProperty("terminal.ansi", "true") // fix terminal not having colors

                jvmArguments.addAll(
                    "-XX:+AllowEnhancedClassRedefinition",
                    "-XX:+IgnoreUnrecognizedVMOptions",
                    "-XX:+AllowRedefinitionToAddDeleteMethods",
                    "-XX:+ClassUnloading"
                )
            }
        }
    }
}

dependencies {
    compileOnly(project(":xplat")) {
        isTransitive = false
    }

    accessTransformers(project(":xplat")) {
        isTransitive = false
    }

    testImplementation(xplat)
    testImplementation(xplat.sourceSets[SourceSet.TEST_SOURCE_SET_NAME].output)
}

java {
    toolchain.vendor.set(JvmVendorSpec.JETBRAINS)
    withSourcesJar()
}

tasks.named(sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].compileJavaTaskName, JavaCompile::class.java) {
    source(xplat.sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].allJava)
}

tasks.named(sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].javadocTaskName, Javadoc::class.java).configure {
    source(xplat.sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].allJava)
}

tasks.named(sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].sourcesJarTaskName, Jar::class.java) {
    from(xplat.sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].allSource)
}

tasks.named(sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].processResourcesTaskName, ProcessResources::class.java) {
    from(xplat.sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].resources)
}

tasks.withType<Jar> {
    manifest {
        attributes(
            "FMLModType" to "GAMELIBRARY",
            "Automatic-Module-Name" to "registree"
        )
    }
}

publishing {
    publications.create("release", MavenPublication::class.java) {
        afterEvaluate {
            groupId = "dev.apexstudios"
            artifactId = "registree-neoforge"
            version = project.version as String
        }

        from(components["java"])
    }

    repositories {
        if(System.getenv("MAVEN_USERNAME") != null && System.getenv("MAVEN_PASSWORD") != null) {
            maven("https://maven.apexmodder.com/releases") {
                name = "ApexStudios-Releases"

                credentials {
                    username = System.getenv("MAVEN_USERNAME")
                    password = System.getenv("MAVEN_PASSWORD")
                }

                authentication.create<BasicAuthentication>("basic")
            }
        }
    }
}