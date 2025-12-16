package dev.apexstudios.registree.core.builder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import dev.apexstudios.registree.api.builder.IBuilderContext;
import dev.apexstudios.registree.api.builder.IItemBuilder;
import dev.apexstudios.registree.api.holder.DeferredItem;
import dev.apexstudios.registree.api.registrar.IItemRegistrar;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.IItemDecorator;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jspecify.annotations.Nullable;

public class ItemBuilder<TItem extends Item> extends Builder<Item, TItem, DeferredItem<TItem>, IItemBuilder<TItem>> implements IItemBuilder<TItem> {
    private BiConsumer<IBuilderContext<Item>, Item.Properties> propertiesModifier = (context, properties) -> properties.setId(context.registryKey());
    private final Function<Item.Properties, TItem> factory;
    private final Map<ItemCapability<?, ?>, ICapabilityProvider<ItemStack, ?, ?>> capabilities = Maps.newHashMap();
    private final List<Supplier<Supplier<IItemDecorator>>> decorators = Lists.newArrayList();
    @Nullable private Supplier<Supplier<IClientItemExtensions>> clientExtensions = null;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ItemBuilder(IItemRegistrar registrar, String identifier, Function<Item.Properties, TItem> factory) {
        super(registrar, identifier);

        this.factory = factory;

         onRegister(item -> {
            registrar.registree().event(RegisterItemDecorationsEvent.class, event -> {
                decorators.forEach(supplier -> event.register(item, supplier.get().get()));
                decorators.clear();
            });

            registrar.registree().event(RegisterCapabilitiesEvent.class, event -> {
                capabilities.forEach((capability, capabilityProvider) -> event.registerItem((ItemCapability) capability, capabilityProvider, item));
                capabilities.clear();
            });

            registrar.registree().event(RegisterClientExtensionsEvent.class, event -> {
                if(clientExtensions != null) {
                    event.registerItem(clientExtensions.get().get(), item);
                    clientExtensions = null;
                }
            });
        });
    }

    @Override
    protected TItem createElement(IBuilderContext<Item> context) {
        var properties = new Item.Properties();
        propertiesModifier.accept(context, properties);
        return factory.apply(properties);
    }

    @Override
    public IItemBuilder<TItem> properties(BiConsumer<IBuilderContext<Item>, Item.Properties> propertiesModifier) {
        this.propertiesModifier = this.propertiesModifier.andThen(propertiesModifier);
        return this;
    }

    @Override
    public IItemBuilder<TItem> decorator(Supplier<Supplier<IItemDecorator>> dectoratorFactory) {
        decorators.add(dectoratorFactory);
        return this;
    }

    @Override
    public IItemBuilder<TItem> extensions(Supplier<Supplier<IClientItemExtensions>> clientExtensions) {
        this.clientExtensions = clientExtensions;
        return this;
    }

    @Override
    public <TCapability, TContext> IItemBuilder<TItem> capability(ItemCapability<TCapability, TContext> capability, ICapabilityProvider<ItemStack, TContext, TCapability> capabilityProvider) {
        capabilities.put(capability, capabilityProvider);
        return this;
    }
}
