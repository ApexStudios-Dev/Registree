package dev.apexstudios.registree.api.holder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class DeferredEntityType<TEntity extends Entity> extends DeferredHolder<EntityType<?>, EntityType<TEntity>> {
    protected DeferredEntityType(ResourceKey<EntityType<?>> registryKey) {
        super(registryKey);
    }
}
