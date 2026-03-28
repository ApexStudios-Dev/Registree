package dev.apexstudios.registree.xplat.holder;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jspecify.annotations.Nullable;

public final class DeferredBlockEntity<TBlockEntity extends BlockEntity> extends DeferredHolder<BlockEntityType<?>, BlockEntityType<TBlockEntity>> {
    public DeferredBlockEntity(ResourceKey<BlockEntityType<?>> registryKey) {
        super(registryKey);
    }

    public @Nullable TBlockEntity get(BlockGetter level, BlockPos pos) {
        return value().getBlockEntity(level, pos);
    }
}
