plugins {
    id("apex-conventions.neoforge") version "0.1.92-beta-pr-12"
    id("apex-conventions.maven-publishing") version "0.1.92-beta-pr-12"
}

group = "dev.apexstudios"

neoForge.enable {
    version = "26.1.0.0-alpha.1+snapshot-1"
    isDisableRecompilation = providers.environmentVariable("CI").map(String::toBoolean).getOrElse(false)
}

repositories {
    maven("https://prmaven.neoforged.net/NeoForge/pr2879")
}

tasks.withType<Jar> {
    manifest {
        attributes(
            "FMLModType" to "GAMELIBRARY",
            "Automatic-Module-Name" to "registree"
        )
    }
}