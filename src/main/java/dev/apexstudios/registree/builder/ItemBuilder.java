package dev.apexstudios.registree.builder;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import dev.apexstudios.registree.registrar.ItemRegistrar;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.IItemDecorator;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jspecify.annotations.Nullable;

public class ItemBuilder<TItem extends Item> extends Builder<ItemRegistrar, Item, TItem, DeferredItem<TItem>, ItemBuilder.Context<TItem>> {
    private final Function<Item.Properties, TItem> factory;
    private BiConsumer<Context<TItem>, Item.Properties> propertiesModifier = (context, properties) -> { };
    private final Multimap<ItemCapability<?, ?>, ICapabilityProvider<ItemStack, ?, ?>> capabilities = HashMultimap.create();
    private final List<IItemDecorator> decorators = Lists.newArrayList();
    private @Nullable IClientItemExtensions clientExtensions = null;
    private final Map<ResourceKey<CreativeModeTab>, CreativeModeTabAppender<TItem>> creativeModeTabs = Maps.newHashMap();

    public ItemBuilder(ItemRegistrar registrar, String identifier, Function<Item.Properties, TItem> factory) {
        super(registrar, identifier, DeferredItem::createItem, Context::new);

        this.factory = factory;
    }

    public ItemBuilder<TItem> properties(BiConsumer<Context<TItem>, Item.Properties> action) {
        propertiesModifier = propertiesModifier.andThen(action);
        return this;
    }

    public ItemBuilder<TItem> properties(Consumer<Item.Properties> action) {
        return properties((context, properties) -> action.accept(properties));
    }

    public <TCapability, TContext extends @Nullable Object> ItemBuilder<TItem> capability(ItemCapability<TCapability, TContext> capability, ICapabilityProvider<ItemStack, TContext, TCapability> provider) {
        capabilities.put(capability, provider);
        return this;
    }

    @SuppressWarnings("unchecked")
    private <TCapability, TContext extends @Nullable Object> void registerCapability(RegisterCapabilitiesEvent event, Context<TItem> context, ItemCapability<TCapability, TContext> capability) {
        for(var provider : capabilities.get(capability)) {
            event.registerItem(capability, (ICapabilityProvider<ItemStack, TContext, TCapability>) provider, context.holder());
        }
    }

    public ItemBuilder<TItem> decorator(IItemDecorator decorator) {
        decorators.add(decorator);
        return this;
    }

    public ItemBuilder<TItem> clientExtensions(IClientItemExtensions clientExtensions) {
        this.clientExtensions = clientExtensions;
        return this;
    }

    public ItemBuilder<TItem> creativeModeTab(ResourceKey<CreativeModeTab> creativeModeTab, CreativeModeTabAppender<TItem> appender) {
        creativeModeTabs.put(creativeModeTab, appender);
        return this;
    }

    public ItemBuilder<TItem> creativeModeTab(ResourceKey<CreativeModeTab> creativeModeTab, BiConsumer<Context<TItem>, CreativeModeTab.Output> appender) {
        return creativeModeTab(creativeModeTab, (context, parameters, output) -> appender.accept(context, output));
    }

    public ItemBuilder<TItem> creativeModeTab(ResourceKey<CreativeModeTab> creativeModeTab, Function<Context<TItem>, ItemStack> generator) {
        return creativeModeTab(creativeModeTab, (context, output) -> output.accept(generator.apply(context)));
    }

    public ItemBuilder<TItem> creativeModeTab(ResourceKey<CreativeModeTab> creativeModeTab) {
        return creativeModeTab(creativeModeTab, context -> context.asItem().getDefaultInstance());
    }

    @Override
    protected TItem compile(Context<TItem> context) {
        var properties = new Item.Properties().setId(context.registryKey());
        propertiesModifier.accept(context, properties);
        return factory.apply(properties);
    }

    @Override
    protected void finalize(Context<TItem> context) {
        if(!capabilities.isEmpty()) {
            context.registree().event(RegisterCapabilitiesEvent.class, event -> {
                for(var capability : capabilities.keySet()) {
                    registerCapability(event, context, capability);
                }

                capabilities.clear();
            });
        }

        if(!decorators.isEmpty()) {
            context.registree().event(RegisterItemDecorationsEvent.class, event -> {
                decorators.forEach(decorator -> event.register(context.holder(), decorator));
                decorators.clear();
            });
        }

        if(clientExtensions != null) {
            context.registree().event(RegisterClientExtensionsEvent.class, event -> {
                event.registerItem(clientExtensions, context.asItem());
                clientExtensions = null;
            });
        }

        if(!creativeModeTabs.isEmpty()) {
            context.registree().event(BuildCreativeModeTabContentsEvent.class, event -> creativeModeTabs.forEach((creativeModeTab, appender) -> {
                if(event.getTabKey().equals(creativeModeTab)) {
                    appender.accept(context, event.getParameters(), event);
                }
            }));
        }
    }

    public static final class Context<TItem extends Item> extends Builder.Context<ItemRegistrar, Item, TItem, DeferredItem<TItem>> implements ItemLike {
        private Context(ItemRegistrar registrar, DeferredItem<TItem> holder) {
            super(registrar, holder);
        }

        @Override
        public Item asItem() {
            return get();
        }
    }

    @FunctionalInterface
    public interface CreativeModeTabAppender<TItem extends Item> {
        void accept(Context<TItem> context, CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output);
    }
}
