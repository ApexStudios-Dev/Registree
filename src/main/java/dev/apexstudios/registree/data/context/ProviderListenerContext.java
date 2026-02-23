package dev.apexstudios.registree.data.context;

import dev.apexstudios.registree.Registree;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;

public interface ProviderListenerContext extends ProviderContext {
    HolderLookup.Provider registries();

    static ProviderListenerContext of(ProviderContext context, HolderLookup.Provider registries) {
        return new ProviderListenerContext() {
            @Override
            public HolderLookup.Provider registries() {
                return registries;
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
