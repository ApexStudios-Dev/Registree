package dev.apexstudios.registree.xplat.holder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;

public final class DeferredBlock<TBlock extends Block> extends DeferredHolder<Block, TBlock> {
    public DeferredBlock(ResourceKey<Block> registryKey) {
        super(registryKey);
    }
}
