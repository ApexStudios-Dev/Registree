package dev.apexstudios.registree.api.holder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.gamerules.GameRule;

public final class DeferredGameRule<TValue> extends ApexDeferredHolder<GameRule<?>, GameRule<TValue>> {
    public DeferredGameRule(ResourceKey<GameRule<?>> registryKey) {
        super(registryKey);
    }
}
