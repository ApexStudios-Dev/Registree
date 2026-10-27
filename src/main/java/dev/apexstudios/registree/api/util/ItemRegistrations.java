package dev.apexstudios.registree.api.util;

import dev.apexstudios.registree.api.Registree;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public interface ItemRegistrations {
    static <TItem extends Item> DeferredItem<TItem> register(Registree registree, String identifier, Function<Item.Properties, TItem> factory, Supplier<Item.Properties> initialProperties, UnaryOperator<Item.Properties> propertiesModifier) {
        return Registrations.registerForHolder(
                registree,
                Registries.ITEM,
                identifier,
                registration -> factory.apply(propertiesModifier.apply(initialProperties.get().setId(registration.registryKey()))),
                DeferredItem::createItem
        );
    }

    static <TItem extends Item> DeferredItem<TItem> register(Registree registree, String identifier, Function<Item.Properties, TItem> factory, UnaryOperator<Item.Properties> propertiesModifier) {
        return register(registree, identifier, factory, Item.Properties::new, propertiesModifier);
    }

    static <TItem extends Item> DeferredItem<TItem> register(Registree registree, String identifier, Function<Item.Properties, TItem> factory) {
        return register(registree, identifier, factory, UnaryOperator.identity());
    }

    static DeferredItem<Item> register(Registree registree, String identifier, Supplier<Item.Properties> initialProperties, UnaryOperator<Item.Properties> propertiesModifier) {
        return register(registree, identifier, Item::new, initialProperties, propertiesModifier);
    }

    static DeferredItem<Item> register(Registree registree, String identifier, UnaryOperator<Item.Properties> propertiesModifier) {
        return register(registree, identifier, Item.Properties::new, propertiesModifier);
    }

    static DeferredItem<Item> register(Registree registree, String identifier) {
        return register(registree, identifier, UnaryOperator.identity());
    }

    static <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerForBlock(Registree registree, String identifier, Supplier<TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, Supplier<Item.Properties> initialProperties, UnaryOperator<Item.Properties> propertiesModifier) {
        return register(
                registree,
                identifier,
                properties -> factory.apply(block.get(), properties),
                initialProperties,
                properties -> propertiesModifier.apply(properties.useBlockDescriptionPrefix().requiredFeatures(block.get().requiredFeatures()))
        );
    }

    static <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerForBlock(Registree registree, String identifier, Supplier<TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, UnaryOperator<Item.Properties> propertiesModifier) {
        return registerForBlock(registree, identifier, block, factory, Item.Properties::new, propertiesModifier);
    }

    static <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerForBlock(Registree registree, String identifier, Supplier<TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory) {
        return registerForBlock(registree, identifier, block, factory, UnaryOperator.identity());
    }

    static DeferredItem<BlockItem> registerForBlock(Registree registree, String identifier, Supplier<? extends Block> block, Supplier<Item.Properties> initialProperties, UnaryOperator<Item.Properties> propertiesModifier) {
        return registerForBlock(registree, identifier, block, BlockItem::new, initialProperties, propertiesModifier);
    }

    static DeferredItem<BlockItem> registerForBlock(Registree registree, String identifier, Supplier<? extends Block> block, UnaryOperator<Item.Properties> propertiesModifier) {
        return registerForBlock(registree, identifier, block, Item.Properties::new, propertiesModifier);
    }

    static DeferredItem<BlockItem> registerForBlock(Registree registree, String identifier, Supplier<? extends Block> block) {
        return registerForBlock(registree, identifier, block, UnaryOperator.identity());
    }

    static <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerForBlock(Registree registree, DeferredHolder<Block, TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, Supplier<Item.Properties> initialProperties, UnaryOperator<Item.Properties> propertiesModifier) {
        return registerForBlock(registree, block.getId().getPath(), block, factory, initialProperties, propertiesModifier);
    }

    static <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerForBlock(Registree registree, DeferredHolder<Block, TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, UnaryOperator<Item.Properties> propertiesModifier) {
        return registerForBlock(registree, block, factory, Item.Properties::new, propertiesModifier);
    }

    static <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerForBlock(Registree registree, DeferredHolder<Block, TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory) {
        return registerForBlock(registree, block, factory, UnaryOperator.identity());
    }

    static DeferredItem<BlockItem> registerForBlock(Registree registree, DeferredHolder<Block, ? extends Block> block, Supplier<Item.Properties> initialProperties, UnaryOperator<Item.Properties> propertiesModifier) {
        return registerForBlock(registree, block, BlockItem::new, initialProperties, propertiesModifier);
    }

    static DeferredItem<BlockItem> registerForBlock(Registree registree, DeferredHolder<Block, ? extends Block> block, UnaryOperator<Item.Properties> propertiesModifier) {
        return registerForBlock(registree, block, Item.Properties::new, propertiesModifier);
    }

    static DeferredItem<BlockItem> registerForBlock(Registree registree, DeferredHolder<Block, ? extends Block> block) {
        return registerForBlock(registree, block, UnaryOperator.identity());
    }
}
