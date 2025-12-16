package dev.apexstudios.registree.core.builder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import dev.apexstudios.registree.api.builder.IBlockEntityTypeBuilder;
import dev.apexstudios.registree.api.builder.IBuilderContext;
import dev.apexstudios.registree.api.holder.DeferredBlockEntityType;
import dev.apexstudios.registree.api.registrar.IBlockEntityTypeRegistrar;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
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

public class BlockEntityTypeBuilder<TBlockEntity extends BlockEntity> extends Builder<BlockEntityType<?>, BlockEntityType<TBlockEntity>, DeferredBlockEntityType<TBlockEntity>, IBlockEntityTypeBuilder<TBlockEntity>> implements IBlockEntityTypeBuilder<TBlockEntity> {
    private final BlockEntityType.BlockEntitySupplier<TBlockEntity> blockEntityFactory;
    private final List<Supplier<? extends Block>> validBlocks = Lists.newArrayList();
    private boolean onlyOpCanSetNbt = false;
    @Nullable private Supplier<Supplier<BlockEntityRendererProvider<TBlockEntity, ? extends BlockEntityRenderState>>> rendererFactory = null;
    private final Map<BlockCapability<?, ?>, ICapabilityProvider<TBlockEntity, ?, ?>> capabilities = Maps.newHashMap();

    @SuppressWarnings({"rawtypes", "unchecked"})
    public BlockEntityTypeBuilder(IBlockEntityTypeRegistrar registrar, String identifier, BlockEntityType.BlockEntitySupplier<TBlockEntity> blockEntityFactory) {
        super(registrar, identifier);

        this.blockEntityFactory = blockEntityFactory;

        onRegister(blockEntityType -> {
            registrar.registree().event(EntityRenderersEvent.RegisterRenderers.class, event -> {
                if(rendererFactory != null) {
                    event.registerBlockEntityRenderer(blockEntityType, rendererFactory.get().get());
                    rendererFactory = null;
                }
            });

            registrar.registree().event(RegisterCapabilitiesEvent.class, event -> {
                capabilities.forEach((capability, capabilityProvider) -> event.registerBlockEntity((BlockCapability) capability, blockEntityType, capabilityProvider));
                capabilities.clear();
            });
        });
    }

    @Override
    protected BlockEntityType<TBlockEntity> createElement(IBuilderContext<BlockEntityType<?>> context) {
        var validBlocks = this.validBlocks.stream().map(Supplier::get).toArray(Block[]::new);
        return new BlockEntityType<>(blockEntityFactory, onlyOpCanSetNbt, validBlocks);
    }

    @Override
    public IBlockEntityTypeBuilder<TBlockEntity> validBlock(Supplier<? extends Block> block) {
        validBlocks.add(block);
        return this;
    }

    @Override
    public IBlockEntityTypeBuilder<TBlockEntity> onlyOpCanSetNbt(boolean onlyOpCanSetNbt) {
        this.onlyOpCanSetNbt = onlyOpCanSetNbt;
        return this;
    }

    @Override
    public <TCapability, TContext> IBlockEntityTypeBuilder<TBlockEntity> capability(BlockCapability<TCapability, TContext> capability, ICapabilityProvider<TBlockEntity, TContext, TCapability> capabilityProvider) {
        capabilities.put(capability, capabilityProvider);
        return this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <TRenderState extends BlockEntityRenderState> IBlockEntityTypeBuilder<TBlockEntity> renderer(Supplier<Supplier<BlockEntityRendererProvider<TBlockEntity, TRenderState>>> rendererFactory) {
        this.rendererFactory = (Supplier) rendererFactory;
        return this;
    }
}
