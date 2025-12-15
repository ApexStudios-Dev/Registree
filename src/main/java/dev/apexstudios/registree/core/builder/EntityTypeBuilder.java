package dev.apexstudios.registree.core.builder;

import com.google.common.collect.Maps;
import dev.apexstudios.registree.api.builder.IBuilderContext;
import dev.apexstudios.registree.api.builder.IEntityTypeBuilder;
import dev.apexstudios.registree.api.builder.IItemBuilder;
import dev.apexstudios.registree.api.holder.DeferredEntityType;
import dev.apexstudios.registree.api.registrar.IEntityTypeRegistrar;
import dev.apexstudios.registree.api.registrar.IItemRegistrar;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterEntitySpectatorShadersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import org.jspecify.annotations.Nullable;

public class EntityTypeBuilder<TEntity extends Entity> extends Builder<EntityType<?>, EntityType<TEntity>, DeferredEntityType<TEntity>, IEntityTypeBuilder<TEntity>> implements IEntityTypeBuilder<TEntity> {
    private final EntityType.EntityFactory<TEntity> factory;
    private final MobCategory category;
    private BiConsumer<IBuilderContext<EntityType<?>>, EntityType.Builder<TEntity>> propertiesModifier = (context, properties) -> { };
    @Nullable private Supplier<Supplier<EntityRendererProvider<TEntity>>> rendererFactory = null;
    private final Map<EntityCapability<?, ?>, ICapabilityProvider<TEntity, ?, ?>> capabilities = Maps.newHashMap();
    @Nullable private Identifier spectatorShader = null;
    @Nullable private Supplier<AttributeSupplier> attributesFactory = null;
    @Nullable private SpawnPlacement<TEntity> spawnPlacement = null;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public EntityTypeBuilder(IEntityTypeRegistrar registrar, String identifier, EntityType.EntityFactory<TEntity> factory, MobCategory category) {
        super(registrar, identifier, DeferredEntityType::new);

        this.factory = factory;
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
                if(attributesFactory != null) {
                    event.put((EntityType<? extends LivingEntity>) entityType, attributesFactory.get());
                    attributesFactory = null;
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

    @Override
    protected EntityType<TEntity> createElement(IBuilderContext<EntityType<?>> context) {
        var builder = EntityType.Builder.of(factory, category);
        propertiesModifier.accept(context, builder);
        return builder.build(context.registryKey());
    }

    @Override
    public IEntityTypeBuilder<TEntity> properties(BiConsumer<IBuilderContext<EntityType<?>>, EntityType.Builder<TEntity>> propertiesModifier) {
        this.propertiesModifier = this.propertiesModifier.andThen(propertiesModifier);
        return this;
    }

    @Override
    public <TCapability, TContext> IEntityTypeBuilder<TEntity> capability(EntityCapability<TCapability, TContext> capability, ICapabilityProvider<TEntity, TContext, TCapability> capabilityProvider) {
        capabilities.put(capability, capabilityProvider);
        return this;
    }

    @Override
    public IEntityTypeBuilder<TEntity> renderer(Supplier<Supplier<EntityRendererProvider<TEntity>>> rendererFactory) {
        this.rendererFactory = rendererFactory;
        return this;
    }

    @Override
    public IEntityTypeBuilder<TEntity> spectatorShader(Identifier spectatorShader) {
        this.spectatorShader = spectatorShader;
        return this;
    }

    @Override
    public IEntityTypeBuilder<TEntity> spectatorShader(String spectatorShader) {
        return spectatorShader(registrar.registryName(spectatorShader));
    }

    @Override
    public IEntityTypeBuilder<TEntity> attributes(Supplier<AttributeSupplier> attributesFactory) {
        this.attributesFactory = attributesFactory;
        return this;
    }

    @Override
    public IEntityTypeBuilder<TEntity> spawnPlacement(SpawnPlacementType placementType, Heightmap.Types heightmapType, SpawnPlacements.SpawnPredicate<TEntity> spawnPredicate) {
        spawnPlacement = new SpawnPlacement<>(placementType, heightmapType, spawnPredicate);
        return this;
    }

    @Override
    public IEntityTypeBuilder<TEntity> spawnEgg(BiConsumer<IBuilderContext.WithValue<EntityType<?>, EntityType<TEntity>>, IItemBuilder<SpawnEggItem>> itemBuilder) {
        return defer(context -> {
            var builder = registrar.registree().items().builder(context.identifier() + IItemRegistrar.SPAWN_EGG_SUFFIX, SpawnEggItem::new).properties(properties -> properties.spawnEgg(context.value()));
            itemBuilder.accept(context, builder);
            builder.register();
        });
    }

    private record SpawnPlacement<TEntity extends Entity>(SpawnPlacementType spawnPlacementType, Heightmap.Types heightmapType, SpawnPlacements.SpawnPredicate<TEntity> spawnPredicate) {

    }
}
