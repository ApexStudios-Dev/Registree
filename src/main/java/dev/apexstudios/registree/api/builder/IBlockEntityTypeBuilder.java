package dev.apexstudios.registree.api.builder;

import dev.apexstudios.registree.api.holder.DeferredBlockEntityType;
import java.util.function.Supplier;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import org.jspecify.annotations.Nullable;

public interface IBlockEntityTypeBuilder<TBlockEntity extends BlockEntity> extends IBuilder.WithHolder<BlockEntityType<?>, BlockEntityType<TBlockEntity>, DeferredBlockEntityType<TBlockEntity>, IBlockEntityTypeBuilder<TBlockEntity>> {
    IBlockEntityTypeBuilder<TBlockEntity> validBlock(Supplier<? extends Block> block);

    @SuppressWarnings("unchecked")
    default IBlockEntityTypeBuilder<TBlockEntity> validBlock(Supplier<? extends Block> block, Supplier<? extends Block>... blocks) {
        validBlock(block);

        for(var validBlock : blocks) {
            validBlock(validBlock);
        }

        return this;
    }

    IBlockEntityTypeBuilder<TBlockEntity> onlyOpCanSetNbt(boolean onlyOpCanSetNbt);

    default IBlockEntityTypeBuilder<TBlockEntity> onlyOpCanSetNbt() {
        return onlyOpCanSetNbt(true);
    }

    default IBlockEntityTypeBuilder<TBlockEntity> anyoneCanSetNbt() {
        return onlyOpCanSetNbt(false);
    }

    <TCapability, TContext extends @Nullable Object> IBlockEntityTypeBuilder<TBlockEntity> capability(BlockCapability<TCapability, TContext> capabilityTContextBlockCapability, ICapabilityProvider<TBlockEntity, TContext, TCapability> capabilityProvider);

    <TRenderState extends BlockEntityRenderState> IBlockEntityTypeBuilder<TBlockEntity> renderer(Supplier<Supplier<BlockEntityRendererProvider<TBlockEntity, TRenderState>>> rendererFactory);
}
