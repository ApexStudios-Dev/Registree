package dev.apexstudios.registree.data.context;

import dev.apexstudios.registree.Registree;
import java.nio.file.Path;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;

public interface ProviderOutputContext extends ProviderListenerContext {
    PackOutput output();

    default PackOutput.PathProvider pathProvider(PackOutput.Target packType, String kind) {
        return output().createPathProvider(packType, kind);
    }

    default PackOutput.PathProvider pathProvider(PackType packType, String kind) {
        return pathProvider(packType(packType), kind);
    }

    default PackOutput.PathProvider tagPathProvider(ResourceKey<? extends Registry<?>> registryKey) {
        return output().createRegistryTagsPathProvider(registryKey);
    }

    default PackOutput.PathProvider elementPathProvider(ResourceKey<? extends Registry<?>> registryKey) {
        return output().createRegistryElementsPathProvider(registryKey);
    }

    default Path outputPath(PackOutput.Target packType, Identifier resourcePath) {
        return output().getOutputFolder(packType).resolve(resourcePath.getNamespace()).resolve(resourcePath.getPath());
    }

    default Path outputPath(PackOutput.Target packType, String... path) {
        return outputPath(packType, registree().registryName(String.join("/", path)));
    }

    default Path outputPath(PackType packType, Identifier resourcePath) {
        return outputPath(packType(packType), resourcePath);
    }

    default Path outputPath(PackType packType, String... path) {
        return outputPath(packType(packType), path);
    }

    static PackOutput.Target packType(PackType packType) {
        return switch (packType) {
            case CLIENT_RESOURCES -> PackOutput.Target.RESOURCE_PACK;
            case SERVER_DATA -> PackOutput.Target.DATA_PACK;
        };
    }

    static ProviderOutputContext of(ProviderListenerContext context, PackOutput output) {
        return new ProviderOutputContext() {
            @Override
            public PackOutput output() {
                return output;
            }

            @Override
            public HolderLookup.Provider registries() {
                return context.registries();
            }

            @Override
            public Registree registree() {
                return context.registree();
            }

            @Override
            public ResourceManager resourceManager(PackType packType) {
                return context.resourceManager(packType);
            }

            @Override
            public FeatureFlagSet enabledFeatures() {
                return context.enabledFeatures();
            }
        };
    }
}
