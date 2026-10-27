package dev.apexstudios.registree.api.util;

import dev.apexstudios.registree.api.Registree;
import dev.apexstudios.registree.api.holders.DeferredBlockEntityType;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.apache.commons.lang3.ArrayUtils;

public interface BlockEntityRegistrations {
    @SafeVarargs
    static <TBlockEntity extends BlockEntity> DeferredBlockEntityType<TBlockEntity> register(Registree registree, String identifier, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory, boolean onlyOpCanSetNbt, Supplier<? extends Block>... validBlocks) {
        return Registrations.registerForHolder(
                registree,
                Registries.BLOCK_ENTITY_TYPE,
                identifier,
                _ -> new BlockEntityType<>(factory, Stream.of(validBlocks).map(Supplier::get).collect(Collectors.toUnmodifiableSet()), onlyOpCanSetNbt),
                DeferredBlockEntityType::new
        );
    }

    @SafeVarargs
    static <TBlockEntity extends BlockEntity> DeferredBlockEntityType<TBlockEntity> register(Registree registree, String identifier, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory, Supplier<? extends Block>... validBlocks) {
        return register(registree, identifier, factory, false, validBlocks);
    }

    @SafeVarargs
    static <TBlockEntity extends BlockEntity> DeferredBlockEntityType<TBlockEntity> registerForBlock(Registree registree, DeferredHolder<Block, ? extends Block> block, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory, boolean onlyOpCanSetNbt, Supplier<? extends Block>... validBlocks) {
        return register(registree, block.getId().getPath(), factory, onlyOpCanSetNbt, ArrayUtils.add(validBlocks, block));
    }

    @SafeVarargs
    static <TBlockEntity extends BlockEntity> DeferredBlockEntityType<TBlockEntity> registerForBlock(Registree registree, DeferredHolder<Block, ? extends Block> block, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory, Supplier<? extends Block>... validBlocks) {
        return registerForBlock(registree, block, factory, false, validBlocks);
    }

    static <TBlockEntity extends BlockEntity> DeferredBlockEntityType<TBlockEntity> registerForBlock(Registree registree, DeferredHolder<Block, ? extends Block> block, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory, boolean onlyOpCanSetNbt) {
        return register(registree, block.getId().getPath(), factory, onlyOpCanSetNbt, block);
    }

    static <TBlockEntity extends BlockEntity> DeferredBlockEntityType<TBlockEntity> registerForBlock(Registree registree, DeferredHolder<Block, ? extends Block> block, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory) {
        return registerForBlock(registree, block, factory, false);
    }
}
