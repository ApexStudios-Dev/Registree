package dev.apexstudios.registree.holder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.gamerules.GameRule;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class DeferredGameRule<TRuleType> extends DeferredHolder<GameRule<?>, GameRule<TRuleType>> {
    DeferredGameRule(ResourceKey<GameRule<?>> registryKey) {
        super(registryKey);
    }
}
