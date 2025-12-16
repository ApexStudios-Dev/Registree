package dev.apexstudios.registree.api.holder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.gamerules.GameRule;

public class DeferredGameRule<TType> extends DeferredHolder<GameRule<?>, GameRule<TType>> {
    protected DeferredGameRule(ResourceKey<GameRule<?>> registryKey) {
        super(registryKey);
    }
}
