package dev.apexstudios.registree.holder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class DeferredEntity<TEntity extends Entity> extends DeferredHolder<EntityType<?>, EntityType<TEntity>> {
    DeferredEntity(ResourceKey<EntityType<?>> registryKey) {
        super(registryKey);
    }
}
