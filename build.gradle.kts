plugins {
    id("apex-conventions.neoforge") version "0.1.88-beta-pr-12"
    id("apex-conventions.maven-publishing") version "0.1.88-beta-pr-12"
}

group = "dev.apexstudios"

apex.neoVersion("26.1.0-alpha.26.1-snapshot-1.20251219.121649")
apex.extendCompilerErrors()

repositories {
    apex.neoPrMaven(this, 2879)
    maven("https://maven.apexstudios.dev/prs/ApexGradle/pr12")
}

tasks.withType<Jar> {
    manifest {
        attributes(
            "FMLModType" to "GAMELIBRARY",
            "Automatic-Module-Name" to "registree"
        )
    }
}