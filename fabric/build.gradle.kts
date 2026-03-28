import org.gradle.internal.extensions.stdlib.capitalized

plugins {
    `java-library`
    `maven-publish`

    id("net.fabricmc.fabric-loom-companion")
    id("net.fabricmc.fabric-loom")
    id("apex-conventions.jspecify")
}

val xplat = evaluationDependsOn(":xplat")

group = "dev.apexstudios.registree"
base.archivesName = "fabric"
version = providers.environmentVariable("VERSION").getOrElse("0.0NONE")

loom {
    enableTransitiveAccessWideners.set(true)
    accessWidenerPath.set(file("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources/registree.accesswidener"))

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

            named(id) {
                if(isClient) {
                    client()

                    programArgs("--launch_target", "net.fabricmc.loader.impl.launch.knot.KnotClient")
                    mainClass.set("net.covers1624.devlogin.DevLogin")
                } else {
                    server()
                }

                configName = id.capitalized()
                ideConfigGenerated(true)
                runDir("run/$id")
                source(sourceSets[SourceSet.TEST_SOURCE_SET_NAME])
                property("terminal.ansi", "true") // fix terminal not having colors

                vmArgs(
                    "-XX:+AllowEnhancedClassRedefinition",
                    "-XX:+IgnoreUnrecognizedVMOptions",
                    "-XX:+AllowRedefinitionToAddDeleteMethods",
                    "-XX:+ClassUnloading"
                )
            }
        }
    }
}

repositories {
    maven("https://maven.covers1624.net")
}

dependencies {
    minecraft(libs.minecraft)
    implementation(libs.bundles.fabric)

    runtimeOnly(libs.devlogin)

    compileOnly(dependencyFactory.create(xplat)) { isTransitive = false }

    testImplementation(xplat)
    testImplementation(xplat.sourceSets[SourceSet.TEST_SOURCE_SET_NAME].output)
}

java {
    toolchain {
        vendor.set(JvmVendorSpec.JETBRAINS)
        languageVersion.set(JavaLanguageVersion.of(25))
    }

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
    exclude("**/accesstransformer**.cfg")
    from(xplat.sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].resources)
}

tasks.withType<ProcessResources> {
    val expanded = mapOf(
        Pair("version", version)
    )

    filesMatching("fabric.mod.json") {
        expand(expanded)
    }

    inputs.properties(expanded)
}

publishing {
    publications.create("release", MavenPublication::class.java) {
        afterEvaluate {
            groupId = project.group as String
            artifactId = project.name
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