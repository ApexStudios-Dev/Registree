package dev.apexstudios.registree.api.builder;

import dev.apexstudios.registree.api.holder.DeferredEntityType;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import org.apache.commons.lang3.function.Consumers;
import org.jspecify.annotations.Nullable;

public interface IEntityTypeBuilder<TEntity extends Entity> extends IBuilder.WithHolder<EntityType<?>, EntityType<TEntity>, DeferredEntityType<TEntity>, IEntityTypeBuilder<TEntity>> {
    IEntityTypeBuilder<TEntity> properties(BiConsumer<IBuilderContext<EntityType<?>>, EntityType.Builder<TEntity>> propertiesModifier);

    default IEntityTypeBuilder<TEntity> properties(Consumer<EntityType.Builder<TEntity>> propertiesModifier) {
        return properties((context, properties) -> propertiesModifier.accept(properties));
    }

    <TCapability, TContext extends @Nullable Object> IEntityTypeBuilder<TEntity> capability(EntityCapability<TCapability, TContext> capability, ICapabilityProvider<TEntity, TContext, TCapability> capabilityProvider);

    IEntityTypeBuilder<TEntity> renderer(Supplier<Supplier<EntityRendererProvider<TEntity>>> rendererFactory);

    IEntityTypeBuilder<TEntity> spectatorShader(Identifier spectatorShader);

    IEntityTypeBuilder<TEntity> spectatorShader(String spectatorShader);

    IEntityTypeBuilder<TEntity> attributes(Supplier<AttributeSupplier> attributesFactory);

    default IEntityTypeBuilder<TEntity> attributes(Consumer<AttributeSupplier.Builder> attributesBuilder) {
        return attributes(() -> {
            var builder = AttributeSupplier.builder();
            attributesBuilder.accept(builder);
            return builder.build();
        });
    }

    IEntityTypeBuilder<TEntity> spawnPlacement(SpawnPlacementType placementType, Heightmap.Types heightmapType, SpawnPlacements.SpawnPredicate<TEntity> spawnPredicate);

    IEntityTypeBuilder<TEntity> spawnEgg(BiConsumer<IBuilderContext.WithValue<EntityType<?>, EntityType<TEntity>>, IItemBuilder<SpawnEggItem>> itemBuilder);

    default IEntityTypeBuilder<TEntity> spawnEgg(Consumer<IItemBuilder<SpawnEggItem>> itemBuilder) {
        return spawnEgg((context, builder) -> itemBuilder.accept(builder));
    }

    default IEntityTypeBuilder<TEntity> defaultSpawnEgg() {
        return spawnEgg(Consumers.nop());
    }
}
