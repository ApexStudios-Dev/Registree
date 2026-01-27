package dev.apexstudios.registree.registrar;

import dev.apexstudios.registree.Registree;
import dev.apexstudios.registree.builder.BlockEntityTypeBuilder;
import dev.apexstudios.registree.holder.DeferredBlockEntityType;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class BlockEntityTypeRegistrar extends Registrar<BlockEntityType<?>> {
    @SuppressWarnings("unchecked")
    private static final Supplier<? extends Block>[] EMPTY = new Supplier[0];

    public BlockEntityTypeRegistrar(Registree registree) {
        super(registree, Registries.BLOCK_ENTITY_TYPE);
    }

    public <TBlockEntity extends BlockEntity> BlockEntityTypeBuilder<TBlockEntity> builder(String identifier, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory) {
        return new BlockEntityTypeBuilder<>(this, identifier, factory);
    }

    @SuppressWarnings("unchecked")
    public <TBlockEntity extends BlockEntity> DeferredBlockEntityType<TBlockEntity> register(String identifier, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory, boolean onlyOpCanSetNbt, Supplier<? extends Block>... validBlocks) {
        return register(identifier, factory, onlyOpCanSetNbt, () -> Stream.of(validBlocks));
    }

    public <TBlockEntity extends BlockEntity> DeferredBlockEntityType<TBlockEntity> register(String identifier, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory, boolean onlyOpCanSetNbt) {
        return register(identifier, factory, onlyOpCanSetNbt, Stream::empty);
    }

    @SuppressWarnings("unchecked")
    public <TBlockEntity extends BlockEntity> DeferredBlockEntityType<TBlockEntity> register(String identifier, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory, Supplier<? extends Block>... validBlocks) {
        return register(identifier, factory, false, validBlocks);
    }

    public <TBlockEntity extends BlockEntity> DeferredBlockEntityType<TBlockEntity> register(String identifier, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory) {
        return register(identifier, factory, false);
    }

    @SuppressWarnings("unchecked")
    public <TBlockEntity extends BlockEntity> DeferredBlockEntityType<TBlockEntity> register(DeferredHolder<Block, ? extends Block> block, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory, boolean onlyOpCanSetNbt, Supplier<? extends Block>... validBlocks) {
        return register(block.getId().getPath(), factory, onlyOpCanSetNbt, () -> Stream.concat(Stream.of(block), Stream.of(validBlocks)));
    }

    public <TBlockEntity extends BlockEntity> DeferredBlockEntityType<TBlockEntity> register(DeferredHolder<Block, ? extends Block> block, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory, boolean onlyOpCanSetNbt) {
        return register(block, factory, onlyOpCanSetNbt, EMPTY);
    }

    @SuppressWarnings("unchecked")
    public <TBlockEntity extends BlockEntity> DeferredBlockEntityType<TBlockEntity> register(DeferredHolder<Block, ? extends Block> block, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory, Supplier<? extends Block>... validBlocks) {
        return register(block, factory, false, validBlocks);
    }

    public <TBlockEntity extends BlockEntity> DeferredBlockEntityType<TBlockEntity> register(DeferredHolder<Block, ? extends Block> block, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory) {
        return register(block, factory, false);
    }

    protected <TBlockEntity extends BlockEntity> DeferredBlockEntityType<TBlockEntity> register(String identifier, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory, boolean onlyOpCanSetNbt, Supplier<Stream<Supplier<? extends Block>>> validBlocks) {
        return registerForHolder(identifier, () -> new BlockEntityType<>(factory, validBlocks.get().map(Supplier::get).collect(Collectors.toSet()), onlyOpCanSetNbt), DeferredBlockEntityType::createBlockEntityType);
    }
}
