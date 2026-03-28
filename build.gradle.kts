plugins {
    id("net.neoforged.moddev") version "2.0.141" apply false
    id("net.fabricmc.fabric-loom-companion") version "1.14.4" apply false
    id("net.fabricmc.fabric-loom") version "1.14-SNAPSHOT" apply false
}

tasks.register("applyAllFormatting") {
    group = "verification"
    dependsOn(subprojects.map { it.tasks.findByName("applyAllFormatting") })
}

tasks.register("checkFormatting") {
    group = "verification"
    dependsOn(subprojects.map { it.tasks.findByName("checkFormatting") })
}

tasks.register("generatePackageInfos") {
    group = "verification"
    dependsOn(subprojects.map { it.tasks.findByName("generatePackageInfos") })
}