# Registree

`Registree` is a library-mod for Minecraft running on the [NeoForge modloader](https://neoforged.net/), designed to make handling all your registry needs easy and efficient. The name is a play on 'Registry' to emphasize its core purpose.

### Key Features
- **Deferred Holders**: Provides many more deferred holder types, making them easier to deal with.
- **Reduced Boilerplate**: Automatically handles registering to the various registry types available, No more creating a 'DeferredRegister' for each registry just 1 'Registree'.

### Obtaining Registree

Registree is a development library and is not intended for end-users to download directly. It can not be found on platforms like CurseForge or Modrinth.
To use it, you must add it as a dependency in your mod's `build.gradle` file.

The library is available through our [Maven repository](https://maven.apexmodder.com/#/releases/dev/apexstudios/registree).

<details>
<summary> Groovy DSL (build.gradle) </summary>

```groovy
repositories {
    maven { url "https://maven.apexmodder.com/releases" }
}

dependencies {
    // Versions here must match
    implementation "dev.apexstudios:registree:<version>"
    jarJar "dev.apexstudios:registree:<version>"
}
```

</details>

<details>
<summary> Kotlin DSL (build.gradle.kts) </summary>

```groovy
repositories {
    maven("https://maven.apexmodder.com/releases")
}

dependencies {
    // Versions here must match
    implementation("dev.apexstudios:registree:<version>")
    jarJar("dev.apexstudios:registree:<version>")
}
```

</details>
