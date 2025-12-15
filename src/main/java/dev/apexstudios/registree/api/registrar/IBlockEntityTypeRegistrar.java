package dev.apexstudios.registree.api.registrar;

import dev.apexstudios.registree.api.builder.IBlockEntityTypeBuilder;
import dev.apexstudios.registree.api.holder.DeferredBlockEntityType;
import dev.apexstudios.registree.core.builder.BlockEntityTypeBuilder;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

public interface IBlockEntityTypeRegistrar extends IRegistrar<BlockEntityType<?>> {
    @SuppressWarnings("unchecked")
    default <TBlockEntity extends BlockEntity> DeferredBlockEntityType<TBlockEntity> registerBlockEntity(String identifier, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory, boolean onlyOpCanSetNbt, Supplier<? extends Block>... validBlocks) {
        Objects.checkIndex(0, validBlocks.length); // at least 1 valid block must exist

        return registerForHolder(identifier, () -> {
            var blocks = Stream.of(validBlocks).map(Supplier::get).toArray(Block[]::new);
            return new BlockEntityType<>(factory, onlyOpCanSetNbt, blocks);
        }, DeferredBlockEntityType::new);
    }

    @SuppressWarnings("unchecked")
    default <TBlockEntity extends BlockEntity> DeferredBlockEntityType<TBlockEntity> registerBlockEntity(String identifier, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory, Supplier<? extends Block>... validBlocks) {
        return registerBlockEntity(identifier, factory, false, validBlocks);
    }

    @SuppressWarnings("unchecked")
    default <TBlockEntity extends BlockEntity> DeferredBlockEntityType<TBlockEntity> registerBlockEntity(DeferredHolder<Block, ? extends Block> block, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory, boolean onlyOpCanSetNbt) {
        return registerBlockEntity(block.getId().getPath(), factory, onlyOpCanSetNbt, block);
    }

    default <TBlockEntity extends BlockEntity> DeferredBlockEntityType<TBlockEntity> registerBlockEntity(DeferredHolder<Block, ? extends Block> block, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory) {
        return registerBlockEntity(block, factory, false);
    }

    default <TBlockEntity extends BlockEntity> IBlockEntityTypeBuilder<TBlockEntity> builder(String identifier, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory) {
        return new BlockEntityTypeBuilder<>(this, identifier, factory);
    }
}
