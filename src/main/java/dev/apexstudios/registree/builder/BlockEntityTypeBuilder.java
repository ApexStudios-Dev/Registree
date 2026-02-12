package dev.apexstudios.registree.builder;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import dev.apexstudios.registree.holder.DeferredBlockEntityType;
import dev.apexstudios.registree.registrar.BlockEntityTypeRegistrar;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.jspecify.annotations.Nullable;

public class BlockEntityTypeBuilder<TBlockEntity extends BlockEntity> extends Builder<BlockEntityTypeRegistrar, BlockEntityType<?>, BlockEntityType<TBlockEntity>, DeferredBlockEntityType<TBlockEntity>, BlockEntityTypeBuilder.Context<TBlockEntity>, BlockEntityTypeBuilder<TBlockEntity>> {
    private final BlockEntityType.BlockEntitySupplier<TBlockEntity> factory;
    private final Set<Supplier<? extends Block>> validBlocks = Sets.newHashSet();
    private boolean onlyOpCanSetNbt = false;
    private @Nullable Supplier<Supplier<BlockEntityRendererProvider<TBlockEntity, ? extends BlockEntityRenderState>>> rendererProvider = null;
    private final Multimap<BlockCapability<?, ?>, ICapabilityProvider<TBlockEntity, ?, ?>> capabilities = HashMultimap.create();

    public BlockEntityTypeBuilder(BlockEntityTypeRegistrar registrar, String identifier, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory) {
        super(registrar, identifier, DeferredBlockEntityType::createBlockEntityType, Context::new);

        this.factory = factory;
    }

    public BlockEntityTypeBuilder<TBlockEntity> validBlock(Supplier<? extends Block> validBlock) {
        validBlocks.add(validBlock);
        return this;
    }

    public BlockEntityTypeBuilder<TBlockEntity> onlyOpCanSetNbt(boolean onlyOpCanSetNbt) {
        this.onlyOpCanSetNbt = onlyOpCanSetNbt;
        return this;
    }

    public BlockEntityTypeBuilder<TBlockEntity> onlyOpCanSetNbt() {
        return onlyOpCanSetNbt(true);
    }

    public BlockEntityTypeBuilder<TBlockEntity> renderer(Supplier<Supplier<BlockEntityRendererProvider<TBlockEntity, ? extends BlockEntityRenderState>>> rendererProvider) {
        this.rendererProvider = rendererProvider;
        return this;
    }

    public <TCapability, TContext extends @Nullable Object> BlockEntityTypeBuilder<TBlockEntity> capability(BlockCapability<TCapability, TContext> capability, ICapabilityProvider<TBlockEntity, TContext, TCapability> provider) {
        capabilities.put(capability, provider);
        return this;
    }

    @SuppressWarnings("unchecked")
    private <TCapability, TContext extends @Nullable Object> void registerCapability(RegisterCapabilitiesEvent event, Context<TBlockEntity> context, BlockCapability<TCapability, TContext> capability) {
        for(var provider : capabilities.get(capability)) {
            event.registerBlockEntity(capability, context.get(), (ICapabilityProvider<TBlockEntity, TContext, TCapability>) provider);
        }
    }

    @Override
    protected BlockEntityType<TBlockEntity> compile(Context<TBlockEntity> context) {
        return new BlockEntityType<>(factory, validBlocks.stream().map(Supplier::get).collect(Collectors.toSet()), onlyOpCanSetNbt);
    }

    @Override
    protected void finalize(Context<TBlockEntity> context) {
        if(rendererProvider != null) {
            context.registree().event(EntityRenderersEvent.RegisterRenderers.class, event -> {
                event.registerBlockEntityRenderer(context.get(), rendererProvider.get().get());
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
    }

    public static final class Context<TBlockEntity extends BlockEntity> extends Builder.Context<BlockEntityTypeRegistrar, BlockEntityType<?>, BlockEntityType<TBlockEntity>, DeferredBlockEntityType<TBlockEntity>> {
        private Context(BlockEntityTypeRegistrar registrar, DeferredBlockEntityType<TBlockEntity> holder) {
            super(registrar, holder);
        }
    }
}
