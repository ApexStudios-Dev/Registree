package dev.apexstudios.registree.api.registrar;

import dev.apexstudios.registree.api.builder.IItemBuilder;
import dev.apexstudios.registree.core.builder.ItemBuilder;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import org.apache.commons.lang3.function.Consumers;

public interface IItemRegistrar extends IRegistrar<Item> {
    default <TItem extends Item> DeferredItem<TItem> registerItem(String identifier, Function<Item.Properties, TItem> factory, Consumer<Item.Properties> propertiesModifier) {
        return registerForHolder(identifier, registryName -> {
            var properties = new Item.Properties().setId(registryKey(registryName));
            propertiesModifier.accept(properties);
            return factory.apply(properties);
        }, DeferredItem::createItem);
    }

    default <TItem extends Item> DeferredItem<TItem> registerItem(String identifier, Function<Item.Properties, TItem> factory) {
        return registerItem(identifier, factory, Consumers.nop());
    }

    default DeferredItem<Item> registerItem(String identifier, Consumer<Item.Properties> propertiesModifier) {
        return registerItem(identifier, Item::new, propertiesModifier);
    }

    default DeferredItem<Item> registerItem(String identifier) {
        return registerItem(identifier, Consumers.nop());
    }

    default <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(String identifier, Supplier<TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, Consumer<Item.Properties> propertiesModifier) {
        return registerItem(identifier, properties -> factory.apply(block.get(), properties), propertiesModifier);
    }

    default <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(String identifier, Supplier<TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory) {
        return registerBlockItem(identifier, block, factory, Consumers.nop());
    }

    default DeferredItem<BlockItem> registerBlockItem(String identifier, Supplier<? extends Block> block, Consumer<Item.Properties> propertiesModifier) {
        return registerBlockItem(identifier, block, BlockItem::new, propertiesModifier);
    }

    default DeferredItem<BlockItem> registerBlockItem(String identifier, Supplier<? extends Block> block) {
        return registerBlockItem(identifier, block, Consumers.nop());
    }

    default <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(DeferredHolder<Block, TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, Consumer<Item.Properties> propertiesModifier) {
        return registerBlockItem(block.getId().getPath(), block, factory, propertiesModifier);
    }

    default <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(DeferredHolder<Block, TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory) {
        return registerBlockItem(block, factory, Consumers.nop());
    }

    default DeferredItem<BlockItem> registerBlockItem(DeferredHolder<Block, ? extends Block> block, Consumer<Item.Properties> propertiesModifier) {
        return registerBlockItem(block, BlockItem::new, propertiesModifier);
    }

    default DeferredItem<BlockItem> registerBlockItem(DeferredHolder<Block, ? extends Block> block) {
        return registerBlockItem(block, Consumers.nop());
    }

    default <TItem extends Item> IItemBuilder<TItem> builder(String identifier, Function<Item.Properties, TItem> factory) {
        return new ItemBuilder<>(this, identifier, factory);
    }

    default IItemBuilder<Item> builder(String identifier) {
        return builder(identifier, Item::new);
    }
}
