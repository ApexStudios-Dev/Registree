plugins {
    id("apex-conventions.neoforge") version "0.1.85"
    id("apex-conventions.maven-publishing") version "0.1.85"
}

group = "dev.apexstudios"

apex.neoVersion("21.10.41-beta", "2025.10.12")
apex.extendCompilerErrors()

tasks.withType<Jar> {
    manifest {
        attributes(
            "FMLModType" to "GAMELIBRARY",
            "Automatic-Module-Name" to "registree"
        )
    }
}