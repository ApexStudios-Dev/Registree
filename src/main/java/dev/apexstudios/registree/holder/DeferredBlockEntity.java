package dev.apexstudios.registree.holder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class DeferredBlockEntity<TBlockEntity extends BlockEntity> extends DeferredHolder<BlockEntityType<?>, BlockEntityType<TBlockEntity>> {
    DeferredBlockEntity(ResourceKey<BlockEntityType<?>> registryKey) {
        super(registryKey);
    }
}
