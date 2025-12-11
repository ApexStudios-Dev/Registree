plugins {
    id("apex-conventions.neoforge") version "0.1.83"
    id("apex-conventions.maven-publishing") version "0.1.83"
    id("apex-conventions.immaculate") version "0.1.83"
}

group = "dev.apexstudios"

apex.neoVersion("21.11.0-beta", "1.21.10", "2025.10.12")
apex.extendCompilerErrors()

tasks.withType<Jar> {
    manifest {
        attributes(
            "FMLModType" to "GAMELIBRARY",
            "Automatic-Module-Name" to "registree"
        )
    }
}