plugins {
    id("apex-conventions.neoforge")
    id("apex-conventions.maven-publishing")
    id("apex-conventions.jspecify")
}

group = "dev.apexstudios"
neoForge.version = libs.versions.neoforge.get()

tasks.withType(Jar::class.java) {
    manifest {
        attributes["FMLModType"] = neoForge.minecraftVersion
        attributes["Automatic-Module-Name"] = project.name.lowercase()
    }
}