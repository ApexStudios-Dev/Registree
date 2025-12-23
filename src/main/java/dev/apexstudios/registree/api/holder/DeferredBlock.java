package dev.apexstudios.registree.api.holder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class DeferredBlock<TBlock extends Block> extends ApexDeferredHolder<Block, TBlock> {
    public DeferredBlock(ResourceKey<Block> registryKey) {
        super(registryKey);
    }

    public boolean is(BlockState blockState) {
        return blockState.is(this);
    }
}
