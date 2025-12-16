package dev.apexstudios.registree.api.builder;

import dev.apexstudios.registree.api.holder.DeferredItem;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.client.IItemDecorator;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jspecify.annotations.Nullable;

public interface IItemBuilder<TItem extends Item> extends IBuilder.WithHolder<Item, TItem, DeferredItem<TItem>, IItemBuilder<TItem>> {
    IItemBuilder<TItem> properties(BiConsumer<IBuilderContext<Item>, Item.Properties> propertiesModifier);

    default IItemBuilder<TItem> properties(Consumer<Item.Properties> propertiesModifier) {
        return properties((context, properties) -> propertiesModifier.accept(properties));
    }

    IItemBuilder<TItem> decorator(Supplier<Supplier<IItemDecorator>> dectoratorFactory);

    IItemBuilder<TItem> extensions(Supplier<Supplier<IClientItemExtensions>> clientExtensions);

    <TCapability, TContext extends @Nullable Object> IItemBuilder<TItem> capability(ItemCapability<TCapability, TContext> capability, ICapabilityProvider<ItemStack, TContext, TCapability> capabilityProvider);
}
