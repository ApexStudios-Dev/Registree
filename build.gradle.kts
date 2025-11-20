plugins {
    id("apex-conventions.neoforge") version "0.1.75"
    id("apex-conventions.maven-publishing") version "0.1.75"
}

group = "dev.apexstudios"

apex.neoVersion("21.11.0-alpha.25w45a.20251119.234730", "1.21.10", "2025.10.12")
apex.extendCompilerErrors()

repositories {
    apex.neoPrMaven(this, 2815)
}

tasks.withType<Jar> {
    manifest {
        attributes(
            "FMLModType" to "GAMELIBRARY",
            "Automatic-Module-Name" to "registree"
        )
    }
}