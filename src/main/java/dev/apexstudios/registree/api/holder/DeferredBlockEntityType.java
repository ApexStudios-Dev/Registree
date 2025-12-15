package dev.apexstudios.registree.api.holder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class DeferredBlockEntityType<TBlockEntity extends BlockEntity> extends DeferredHolder<BlockEntityType<?>, BlockEntityType<TBlockEntity>> {
    public DeferredBlockEntityType(ResourceKey<BlockEntityType<?>> registryKey) {
        super(registryKey);
    }
}
