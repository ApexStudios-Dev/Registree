package dev.apexstudios.registree.api.holder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.gamerules.GameRule;

public final class DeferredGameRule<TRule> extends ApexDeferredHolder<GameRule<?>, GameRule<TRule>> {
    public DeferredGameRule(ResourceKey<GameRule<?>> registryKey) {
        super(registryKey);
    }
}
