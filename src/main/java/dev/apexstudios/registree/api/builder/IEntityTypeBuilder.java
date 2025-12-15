package dev.apexstudios.registree.api.builder;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

public interface IEntityTypeBuilder<TEntity extends Entity> extends IBuilder<EntityType<?>, EntityType<TEntity>, DeferredHolder<EntityType<?>, EntityType<TEntity>>, IEntityTypeBuilder<TEntity>> {
}

/*
public final class EntityTypeBuilder<TEntity extends Entity> extends Builder<EntityType<?>, EntityType<TEntity>, DeferredHolder<EntityType<?>, EntityType<TEntity>>, EntityTypeBuilder<TEntity>> {
    private final EntityType.EntityFactory<TEntity> entityFactory;
    private final MobCategory category;
    private BiConsumer<IBuilderContext<EntityType<?>>, EntityType.Builder<TEntity>> propertiesModifier = (context, properties) -> { };
    @Nullable private Supplier<Supplier<EntityRendererProvider<TEntity>>> rendererFactory = null;
    private final Map<EntityCapability<?, ?>, ICapabilityProvider<TEntity, ?, ?>> capabilities = Maps.newHashMap();
    @Nullable private Identifier spectatorShader = null;
    @Nullable private Supplier<AttributeSupplier> attributeFactory = null;
    @Nullable private SpawnPlacement<TEntity> spawnPlacement = null;

    @SuppressWarnings({"rawtypes", "unchecked"})
    @ApiStatus.Internal
    public EntityTypeBuilder(IRegistrar<EntityType<?>> registrar, String identifier, EntityType.EntityFactory<TEntity> entityFactory, MobCategory category) {
        super(registrar, identifier, DeferredHolder::create);

        this.entityFactory = entityFactory;
        this.category = category;

        onRegister(entityType -> {
            registrar.registree().event(EntityRenderersEvent.RegisterRenderers.class, event -> {
                if(rendererFactory != null) {
                    event.registerEntityRenderer(entityType, rendererFactory.get().get());
                    rendererFactory = null;
                }
            });

            registrar.registree().event(RegisterCapabilitiesEvent.class, event -> {
                capabilities.forEach((capability, capabilityProvider) -> event.registerEntity((EntityCapability) capability, entityType, capabilityProvider));
                capabilities.clear();
            });

            registrar.registree().event(RegisterEntitySpectatorShadersEvent.class, event -> {
                if(spectatorShader != null) {
                    event.register(entityType, spectatorShader);
                    spectatorShader = null;
                }
            });

            registrar.registree().event(EntityAttributeCreationEvent.class, event -> {
                if(attributeFactory != null) {
                    event.put((EntityType<? extends LivingEntity>) entityType, attributeFactory.get());
                    attributeFactory = null;
                }
            });

            registrar.registree().event(RegisterSpawnPlacementsEvent.class, event -> {
                if(spawnPlacement != null) {
                    event.register(entityType, spawnPlacement.spawnPlacementType, spawnPlacement.heightmapType, spawnPlacement.spawnPredicate, RegisterSpawnPlacementsEvent.Operation.REPLACE);
                    spawnPlacement = null;
                }
            });
        });
    }

    public EntityTypeBuilder<TEntity> properties(BiConsumer<IBuilderContext<EntityType<?>>, EntityType.Builder<TEntity>> propertiesModifier) {
        this.propertiesModifier = this.propertiesModifier.andThen(propertiesModifier);
        return this;
    }

    public EntityTypeBuilder<TEntity> properties(Consumer<EntityType.Builder<TEntity>> propertiesModifier) {
        return properties((context, properties) -> propertiesModifier.accept(properties));
    }

    public <TCapability, TContext extends @Nullable Object> EntityTypeBuilder<TEntity> capability(EntityCapability<TCapability, TContext> capability, ICapabilityProvider<TEntity, TContext, TCapability> capabilityProvider) {
        capabilities.put(capability, capabilityProvider);
        return this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public EntityTypeBuilder<TEntity> renderer(Supplier<Supplier<EntityRendererProvider<TEntity>>> rendererFactory) {
        this.rendererFactory = (Supplier) rendererFactory;
        return this;
    }

    public EntityTypeBuilder<TEntity> spectatorShader(Identifier spectatorShader) {
        this.spectatorShader = spectatorShader;
        return this;
    }

    public EntityTypeBuilder<TEntity> spectatorShader(String spectatorShader) {
        return spectatorShader(registrar.registryName(spectatorShader));
    }

    public EntityTypeBuilder<TEntity> attribute(Supplier<AttributeSupplier> attributeFactory) {
        this.attributeFactory = attributeFactory;
        return this;
    }

    public EntityTypeBuilder<TEntity> attribute(Consumer<AttributeSupplier.Builder> builder) {
        return attribute(() -> Util.make(AttributeSupplier.builder(), builder).build());
    }

    public EntityTypeBuilder<TEntity> spawnPlacement(SpawnPlacementType spawnPlacementType, Heightmap.Types heightmapType, SpawnPlacements.SpawnPredicate<TEntity> spawnPredicate) {
        spawnPlacement = new SpawnPlacement<>(spawnPlacementType, heightmapType, spawnPredicate);
        return this;
    }

    @Override
    protected EntityType<TEntity> createElement(IBuilderContext<EntityType<?>> context) {
        var builder = EntityType.Builder.of(entityFactory, category);
        propertiesModifier.accept(context, builder);
        return builder.build(context.registryKey());
    }

    private record SpawnPlacement<TEntity extends Entity>(SpawnPlacementType spawnPlacementType, Heightmap.Types heightmapType, SpawnPlacements.SpawnPredicate<TEntity> spawnPredicate) { }
}*/
