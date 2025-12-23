plugins {
    id("apex-conventions.neoforge") version "0.1.87"
    id("apex-conventions.maven-publishing") version "0.1.87"
}

group = "dev.apexstudios"

apex.neoVersion("26.1.0.0-alpha.1+snapshot-1")
apex.extendCompilerErrors()

tasks.withType<Jar> {
    manifest {
        attributes(
            "FMLModType" to "GAMELIBRARY",
            "Automatic-Module-Name" to "registree"
        )
    }
}