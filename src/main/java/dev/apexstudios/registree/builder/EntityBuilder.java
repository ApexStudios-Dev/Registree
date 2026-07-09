package dev.apexstudios.registree.builder;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import dev.apexstudios.registree.BaseRegistree;
import dev.apexstudios.registree.holder.DeferredEntity;
import dev.apexstudios.registree.holder.Holders;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public final class EntityBuilder<TEntity extends Entity> extends AbstractBuilder<EntityType<?>, EntityType<TEntity>, DeferredEntity<TEntity>, EntityBuilder<TEntity>> {
    private final EntityType.EntityFactory<TEntity> factory;
    private final MobCategory category;
    private Function<EntityType.Builder<TEntity>, EntityType.Builder<TEntity>> propertiesModifier = Function.identity();
    private @Nullable Supplier<Supplier<EntityRendererProvider<TEntity>>> rendererProvider = null;
    private final Multimap<EntityCapability<?, ?>, ICapabilityProvider<TEntity, ?, ?>> capabilities = MultimapBuilder.linkedHashKeys().linkedListValues().build();
    private RegistryEventHelper.@Nullable SpawnPlacement<TEntity> spawnPlacement = null;
    private @Nullable Identifier spectatorShader = null;

    @ApiStatus.Internal
    public EntityBuilder(BaseRegistree<?> registree, String identifier, EntityType.EntityFactory<TEntity> factory, MobCategory category) {
        super(registree, Registries.ENTITY_TYPE, identifier, Holders::createEntity);

        this.factory = factory;
        this.category = category;
    }

    public EntityBuilder<TEntity> properties(UnaryOperator<EntityType.Builder<TEntity>> propertiesModifier) {
        this.propertiesModifier = this.propertiesModifier.andThen(propertiesModifier);
        return this;
    }

    public EntityBuilder<TEntity> renderer(Supplier<Supplier<EntityRendererProvider<TEntity>>> rendererProvider) {
        this.rendererProvider = rendererProvider;
        return this;
    }

    public <TCapability, TContext extends @Nullable Object> EntityBuilder<TEntity> capability(EntityCapability<TCapability, TContext> capability, ICapabilityProvider<TEntity, TContext, TCapability> provider) {
        capabilities.put(capability, provider);
        return this;
    }

    public EntityBuilder<TEntity> spawnPlacement(Heightmap.Types heightmap, SpawnPlacementType type, SpawnPlacements.SpawnPredicate<TEntity> predicate) {
        spawnPlacement = new RegistryEventHelper.SpawnPlacement<>(heightmap, type, predicate);
        return this;
    }

    public EntityBuilder<TEntity> spawnPlacement(Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<TEntity> predicate) {
        return spawnPlacement(heightmap, SpawnPlacementTypes.NO_RESTRICTIONS, predicate);
    }

    public EntityBuilder<TEntity> spawnPlacement(SpawnPlacementType type, SpawnPlacements.SpawnPredicate<TEntity> predicate) {
        return spawnPlacement(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, type, predicate);
    }

    public EntityBuilder<TEntity> spawnPlacement(SpawnPlacements.SpawnPredicate<TEntity> predicate) {
        return spawnPlacement(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnPlacementTypes.NO_RESTRICTIONS, predicate);
    }

    public EntityBuilder<TEntity> spectatorShader(Identifier spectatorShader) {
        this.spectatorShader = spectatorShader;
        return this;
    }

    public EntityBuilder<TEntity> spectatorShader(String spectatorShader) {
        return spectatorShader(registree.registryName(spectatorShader));
    }

    public EntityBuilder<TEntity> spectatorShader() {
        return spectatorShader(identifier);
    }

    @Override
    protected EntityType<TEntity> createValue(ResourceKey<EntityType<?>> registryKey) {
        return propertiesModifier.apply(EntityType.Builder.of(factory, category)).build(registryKey);
    }

    @Override
    protected void registerEvents() {
        super.registerEvents();

        RegistryEventHelper.registerEntityCapabilities(registree, this::value, capabilities);
        RegistryEventHelper.registerEntitySpawnPlacement(registree, this::value, spawnPlacement);

        if(FMLEnvironment.getDist().isClient()) {
            RegistryClientEventHelper.registerEntityRenderer(registree, this::value, rendererProvider);
            RegistryClientEventHelper.registerEntitySpectatorShader(registree, this::value, spectatorShader);
        }
    }
}
