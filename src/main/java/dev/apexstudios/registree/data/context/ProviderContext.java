package dev.apexstudios.registree.data.context;

import dev.apexstudios.registree.Registree;
import java.util.function.Function;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;

public interface ProviderContext {
    Registree registree();

    ResourceManager resourceManager(PackType packType);

    FeatureFlagSet enabledFeatures();

    static ProviderContext of(Registree registree, Function<PackType, ResourceManager> resourceManagerGetter, FeatureFlagSet enabledFeatures) {
        return new ProviderContext() {
            @Override
            public Registree registree() {
                return registree;
            }

            @Override
            public ResourceManager resourceManager(PackType packType) {
                return resourceManagerGetter.apply(packType);
            }

            @Override
            public FeatureFlagSet enabledFeatures() {
                return enabledFeatures;
            }
        };
    }
}
