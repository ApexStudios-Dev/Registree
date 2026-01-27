package dev.apexstudios.registree.holder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.gamerules.GameRule;
import net.neoforged.neoforge.registries.DeferredHolder;

public class DeferredGameRule<TValue> extends DeferredHolder<GameRule<?>, GameRule<TValue>> {
    protected DeferredGameRule(ResourceKey<GameRule<?>> registryKey) {
        super(registryKey);
    }

    public static <TValue> DeferredGameRule<TValue> createGameRule(ResourceKey<GameRule<?>> registryKey) {
        return new DeferredGameRule<>(registryKey);
    }

    public static <TValue> DeferredGameRule<TValue> createGameRule(Identifier registryName) {
        return createGameRule(ResourceKey.create(Registries.GAME_RULE, registryName));
    }
}
