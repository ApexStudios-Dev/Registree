package dev.apexstudios.registree.builder;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import dev.apexstudios.registree.BaseRegistree;
import dev.apexstudios.registree.holder.Holders;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.client.IItemDecorator;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public final class ItemBuilder<TItem extends Item> extends AbstractBuilder<Item, TItem, DeferredItem<TItem>, ItemBuilder<TItem>> {
    private final Function<Item.Properties, TItem> factory;
    private Supplier<Item.Properties> initialProperties = Item.Properties::new;
    private Function<Item.Properties, Item.Properties> propertiesModifier = Function.identity();
    private final Map<ResourceKey<CreativeModeTab>, RegistryEventHelper.CreativeModeTabAppender<TItem>> creativeModeTabs = new LinkedHashMap<>();
    private final Map<Identifier, CauldronInteraction> cauldronInteractions = new HashMap<>();
    private final List<CauldronInteraction> globalCauldronInteractions = new LinkedList<>();
    private final Multimap<ItemCapability<?, ?>, ICapabilityProvider<ItemStack, ?, ?>> capabilities = MultimapBuilder.hashKeys().linkedListValues().build();
    private final List<IItemDecorator> decorators = new LinkedList<>();
    private @Nullable Supplier<Supplier<IClientItemExtensions>> clientExtension = null;

    @ApiStatus.Internal
    public ItemBuilder(BaseRegistree<?> registree, String identifier, Function<Item.Properties, TItem> factory) {
        super(registree, Registries.ITEM, identifier, Holders::createItem);

        this.factory = factory;
    }

    public ItemBuilder<TItem> initialProperties(Supplier<Item.Properties> initialProperties) {
        this.initialProperties = initialProperties;
        return this;
    }

    public ItemBuilder<TItem> properties(UnaryOperator<Item.Properties> propertiesModifier) {
        this.propertiesModifier = this.propertiesModifier.andThen(propertiesModifier);
        return this;
    }

    public ItemBuilder<TItem> creativeModeTab(ResourceKey<CreativeModeTab> creativeModeTab, RegistryEventHelper.CreativeModeTabAppender<TItem> generator) {
        creativeModeTabs.put(creativeModeTab, generator);
        return this;
    }

    public ItemBuilder<TItem> creativeModeTab(ResourceKey<CreativeModeTab> creativeModeTab, BiConsumer<TItem, CreativeModeTab.Output> generator) {
        return creativeModeTab(creativeModeTab, (item, parameters, output) -> generator.accept(item, output));
    }

    public ItemBuilder<TItem> creativeModeTab(ResourceKey<CreativeModeTab> creativeModeTab, Function<TItem, ItemStack> generator) {
        return creativeModeTab(creativeModeTab, (item, output) -> output.accept(generator.apply(item)));
    }

    public ItemBuilder<TItem> creativeModeTab(ResourceKey<CreativeModeTab> creativeModeTab) {
        return creativeModeTab(creativeModeTab, Item::getDefaultInstance);
    }

    public ItemBuilder<TItem> cauldronInteraction(Identifier dispatcherId, CauldronInteraction interaction) {
        cauldronInteractions.put(dispatcherId, interaction);
        return this;
    }

    public ItemBuilder<TItem> cauldronInteraction(CauldronInteraction interaction) {
        globalCauldronInteractions.add(interaction);
        return this;
    }

    public <TCapability, TContext extends @Nullable Object> ItemBuilder<TItem> capability(ItemCapability<TCapability, TContext> capability, ICapabilityProvider<ItemStack, TContext, TCapability> provider) {
        capabilities.put(capability, provider);
        return this;
    }

    public ItemBuilder<TItem> decorator(IItemDecorator decorator) {
        decorators.add(decorator);
        return this;
    }

    public ItemBuilder<TItem> decorator(IItemDecorator decorator, IItemDecorator... decorators) {
        decorator(decorator);

        for(var dec : decorators) {
            decorator(dec);
        }

        return this;
    }

    public ItemBuilder<TItem> clientExtension(Supplier<Supplier<IClientItemExtensions>> clientExtension) {
        this.clientExtension = clientExtension;
        return this;
    }

    @Override
    protected TItem createValue(ResourceKey<Item> registryKey) {
        return propertiesModifier
                .<Item.Properties>compose(properties -> properties.setId(registryKey))
                .andThen(factory)
                .apply(initialProperties.get());
    }

    @Override
    protected void registerEvents() {
        super.registerEvents();

        RegistryEventHelper.registerItemCapabilities(registree, this::value, capabilities);
        RegistryEventHelper.appendItemToCreativeModeTabs(registree, this::value, creativeModeTabs);
        RegistryEventHelper.registerItemCauldronInteractions(registree, this::value, cauldronInteractions);
        RegistryEventHelper.registerItemGlobalCauldronInteractions(registree, this::value, globalCauldronInteractions);

        if(FMLEnvironment.getDist().isClient()) {
            RegistryClientEventHelper.registerItemDecorations(registree, this::value, decorators);
            RegistryClientEventHelper.registerItemClientExtension(registree, this::value, clientExtension);
        }
    }
}
