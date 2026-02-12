package dev.apexstudios.registree.registrar;

import dev.apexstudios.registree.Registree;
import dev.apexstudios.registree.builder.ItemBuilder;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import org.apache.commons.lang3.function.Consumers;

public class ItemRegistrar extends Registrar<Item> {
    public ItemRegistrar(Registree registree) {
        super(registree, Registries.ITEM);
    }

    public <TItem extends Item> ItemBuilder<TItem> builder(String identifier, Function<Item.Properties, TItem> factory) {
        return new ItemBuilder<>(this, identifier, factory);
    }

    public ItemBuilder<Item> builder(String identifier) {
        return builder(identifier, Item::new);
    }

    public <TItem extends Item> DeferredItem<TItem> register(String identifier, Function<Item.Properties, TItem> factory, Consumer<Item.Properties> propertiesModifier) {
        return registerForHolder(identifier, registryName -> {
            var properties = new Item.Properties().setId(registryKey(registryName));
            propertiesModifier.accept(properties);
            return factory.apply(properties);
        }, DeferredItem::createItem);
    }

    public <TItem extends Item> DeferredItem<TItem> register(String identifier, Function<Item.Properties, TItem> factory) {
        return register(identifier, factory, Consumers.nop());
    }

    public DeferredItem<Item> register(String identifier, Consumer<Item.Properties> propertiesModifier) {
        return register(identifier, Item::new, propertiesModifier);
    }

    public DeferredItem<Item> register(String identifier) {
        return register(identifier, Consumers.nop());
    }

    public <TItem extends Item, TBlock extends Block> DeferredItem<TItem> register(String identifier, Supplier<TBlock> blockSupplier, BiFunction<TBlock, Item.Properties, TItem> factory, Consumer<Item.Properties> propertiesModifier) {
        return register(identifier, properties -> factory.apply(blockSupplier.get(), properties), properties -> {
            properties.useBlockDescriptionPrefix();
            propertiesModifier.accept(properties);
        });
    }

    public <TItem extends Item, TBlock extends Block> DeferredItem<TItem> register(String identifier, Supplier<TBlock> blockSupplier, BiFunction<TBlock, Item.Properties, TItem> factory) {
        return register(identifier, blockSupplier, factory, Consumers.nop());
    }

    public DeferredItem<BlockItem> register(String identifier, Supplier<? extends Block> blockSupplier, Consumer<Item.Properties> propertiesModifier) {
        return register(identifier, blockSupplier, BlockItem::new, propertiesModifier);
    }

    public DeferredItem<BlockItem> register(String identifier, Supplier<? extends Block> blockSupplier) {
        return register(identifier, blockSupplier, Consumers.nop());
    }

    public <TItem extends Item, TBlock extends Block> DeferredItem<TItem> register(DeferredHolder<Block, TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, Consumer<Item.Properties> propertiesModifier) {
        return register(block.getId().getPath(), block, factory, propertiesModifier);
    }

    public <TItem extends Item, TBlock extends Block> DeferredItem<TItem> register(DeferredHolder<Block, TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory) {
        return register(block, factory, Consumers.nop());
    }

    public DeferredItem<BlockItem> register(DeferredHolder<Block, ? extends Block> block, Consumer<Item.Properties> propertiesModifier) {
        return register(block, BlockItem::new, propertiesModifier);
    }

    public DeferredItem<BlockItem> register(DeferredHolder<Block, ? extends Block> block) {
        return register(block, Consumers.nop());
    }
}
