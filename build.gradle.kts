plugins {
    `java-library`
    `maven-publish`

    id("net.neoforged.moddev") version "2.0.141"
    id("apex-conventions.jspecify")
}

group = "dev.apexstudios"
base.archivesName = "registree"
version = providers.environmentVariable("VERSION").getOrElse("0.0NONE")
neoForge.version = libs.versions.neoforge.get()

java {
    toolchain.vendor.set(JvmVendorSpec.JETBRAINS)
    withSourcesJar()
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
            artifactId = "registree"
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