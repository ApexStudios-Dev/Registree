plugins {
    `java-library`
    `maven-publish`

    id("net.neoforged.moddev")
    id("apex-conventions.jspecify")
}

group = "dev.apexstudios"
base.archivesName = "registree-xplat"
version = providers.environmentVariable("VERSION").getOrElse("0.0NONE")

neoForge {
    validateAccessTransformers.set(true)

    accessTransformers {
        from(file("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources/META-INF/accesstransformer-xplat.cfg"))
        publish(file("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources/META-INF/accesstransformer-xplat.cfg"))
    }

    neoFormVersion = libs.versions.neoform.get()
    addModdingDependenciesTo(sourceSets[SourceSet.TEST_SOURCE_SET_NAME])
}

java {
    toolchain.vendor.set(JvmVendorSpec.JETBRAINS)
    withSourcesJar()
}

publishing {
    publications.create("release", MavenPublication::class.java) {
        afterEvaluate {
            groupId = "dev.apexstudios"
            artifactId = "registree-xplat"
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