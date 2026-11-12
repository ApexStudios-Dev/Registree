package dev.apexstudios.registree.api.holders;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.gamerules.GameRule;
import net.neoforged.neoforge.registries.DeferredHolder;

public class DeferredGameRule<TValue> extends DeferredHolder<GameRule<?>, GameRule<TValue>> {
    public DeferredGameRule(ResourceKey<GameRule<?>> registryKey) {
        super(registryKey);
    }
}
