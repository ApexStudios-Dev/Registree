package dev.apexstudios.registree.api.holder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.gamerules.GameRule;
import net.neoforged.neoforge.registries.DeferredHolder;

public class DeferredGameRule<TType> extends DeferredHolder<GameRule<?>, GameRule<TType>> {
    public DeferredGameRule(ResourceKey<GameRule<?>> registryKey) {
        super(registryKey);
    }
}
