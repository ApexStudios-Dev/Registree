package dev.apexstudios.registree.api.builder;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.IBlockCapabilityProvider;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.apache.commons.lang3.function.Consumers;
import org.jspecify.annotations.Nullable;

public interface IBlockBuilder<TBlock extends Block> extends IBuilder<Block, TBlock, DeferredBlock<TBlock>, IBlockBuilder<TBlock>> {
    IBlockBuilder<TBlock> initialProperties(Function<IBuilderContext<Block>, BlockBehaviour.Properties> initialProperties);

    default IBlockBuilder<TBlock> initialProperties(Supplier<BlockBehaviour.Properties> initialProperties) {
        return initialProperties(context -> initialProperties.get());
    }

    default IBlockBuilder<TBlock> copyLegacyPropertiesFrom(Function<IBuilderContext<Block>, BlockBehaviour> blockSupplier) {
        return initialProperties(context -> BlockBehaviour.Properties.ofLegacyCopy(blockSupplier.apply(context)));
    }

    default IBlockBuilder<TBlock> copyLegacyPropertiesFrom(Supplier<BlockBehaviour> blockSupplier) {
        return copyLegacyPropertiesFrom(context -> blockSupplier.get());
    }

    default IBlockBuilder<TBlock> copyPropertiesFrom(Function<IBuilderContext<Block>, BlockBehaviour> blockSupplier) {
        return initialProperties(context -> BlockBehaviour.Properties.ofFullCopy(blockSupplier.apply(context)));
    }

    default IBlockBuilder<TBlock> copyPropertiesFrom(Supplier<BlockBehaviour> blockSupplier) {
        return copyPropertiesFrom(context -> blockSupplier.get());
    }

    IBlockBuilder<TBlock> properties(BiConsumer<IBuilderContext<Block>, BlockBehaviour.Properties> propertiesModifier);

    default IBlockBuilder<TBlock> properties(Consumer<BlockBehaviour.Properties> propertiesModifier) {
        return properties((context, properties) -> propertiesModifier.accept(properties));
    }

    <TCapability, TContext extends @Nullable Object> IBlockBuilder<TBlock> capability(BlockCapability<TCapability, TContext> capability, IBlockCapabilityProvider<TCapability, TContext> capabilityProvider);

    IBlockBuilder<TBlock> extensions(Supplier<Supplier<IClientBlockExtensions>> clientExtensions);

    <TItem extends Item> IBlockBuilder<TBlock> item(BiFunction<TBlock, Item.Properties, TItem> itemFactory, BiConsumer<IBuilderContext<Block>, IItemBuilder<TItem>> itemBuilder);

    default <TItem extends Item> IBlockBuilder<TBlock> item(BiFunction<TBlock, Item.Properties, TItem> itemFactory, Consumer<IItemBuilder<TItem>> itemBuilder) {
        return item(itemFactory, (context, builder) -> itemBuilder.accept(builder));
    }

    default <TItem extends Item> IBlockBuilder<TBlock> item(BiFunction<TBlock, Item.Properties, TItem> itemFactory) {
        return item(itemFactory, Consumers.nop());
    }

    default IBlockBuilder<TBlock> defaultItem(BiConsumer<IBuilderContext<Block>, IItemBuilder<BlockItem>> itemBuilder) {
        return item(BlockItem::new, itemBuilder);
    }

    default IBlockBuilder<TBlock> defaultItem(Consumer<IItemBuilder<BlockItem>> itemBuilder) {
        return defaultItem((context, builder) -> itemBuilder.accept(builder));
    }

    default IBlockBuilder<TBlock> defaultItem() {
        return defaultItem(Consumers.nop());
    }

    <TBlockEntity extends BlockEntity> IBlockBuilder<TBlock> blockEntity(BlockEntityType.BlockEntitySupplier<TBlockEntity> blockEntityFactory, BiConsumer<IBuilderContext<Block>, IBlockEntityTypeBuilder<TBlockEntity>> blockEntityTypeBuilder);

    default <TBlockEntity extends BlockEntity> IBlockBuilder<TBlock> blockEntity(BlockEntityType.BlockEntitySupplier<TBlockEntity> blockEntityFactory, Consumer<IBlockEntityTypeBuilder<TBlockEntity>> blockEntityTypeBuilder) {
        return blockEntity(blockEntityFactory, (context, builder) -> blockEntityTypeBuilder.accept(builder));
    }

    default <TBlockEntity extends BlockEntity> IBlockBuilder<TBlock> blockEntity(BlockEntityType.BlockEntitySupplier<TBlockEntity> blockEntityFactory) {
        return blockEntity(blockEntityFactory, Consumers.nop());
    }
}
