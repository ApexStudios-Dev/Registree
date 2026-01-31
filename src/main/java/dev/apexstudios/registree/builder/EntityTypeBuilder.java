package dev.apexstudios.registree.builder;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.apexstudios.registree.holder.DeferredEntityType;
import dev.apexstudios.registree.registrar.EntityTypeRegistrar;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterEntitySpectatorShadersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import org.jspecify.annotations.Nullable;

public class EntityTypeBuilder<TEntity extends Entity> extends Builder<EntityTypeRegistrar, EntityType<?>, EntityType<TEntity>, DeferredEntityType<TEntity>, EntityTypeBuilder.Context<TEntity>> {
    private final EntityType.EntityFactory<TEntity> factory;
    private final MobCategory category;
    private BiConsumer<Context<TEntity>, EntityType.Builder<TEntity>> propertiesModifier = (context, builder) -> { };
    private @Nullable EntityRendererProvider<TEntity> rendererProvider = null;
    private final Multimap<EntityCapability<?, ?>, ICapabilityProvider<TEntity, ?, ?>> capabilities = HashMultimap.create();
    private @Nullable Function<Context<TEntity>, Identifier> spectatorShader = null;
    private @Nullable AttributeSupplier attributes = null;
    private @Nullable SpawnPlacement<TEntity> spawnPlacement = null;

    public EntityTypeBuilder(EntityTypeRegistrar registrar, String identifier, EntityType.EntityFactory<TEntity> factory, MobCategory category) {
        super(registrar, identifier, DeferredEntityType::createEntityType, Context::new);

        this.factory = factory;
        this.category = category;
    }

    public EntityTypeBuilder<TEntity> properties(BiConsumer<Context<TEntity>, EntityType.Builder<TEntity>> propertiesModifier) {
        this.propertiesModifier = this.propertiesModifier.andThen(propertiesModifier);
        return this;
    }

    public EntityTypeBuilder<TEntity> properties(Consumer<EntityType.Builder<TEntity>> propertiesModifier) {
        return properties((context, builder) -> propertiesModifier.accept(builder));
    }

    public EntityTypeBuilder<TEntity> renderer(EntityRendererProvider<TEntity> rendererProvider) {
        this.rendererProvider = rendererProvider;
        return this;
    }

    public <TCapability, TContext extends @Nullable Object> EntityTypeBuilder<TEntity> capability(EntityCapability<TCapability, TContext> capability, ICapabilityProvider<TEntity, TContext, TCapability> provider) {
        capabilities.put(capability, provider);
        return this;
    }

    @SuppressWarnings("unchecked")
    private <TCapability, TContext extends @Nullable Object> void registerCapability(RegisterCapabilitiesEvent event, Context<TEntity> context, EntityCapability<TCapability, TContext> capability) {
        for(var provider : capabilities.get(capability)) {
            event.registerEntity(capability, context.get(), (ICapabilityProvider<TEntity, TContext, TCapability>) provider);
        }
    }

    public EntityTypeBuilder<TEntity> spectatorShader(Identifier spectatorShader) {
        this.spectatorShader = context -> spectatorShader;
        return this;
    }

    public EntityTypeBuilder<TEntity> spectatorShader(String spectatorShader) {
        this.spectatorShader = context -> context.registree().registryName(spectatorShader);
        return this;
    }

    public EntityTypeBuilder<TEntity> attributes(AttributeSupplier attributes) {
        this.attributes = attributes;
        return this;
    }

    public EntityTypeBuilder<TEntity> spawnPlacement(@Nullable SpawnPlacementType type, Heightmap.@Nullable Types heightmap, SpawnPlacements.SpawnPredicate<TEntity> predicate) {
        spawnPlacement = new SpawnPlacement<>(type, heightmap, predicate);
        return this;
    }

    public EntityTypeBuilder<TEntity> spawnPlacement(Heightmap.@Nullable Types heightmap, SpawnPlacements.SpawnPredicate<TEntity> predicate) {
        return spawnPlacement(null, heightmap, predicate);
    }

    public EntityTypeBuilder<TEntity> spawnPlacement(@Nullable SpawnPlacementType type, SpawnPlacements.SpawnPredicate<TEntity> predicate) {
        return spawnPlacement(type, null, predicate);
    }

    public EntityTypeBuilder<TEntity> spawnPlacement(SpawnPlacements.SpawnPredicate<TEntity> predicate) {
        return spawnPlacement(null, null, predicate);
    }

    @Override
    protected EntityType<TEntity> compile(Context<TEntity> context) {
        var builder = EntityType.Builder.of(factory, category);
        propertiesModifier.accept(context, builder);
        return builder.build(context.registryKey());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void finalize(Context<TEntity> context) {
        if(rendererProvider != null) {
            context.registree().event(EntityRenderersEvent.RegisterRenderers.class, event -> {
                event.registerEntityRenderer(context.get(), rendererProvider);
                rendererProvider = null;
            });
        }

        if(!capabilities.isEmpty()) {
            context.registree().event(RegisterCapabilitiesEvent.class, event -> {
                for(var capability : capabilities.keySet()) {
                    registerCapability(event, context, capability);
                }

                capabilities.clear();
            });
        }

        if(spectatorShader != null) {
            context.registree().event(RegisterEntitySpectatorShadersEvent.class, event -> {
                event.register(context.get(), spectatorShader.apply(context));
                spectatorShader = null;
            });
        }

        if(attributes != null) {
            context.registree().event(EntityAttributeCreationEvent.class, event -> {
                event.put((EntityType<? extends LivingEntity>) context.get(), attributes);
                attributes = null;
            });
        }

        if(spawnPlacement != null) {
            context.registree().event(RegisterSpawnPlacementsEvent.class, event -> {
                event.register(context.get(), spawnPlacement.type, spawnPlacement.heightmap, spawnPlacement.predicate, RegisterSpawnPlacementsEvent.Operation.REPLACE);
                spawnPlacement = null;
            });
        }
    }

    public static final class Context<TEntity extends Entity> extends Builder.Context<EntityTypeRegistrar, EntityType<?>, EntityType<TEntity>, DeferredEntityType<TEntity>> {
        private Context(EntityTypeRegistrar registrar, DeferredEntityType<TEntity> holder) {
            super(registrar, holder);
        }
    }

    private record SpawnPlacement<TEntity extends Entity>(@Nullable SpawnPlacementType type, Heightmap.@Nullable Types heightmap, SpawnPlacements.SpawnPredicate<TEntity> predicate) { }
}
