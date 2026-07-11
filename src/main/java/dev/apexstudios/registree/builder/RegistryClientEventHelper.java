package dev.apexstudios.registree.builder;

import com.mojang.datafixers.util.Either;
import dev.apexstudios.registree.BaseRegistree;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.client.renderer.block.BuiltInBlockModels;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.IItemDecorator;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterBlockModelsEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterEntitySpectatorShadersEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.FluidType;
import org.jspecify.annotations.Nullable;

public interface RegistryClientEventHelper {
    static <TExtension> void registerClientExtension(BaseRegistree<?> registree, @Nullable Supplier<@Nullable Supplier<@Nullable TExtension>> extension, BiConsumer<RegisterClientExtensionsEvent, TExtension> registrar) {
        ifPresent(extension, ext -> registree.event(RegisterClientExtensionsEvent.class, event -> registrar.accept(event, ext)));
    }

    static <TExtension, TOwner> void registerClientExtension(BaseRegistree<?> registree, Supplier<TOwner> owner, @Nullable Supplier<@Nullable Supplier<@Nullable TExtension>> extension, CapabilityRegistrar<TExtension, TOwner> registrar) {
        registerClientExtension(registree, extension, (event, ext) -> registrar.register(event, ext, owner.get()));
    }

    static void registerBlockClientExtension(BaseRegistree<?> registree, Supplier<Block> block, @Nullable Supplier<@Nullable Supplier<@Nullable IClientBlockExtensions>> extension) {
        registerClientExtension(registree, block, extension, RegisterClientExtensionsEvent::registerBlock);
    }

    static void registerFluidTypeClientExtension(BaseRegistree<?> registree, Supplier<FluidType> fluidType, @Nullable Supplier<@Nullable Supplier<@Nullable IClientFluidTypeExtensions>> extension) {
        registerClientExtension(registree, fluidType, extension, RegisterClientExtensionsEvent::registerFluidType);
    }

    static void registerItemClientExtension(BaseRegistree<?> registree, ItemLike item, @Nullable Supplier<@Nullable Supplier<@Nullable IClientItemExtensions>> extension) {
        registerClientExtension(registree, item::asItem, extension, RegisterClientExtensionsEvent::registerItem);
    }

    static void registerBlockTintSources(BaseRegistree<?> registree, Supplier<Block> block, @Nullable Supplier<@Nullable Supplier<@Nullable List<BlockTintSource>>> tintSources) {
        if(tintSources == null) {
            return;
        }

        registree.event(RegisterColorHandlersEvent.BlockTintSources.class, event -> ifPresent(tintSources, sources -> {
            if(!sources.isEmpty()) {
                event.register(sources, block.get());
            }
        }));
    }

    static void registerBlockModel(BaseRegistree<?> registree, Supplier<Block> block, @Nullable Supplier<@Nullable Supplier<BuiltInBlockModels.@Nullable ModelFactory>> modelFactory) {
        // nothing to register if initial supplier is null
        if(modelFactory == null) {
            return;
        }

        registree.event(RegisterBlockModelsEvent.class, event -> ifPresent(modelFactory, factory -> event.register(factory, block.get())));
    }

    static void registerFluidModel(BaseRegistree<?> registree, Supplier<Fluid> fluid, @Nullable Supplier<@Nullable Supplier<FluidModel.@Nullable Unbaked>> model) {
        if(model == null) {
            return;
        }

        registree.event(RegisterFluidModelsEvent.class, event -> ifPresent(model, mdl -> event.register(mdl, fluid.get())));
    }

    static <TBlockEntity extends BlockEntity> void registerBlockEntityRenderer(BaseRegistree<?> registree, Supplier<BlockEntityType<TBlockEntity>> blockEntityType, @Nullable Supplier<@Nullable Supplier<@Nullable BlockEntityRendererProvider<TBlockEntity, ?>>> rendererProvider) {
        if(rendererProvider == null) {
            return;
        }

        registree.event(EntityRenderersEvent.RegisterRenderers.class, event -> ifPresent(rendererProvider, provider -> event.registerBlockEntityRenderer(blockEntityType.get(), provider)));
    }

