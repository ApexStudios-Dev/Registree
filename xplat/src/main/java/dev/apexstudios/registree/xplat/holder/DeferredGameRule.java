package dev.apexstudios.registree.xplat.holder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.gamerules.GameRule;

public final class DeferredGameRule<TValue> extends DeferredHolder<GameRule<?>, GameRule<TValue>> {
    public DeferredGameRule(ResourceKey<GameRule<?>> registryKey) {
        super(registryKey);
    }
}
