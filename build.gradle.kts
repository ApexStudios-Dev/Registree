plugins {
    id("apex-conventions.neoforge")
    id("apex-conventions.maven-publishing")
    id("apex-conventions.jspecify")
}

group = "dev.apexstudios"
neoForge.version = libs.versions.neoforge.get()

repositories {
    /*maven("https://prmaven.neoforged.net/NeoForge/pr3198") {
        content {
            includeModule("net.neoforged", "neoforge")
            includeModule("net.neoforged", "testframework")
        }
    }*/
}

tasks.withType(Jar::class.java) {
    manifest {
        attributes["FMLModType"] = "GAMELIBRARY"
        attributes["Automatic-Module-Name"] = project.name.lowercase()
    }
}