    static <TEntity extends Entity> void registerEntityRenderer(BaseRegistree<?> registree, Supplier<EntityType<TEntity>> entityType, @Nullable Supplier<@Nullable Supplier<@Nullable EntityRendererProvider<TEntity>>> rendererProvider) {
        if(rendererProvider == null) {
            return;
        }

        registree.event(EntityRenderersEvent.RegisterRenderers.class, event -> ifPresent(rendererProvider, provider -> event.registerEntityRenderer(entityType.get(), provider)));
    }

    static void registerEntitySpectatorShader(BaseRegistree<?> registree, Supplier<EntityType<?>> entityType, @Nullable Identifier spectatorShader) {
        if(spectatorShader == null) {
            return;
        }

        registree.event(RegisterEntitySpectatorShadersEvent.class, event -> event.register(entityType.get(), spectatorShader));
    }

    static void registerItemDecorations(BaseRegistree<?> registree, ItemLike item, List<IItemDecorator> decorations) {
        if(decorations.isEmpty()) {
            return;
        }

        var immutable = List.copyOf(decorations);
        registree.event(RegisterItemDecorationsEvent.class, event -> immutable.forEach(decoration -> event.register(item, decoration)));
    }

    static <TMenu extends AbstractContainerMenu> void registerMenuScreenFactory(BaseRegistree<?> registree, Supplier<MenuType<TMenu>> menuType, @Nullable Supplier<@Nullable Supplier<MenuScreens.@Nullable ScreenConstructor<TMenu, ?>>> screenFactory) {
        if(screenFactory == null) {
            return;
        }

        registree.event(RegisterMenuScreensEvent.class, event -> ifPresent(screenFactory, factory -> event.register(menuType.get(), factory)));
    }

    static <TParticleType extends ParticleType<TOptions>, TOptions extends ParticleOptions> void registerParticleProvider(BaseRegistree<?> registree, Supplier<TParticleType> particleType, @Nullable Either<Supplier<@Nullable Supplier<@Nullable ParticleProvider<TOptions>>>, Supplier<@Nullable Supplier<ParticleResources.@Nullable SpriteParticleRegistration<TOptions>>>> provider) {
        if(provider == null) {
            return;
        }

        provider.ifLeft(supplier -> registerParticleProvider(registree, particleType, supplier))
                .ifRight(supplier -> registerParticleSpriteProvider(registree, particleType, supplier));
    }

    static <TParticleType extends ParticleType<TOptions>, TOptions extends ParticleOptions> void registerParticleProvider(BaseRegistree<?> registree, Supplier<TParticleType> particleType, @Nullable Supplier<@Nullable Supplier<@Nullable ParticleProvider<TOptions>>> provider) {
        if(provider == null) {
            return;
        }

        registree.event(RegisterParticleProvidersEvent.class, event -> ifPresent(provider, factory -> event.registerSpecial(particleType.get(), factory)));
    }

    static <TParticleType extends ParticleType<TOptions>, TOptions extends ParticleOptions> void registerParticleSpriteProvider(BaseRegistree<?> registree, Supplier<TParticleType> particleType, @Nullable Supplier<@Nullable Supplier<ParticleResources.@Nullable SpriteParticleRegistration<TOptions>>> provider) {
        if(provider == null) {
            return;
        }

        registree.event(RegisterParticleProvidersEvent.class, event -> ifPresent(provider, factory -> event.registerSpriteSet(particleType.get(), factory)));
    }

    static <T> @Nullable T get(@Nullable Supplier<@Nullable Supplier<@Nullable T>> supplier) {
        if(supplier == null) {
            return null;
        }

        var factory = supplier.get();

        if(factory == null) {
            return null;
        }

        return factory.get();
    }

    static <T> void ifPresent(@Nullable Supplier<@Nullable Supplier<@Nullable T>> supplier, Consumer<T> action) {
        var value = get(supplier);

        if(value != null) {
            action.accept(value);
        }
    }

    @FunctionalInterface
    interface CapabilityRegistrar<TExtension, TOwner> {
        void register(RegisterClientExtensionsEvent event, TExtension extension, TOwner owner);
    }
}
