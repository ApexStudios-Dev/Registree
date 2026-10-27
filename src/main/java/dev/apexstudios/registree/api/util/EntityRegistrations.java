package dev.apexstudios.registree.api.util;

import dev.apexstudios.registree.api.Registree;
import dev.apexstudios.registree.api.holders.DeferredEntityType;
import java.util.function.UnaryOperator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public interface EntityRegistrations {
    @SuppressWarnings("NullableProblems")
    static <TEntity extends Entity> DeferredEntityType<TEntity> register(Registree registree, String identifier, EntityType.EntityFactory<TEntity> factory, MobCategory category, UnaryOperator<EntityType.Builder<TEntity>> properties) {
        return Registrations.registerForHolder(
                registree,
                Registries.ENTITY_TYPE,
                identifier,
                registration -> properties.apply(EntityType.Builder.of(factory, category)).build(registration.registryKey()),
                DeferredEntityType::new
        );
    }

    static <TEntity extends Entity> DeferredEntityType<TEntity> register(Registree registree, String identifier, EntityType.EntityFactory<TEntity> factory, MobCategory category) {
        return register(registree, identifier, factory, category, UnaryOperator.identity());
    }
}
