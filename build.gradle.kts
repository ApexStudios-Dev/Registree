plugins {
    id("apex-conventions.neoforge") version "0.1.88"
    id("apex-conventions.maven-publishing") version "0.1.88"
    id("apex-conventions.jspecify") version "0.1.88"
}

group = "dev.apexstudios"

apex.neoVersion("26.1.0.0-alpha.5+snapshot-2")
apex.extendCompilerErrors()

tasks.withType<Jar> {
    manifest {
        attributes(
            "FMLModType" to "GAMELIBRARY",
            "Automatic-Module-Name" to "registree"
        )
    }
}