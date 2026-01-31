package dev.apexstudios.registree.holder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class DeferredEntityType<TEntity extends Entity> extends DeferredHolder<EntityType<?>, EntityType<TEntity>> {
    protected DeferredEntityType(ResourceKey<EntityType<?>> registryKey) {
        super(registryKey);
    }

    public static <TEntity extends Entity> DeferredEntityType<TEntity> createEntityType(ResourceKey<EntityType<?>> registryKey) {
        return new DeferredEntityType<>(registryKey);
    }

    public static <TEntity extends Entity> DeferredEntityType<TEntity> createEntityType(Identifier registryName) {
        return createEntityType(ResourceKey.create(Registries.ENTITY_TYPE, registryName));
    }
}
