package dev.apexstudios.registree.holder;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jspecify.annotations.Nullable;

public class DeferredBlockEntityType<TBlockEntity extends BlockEntity> extends DeferredHolder<BlockEntityType<?>, BlockEntityType<TBlockEntity>> {
    protected DeferredBlockEntityType(ResourceKey<BlockEntityType<?>> registryKey) {
        super(registryKey);
    }

    public @Nullable TBlockEntity get(BlockGetter level, BlockPos pos) {
        return value().getBlockEntity(level, pos);
    }

    @SuppressWarnings("NullableProblems")
    public Optional<TBlockEntity> find(BlockGetter level, BlockPos pos) {
        return Optional.ofNullable(get(level, pos));
    }

    public static <TBlockEntity extends BlockEntity> DeferredBlockEntityType<TBlockEntity> createBlockEntityType(ResourceKey<BlockEntityType<?>> registryKey) {
        return new DeferredBlockEntityType<>(registryKey);
    }

    public static <TBlockEntity extends BlockEntity> DeferredBlockEntityType<TBlockEntity> createBlockEntityType(Identifier registryName) {
        return createBlockEntityType(ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, registryName));
    }
}
