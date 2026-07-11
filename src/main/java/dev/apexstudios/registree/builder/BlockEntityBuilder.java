package dev.apexstudios.registree.builder;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import dev.apexstudios.registree.BaseRegistree;
import dev.apexstudios.registree.holder.DeferredBlockEntity;
import dev.apexstudios.registree.holder.Holders;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public final class BlockEntityBuilder<TBlockEntity extends BlockEntity> extends AbstractBuilder<BlockEntityType<?>, BlockEntityType<TBlockEntity>, DeferredBlockEntity<TBlockEntity>, BlockEntityBuilder<TBlockEntity>> {
    private final BlockEntityType.BlockEntitySupplier<TBlockEntity> factory;
    private final List<Supplier<? extends Block>> validBlocks = new LinkedList<>();
    private boolean onlyOpCanSetNbt = false;
    private @Nullable Supplier<Supplier<BlockEntityRendererProvider<TBlockEntity, ?>>> rendererProvider = null;
    private final Multimap<BlockCapability<?, ?>, ICapabilityProvider<TBlockEntity, ?, ?>> capabilities = MultimapBuilder.linkedHashKeys().linkedListValues().build();

    @ApiStatus.Internal
    public BlockEntityBuilder(BaseRegistree<?> registree, String identifier, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory) {
        super(registree, Registries.BLOCK_ENTITY_TYPE, identifier, Holders::createBlockEntity);

        this.factory = factory;
    }

    public BlockEntityBuilder<TBlockEntity> validBlock(Supplier<? extends Block> block) {
        validBlocks.add(block);
        return this;
    }

    @SafeVarargs
    public final BlockEntityBuilder<TBlockEntity> validBlock(Supplier<? extends Block> block, Supplier<? extends Block>... blocks) {
        Collections.addAll(validBlocks, blocks);
        return validBlock(block);
    }

    public BlockEntityBuilder<TBlockEntity> onlyOpCanSetNbt(boolean onlyOpCanSetNbt) {
        this.onlyOpCanSetNbt = onlyOpCanSetNbt;
        return this;
    }

    public BlockEntityBuilder<TBlockEntity> onlyOpCanSetNbt() {
        return onlyOpCanSetNbt(true);
    }

    public BlockEntityBuilder<TBlockEntity> anyoneCanSetNbt(boolean onlyOpCanSetNbt) {
        return onlyOpCanSetNbt(false);
    }

    public BlockEntityBuilder<TBlockEntity> renderer(Supplier<Supplier<BlockEntityRendererProvider<TBlockEntity, ?>>> rendererProvider) {
        this.rendererProvider = rendererProvider;
        return this;
    }

    public <TCapability, TContext extends @Nullable Object> BlockEntityBuilder<TBlockEntity> capability(BlockCapability<TCapability, TContext> capability, ICapabilityProvider<TBlockEntity, TContext, TCapability> provider) {
        capabilities.put(capability, provider);
        return this;
    }

    @Override
    protected BlockEntityType<TBlockEntity> createValue(ResourceKey<BlockEntityType<?>> registryKey) {
        var validBlocks = this.validBlocks.stream().map(Supplier::get).collect(Collectors.<Block>toUnmodifiableSet());
        return new BlockEntityType<>(factory, validBlocks, onlyOpCanSetNbt);
    }

    @Override
    protected void registerEvents() {
        super.registerEvents();

        RegistryEventHelper.registerBlockEntityCapabilities(registree, this::value, capabilities);
        RegistryClientEventHelper.registerBlockEntityRenderer(registree, this::value, rendererProvider);
    }
}
