package dev.apexstudios.registree.api.holder;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class DeferredBlockEntity<TBlockEntity extends BlockEntity> extends ApexDeferredHolder<BlockEntityType<?>, BlockEntityType<TBlockEntity>> {
    public DeferredBlockEntity(ResourceKey<BlockEntityType<?>> registryKey) {
        super(registryKey);
    }

    public TBlockEntity get(BlockGetter level, BlockPos pos) {
        return value().getBlockEntity(level, pos);
    }

    public boolean isValid(BlockState blockState) {
        return value().isValid(blockState);
    }

    public boolean isValid(Block block) {
        return value().getValidBlocks().contains(block);
    }
}
