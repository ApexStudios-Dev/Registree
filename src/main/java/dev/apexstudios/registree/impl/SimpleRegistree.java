package dev.apexstudios.registree.impl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.apexstudios.registree.api.Registree;
import dev.apexstudios.registree.api.holder.ApexDeferredHolder;
import dev.apexstudios.registree.api.holder.DeferredBlock;
import dev.apexstudios.registree.api.holder.DeferredBlockEntity;
import dev.apexstudios.registree.api.holder.DeferredDataComponent;
import dev.apexstudios.registree.api.holder.DeferredEntity;
import dev.apexstudios.registree.api.holder.DeferredFluid;
import dev.apexstudios.registree.api.holder.DeferredFluidType;
import dev.apexstudios.registree.api.holder.DeferredItem;
import dev.apexstudios.registree.api.holder.DeferredMenu;
import dev.apexstudios.registree.api.holder.DeferredParticleType;
import dev.apexstudios.registree.api.holder.DeferredRecipeSerializer;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.commons.lang3.function.Consumers;
import org.jetbrains.annotations.Nullable;
// testing
public class SimpleRegistree implements Registree {
    protected final String namespace;

    private final Table<ResourceKey<? extends Registry<?>>, String, Holder.Reference<?>> holders = HashBasedTable.create();
    private final Table<ResourceKey<? extends Registry<?>>, String, Function<ResourceLocation, ?>> factories = HashBasedTable.create();
    private final Table<ResourceKey<? extends Registry<?>>, String, Consumer<?>> listeners = HashBasedTable.create();
    private final Set<ResourceKey<? extends Registry<?>>> registered = Sets.newHashSet();
    private final Set<ResourceKey<? extends Registry<?>>> finalized = Sets.newHashSet();
    private boolean frozen = false;
    @Nullable private IEventBus modBus = null;
    private Consumer<IEventBus> delayedEventRegistration = Consumers.nop();

    public SimpleRegistree(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public final void registerEvents(IEventBus modBus) {
        if(this.modBus != null)
            return;

        modBus.addListener(RegisterEvent.class, event -> register(event.getRegistry()));
        modBus.addListener(EventPriority.LOW, RegisterEvent.class, event -> invokeListeners(event.getRegistry()));
        modBus.addListener(EventPriority.LOWEST, RegisterEvent.class, event -> frozen = true);
        delayedEventRegistration.accept(modBus);
        delayedEventRegistration = null;
        this.modBus = modBus;
    }

    protected final void withEventBus(Consumer<IEventBus> consumer) {
        if(modBus == null)
            delayedEventRegistration = delayedEventRegistration.andThen(consumer);
        else
            consumer.accept(modBus);
    }

    private <TRegistry> void register(Registry<TRegistry> registry) {
        var registryType = registry.key();

        if(!registered.add(registryType))
            throw new IllegalStateException("Duplicate registry registration: " + namespace + '#' + registryType.location());

        factories.row(registryType).forEach((registryName, factory) -> {
            var fullName = registryName(registryName);
            var holder = Registry.registerForHolder(registry, fullName, (TRegistry) factory.apply(fullName));
            holders.put(registryType, registryName, holder);
        });
    }

    private <TRegistry> void invokeListeners(Registry<TRegistry> registry) {
        var registryType = registry.key();

        if(!registered.contains(registryType))
            throw new IllegalStateException("Can not finalize registry before elements are registered: " + namespace + '#' + registryType.location());
        if(!finalized.add(registryType))
            throw new IllegalStateException("Duplicate registry finalization: " + namespace + '#' + registryType.location());

        listeners.row(registryType).forEach((registryName, listener) -> {
            getOptional(registryType, registryName).ifPresent((Consumer<? super TRegistry>) listener);
        });
    }

    @Override
    public final String namespace() {
        return namespace;
    }

    @Override
    public final Stream<ResourceKey<? extends Registry<?>>> listRegistries() {
        return holders.rowKeySet().stream();
    }

    @Override
    public final <TRegistry> Optional<Holder.Reference<TRegistry>> get(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName) {
        return Optional.ofNullable((Holder.Reference<TRegistry>) holders.get(registryType, registryName));
    }

    @Override
    public final <TRegistry> Stream<Holder.Reference<TRegistry>> listElements(ResourceKey<? extends Registry<TRegistry>> registryType) {
        return holders.row(registryType).values().stream().map(holder -> (Holder.Reference<TRegistry>) holder);
    }

    @Override
    public final <TRegistry> boolean containsKey(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName) {
        return holders.contains(registryType, registryName) || factories.contains(registryType, registryName);
    }

    @Override
    public final <TRegistry> void listenFor(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Consumer<? super TRegistry> listener) {
        if(finalized.contains(registryType))
            getOptional(registryType, registryName).ifPresent(listener);
        else
            listeners.put(registryType, registryName, listener);
    }

    @Override
    public final boolean isRegistered(ResourceKey<? extends Registry<?>> registryType) {
        return frozen || registered.contains(registryType) || finalized.contains(registryType);
    }

    @Override
    public final boolean isRegistered() {
        return frozen;
    }

    @Override
    public final <TRegistry> ResourceKey<TRegistry> register(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Function<ResourceLocation, ? extends TRegistry> factory) {
        if(registered.contains(registryType))
            throw new IllegalStateException("Registree is already frozen: " + namespace + '#' + registryType.location());
        if(factories.put(registryType, registryName, factory) != null)
            throw new IllegalStateException("Duplicate registration: " + registryName + " in registry: " + namespace + '#' + registryType.location());

        return registryKey(registryType, registryName);
    }

    @Override
    public final <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory, FeatureFlagSet requiredFeatures) {
        var holder = registerMenu(registryName, factory, requiredFeatures);
        withEventBus(modBus -> modBus.addListener(EventPriority.LOW, RegisterMenuScreensEvent.class, event -> event.register(holder.value(), screenFactory.get())));
        return holder;
    }

    // region: Finalize methods
    @Override
    public final String registryIdentifier(String registryName) {
        return Registree.super.registryIdentifier(registryName);
    }

    @Override
    public final ResourceLocation registryName(String registryName) {
        return Registree.super.registryName(registryName);
    }

    @Override
    public final <TRegistry> ResourceKey<TRegistry> registryKey(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName) {
        return Registree.super.registryKey(registryType, registryName);
    }

    @Override
    public final <TRegistry> TagKey<TRegistry> tag(ResourceKey<? extends Registry<TRegistry>> registryType, String tagPath) {
        return Registree.super.tag(registryType, tagPath);
    }

    @Override
    public final <TRegistry> Holder.Reference<TRegistry> getOrThrow(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName) {
        return Registree.super.getOrThrow(registryType, registryName);
    }

    @Override
    public final <TRegistry> @Nullable TRegistry getValue(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName) {
        return Registree.super.getValue(registryType, registryName);
    }

    @Override
    public final <TRegistry> Optional<TRegistry> getOptional(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName) {
        return Registree.super.getOptional(registryType, registryName);
    }

    @Override
    public final <TRegistry> TRegistry getValueOrThrow(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName) {
        return Registree.super.getValueOrThrow(registryType, registryName);
    }

    @Override
    public final <TRegistry> Stream<TRegistry> stream(ResourceKey<? extends Registry<TRegistry>> registryType) {
        return Registree.super.stream(registryType);
    }

    @Override
    public final <TRegistry> HolderLookup.RegistryLookup<TRegistry> asLookup(ResourceKey<? extends Registry<TRegistry>> registryType) {
        return Registree.super.asLookup(registryType);
    }

    @Override
    public final <TRegistry> ResourceKey<TRegistry> register(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Supplier<? extends TRegistry> factory) {
        return Registree.super.register(registryType, registryName, factory);
    }

    @Override
    public final <TRegistry, TElement extends TRegistry> TElement registerElement(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Function<ResourceLocation, TElement> factory) {
        return Registree.super.registerElement(registryType, registryName, factory);
    }

    @Override
    public final <TRegistry, TElement extends TRegistry> TElement registerElement(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Supplier<TElement> factory) {
        return Registree.super.registerElement(registryType, registryName, factory);
    }

    @Override
    public final <TRegistry, THolder extends Holder<TRegistry>> THolder registerForHolder(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Function<ResourceLocation, ? extends TRegistry> elementFactory, Function<ResourceKey<TRegistry>, THolder> holderFactory) {
        return Registree.super.registerForHolder(registryType, registryName, elementFactory, holderFactory);
    }

    @Override
    public final <TRegistry, THolder extends Holder<TRegistry>> THolder registerForHolder(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Supplier<? extends TRegistry> elementFactory, Function<ResourceKey<TRegistry>, THolder> holderFactory) {
        return Registree.super.registerForHolder(registryType, registryName, elementFactory, holderFactory);
    }

    @Override
    public final <TRegistry, TElement extends TRegistry> ApexDeferredHolder<TRegistry, TElement> registerForHolder(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Function<ResourceLocation, ? extends TRegistry> factory) {
        return Registree.super.registerForHolder(registryType, registryName, factory);
    }

    @Override
    public final <TRegistry, TElement extends TRegistry> ApexDeferredHolder<TRegistry, TElement> registerForHolder(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Supplier<? extends TRegistry> factory) {
        return Registree.super.registerForHolder(registryType, registryName, factory);
    }

    @Override
    public final <TItem extends Item> DeferredItem<TItem> registerItem(String registryName, Function<Item.Properties, TItem> factory, Supplier<Item.Properties> propertiesFactory) {
        return Registree.super.registerItem(registryName, factory, propertiesFactory);
    }

    @Override
    public final <TItem extends Item> DeferredItem<TItem> registerItem(String registryName, Function<Item.Properties, TItem> factory, UnaryOperator<Item.Properties> propertiesMutator) {
        return Registree.super.registerItem(registryName, factory, propertiesMutator);
    }

    @Override
    public final <TItem extends Item> DeferredItem<TItem> registerItem(String registryName, Function<Item.Properties, TItem> factory, Item.Properties properties) {
        return Registree.super.registerItem(registryName, factory, properties);
    }

    @Override
    public final <TItem extends Item> DeferredItem<TItem> registerItem(String registryName, Function<Item.Properties, TItem> factory) {
        return Registree.super.registerItem(registryName, factory);
    }

    @Override
    public final DeferredItem<Item> registerSimpleItem(String registryName, Supplier<Item.Properties> propertiesFactory) {
        return Registree.super.registerSimpleItem(registryName, propertiesFactory);
    }

    @Override
    public final DeferredItem<Item> registerSimpleItem(String registryName, UnaryOperator<Item.Properties> propertiesMutator) {
        return Registree.super.registerSimpleItem(registryName, propertiesMutator);
    }

    @Override
    public final DeferredItem<Item> registerSimpleItem(String registryName, Item.Properties properties) {
        return Registree.super.registerSimpleItem(registryName, properties);
    }

    @Override
    public final DeferredItem<Item> registerSimpleItem(String registryName) {
        return Registree.super.registerSimpleItem(registryName);
    }

    @Override
    public final <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(String registryName, Supplier<TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, Supplier<Item.Properties> propertiesFactory) {
        return Registree.super.registerBlockItem(registryName, block, factory, propertiesFactory);
    }

    @Override
    public final <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(String registryName, Supplier<TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, UnaryOperator<Item.Properties> propertiesMutator) {
        return Registree.super.registerBlockItem(registryName, block, factory, propertiesMutator);
    }

    @Override
    public final <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(String registryName, Supplier<TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, Item.Properties properties) {
        return Registree.super.registerBlockItem(registryName, block, factory, properties);
    }

    @Override
    public final <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(String registryName, Supplier<TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory) {
        return Registree.super.registerBlockItem(registryName, block, factory);
    }

    @Override
    public final DeferredItem<BlockItem> registerSimpleBlockItem(String registryName, Supplier<? extends Block> block, Supplier<Item.Properties> propertiesFactory) {
        return Registree.super.registerSimpleBlockItem(registryName, block, propertiesFactory);
    }

    @Override
    public final DeferredItem<BlockItem> registerSimpleBlockItem(String registryName, Supplier<? extends Block> block, UnaryOperator<Item.Properties> propertiesMutator) {
        return Registree.super.registerSimpleBlockItem(registryName, block, propertiesMutator);
    }

    @Override
    public final DeferredItem<BlockItem> registerSimpleBlockItem(String registryName, Supplier<? extends Block> block, Item.Properties properties) {
        return Registree.super.registerSimpleBlockItem(registryName, block, properties);
    }

    @Override
    public final DeferredItem<BlockItem> registerSimpleBlockItem(String registryName, Supplier<? extends Block> block) {
        return Registree.super.registerSimpleBlockItem(registryName, block);
    }

    @Override
    public final <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(DeferredHolder<Block, TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, Supplier<Item.Properties> propertiesFactory) {
        return Registree.super.registerBlockItem(block, factory, propertiesFactory);
    }

    @Override
    public final <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(DeferredHolder<Block, TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, UnaryOperator<Item.Properties> propertiesMutator) {
        return Registree.super.registerBlockItem(block, factory, propertiesMutator);
    }

    @Override
    public final <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(DeferredHolder<Block, TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, Item.Properties properties) {
        return Registree.super.registerBlockItem(block, factory, properties);
    }

    @Override
    public final <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(DeferredHolder<Block, TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory) {
        return Registree.super.registerBlockItem(block, factory);
    }

    @Override
    public final DeferredItem<BlockItem> registerSimpleBlockItem(DeferredHolder<Block, ? extends Block> block, Supplier<Item.Properties> propertiesFactory) {
        return Registree.super.registerSimpleBlockItem(block, propertiesFactory);
    }

    @Override
    public final DeferredItem<BlockItem> registerSimpleBlockItem(DeferredHolder<Block, ? extends Block> block, UnaryOperator<Item.Properties> propertiesMutator) {
        return Registree.super.registerSimpleBlockItem(block, propertiesMutator);
    }

    @Override
    public final DeferredItem<BlockItem> registerSimpleBlockItem(DeferredHolder<Block, ? extends Block> block, Item.Properties properties) {
        return Registree.super.registerSimpleBlockItem(block, properties);
    }

    @Override
    public final DeferredItem<BlockItem> registerSimpleBlockItem(DeferredHolder<Block, ? extends Block> block) {
        return Registree.super.registerSimpleBlockItem(block);
    }

    @Override
    public final <TBlock extends Block> DeferredBlock<TBlock> registerBlock(String registryName, Function<BlockBehaviour.Properties, TBlock> factory, Supplier<BlockBehaviour.Properties> propertiesFactory) {
        return Registree.super.registerBlock(registryName, factory, propertiesFactory);
    }

    @Override
    public final <TBlock extends Block> DeferredBlock<TBlock> registerBlock(String registryName, Function<BlockBehaviour.Properties, TBlock> factory, BlockBehaviour.Properties properties) {
        return Registree.super.registerBlock(registryName, factory, properties);
    }

    @Override
    public final DeferredBlock<Block> registerSimpleBlock(String registryName, Supplier<BlockBehaviour.Properties> propertiesFactory) {
        return Registree.super.registerSimpleBlock(registryName, propertiesFactory);
    }

    @Override
    public final DeferredBlock<Block> registerSimpleBlock(String registryName, BlockBehaviour.Properties properties) {
        return Registree.super.registerSimpleBlock(registryName, properties);
    }

    @Override
    public final <TBlockEntity extends BlockEntity> DeferredBlockEntity<TBlockEntity> registerBlockEntity(String registryName, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory, Supplier<? extends Block>... validBlocks) {
        return Registree.super.registerBlockEntity(registryName, factory, validBlocks);
    }

    @Override
    public final <TBlockEntity extends BlockEntity> DeferredBlockEntity<TBlockEntity> registerBlockEntity(DeferredHolder<Block, ?> block, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory, Supplier<? extends Block>... validBlocks) {
        return Registree.super.registerBlockEntity(block, factory, validBlocks);
    }

    @Override
    public final <TBlockEntity extends BlockEntity> DeferredBlockEntity<TBlockEntity> registerBlockEntity(DeferredHolder<Block, ?> block, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory) {
        return Registree.super.registerBlockEntity(block, factory);
    }

    @Override
    public final <TEntity extends Entity> DeferredEntity<TEntity> registerEntity(String registryName, EntityType.EntityFactory<TEntity> factory, MobCategory category, UnaryOperator<EntityType.Builder<TEntity>> propertiesMutator) {
        return Registree.super.registerEntity(registryName, factory, category, propertiesMutator);
    }

    @Override
    public final <TEntity extends Entity> DeferredEntity<TEntity> registerEntity(String registryName, EntityType.EntityFactory<TEntity> factory, MobCategory category) {
        return Registree.super.registerEntity(registryName, factory, category);
    }

    @Override
    public final DeferredItem<SpawnEggItem> registerSpawnEggItem(DeferredHolder<EntityType<?>, EntityType<?>> entityType, Supplier<Item.Properties> propertiesFactory) {
        return Registree.super.registerSpawnEggItem(entityType, propertiesFactory);
    }

    @Override
    public final DeferredItem<SpawnEggItem> registerSpawnEggItem(DeferredHolder<EntityType<?>, EntityType<?>> entityType, UnaryOperator<Item.Properties> propertiesMutator) {
        return Registree.super.registerSpawnEggItem(entityType, propertiesMutator);
    }

    @Override
    public final DeferredItem<SpawnEggItem> registerSpawnEggItem(DeferredHolder<EntityType<?>, EntityType<?>> entityType, Item.Properties properties) {
        return Registree.super.registerSpawnEggItem(entityType, properties);
    }

    @Override
    public final DeferredItem<SpawnEggItem> registerSpawnEggItem(DeferredHolder<EntityType<?>, EntityType<?>> entityType) {
        return Registree.super.registerSpawnEggItem(entityType);
    }

    @Override
    public final <TData> DeferredDataComponent<TData> registerDataComponent(String registryName, UnaryOperator<DataComponentType.Builder<TData>> builder) {
        return Registree.super.registerDataComponent(registryName, builder);
    }

    @Override
    public final <TData> DeferredDataComponent<TData> registerDataComponent(String registryName, Codec<TData> codec, StreamCodec<RegistryFriendlyByteBuf, TData> streamCodec) {
        return Registree.super.registerDataComponent(registryName, codec, streamCodec);
    }

    @Override
    public final <TData> DeferredDataComponent<TData> registerDataComponent(String registryName, Codec<TData> codec, Codec<TData> networkCodec) {
        return Registree.super.registerDataComponent(registryName, codec, networkCodec);
    }

    @Override
    public final <TData> DeferredDataComponent<TData> registerDataComponent(String registryName, Codec<TData> codec) {
        return Registree.super.registerDataComponent(registryName, codec);
    }

    @Override
    public final ResourceKey<CreativeModeTab> registerCreativeModeTab(String registryName, UnaryOperator<CreativeModeTab.Builder> builder) {
        return Registree.super.registerCreativeModeTab(registryName, builder);
    }

    @Override
    public final ResourceKey<CreativeModeTab> registerCreativeModeTab(String registryName, Supplier<ItemStack> icon, CreativeModeTab.DisplayItemsGenerator itemsGenerator) {
        return Registree.super.registerCreativeModeTab(registryName, icon, itemsGenerator);
    }

    @Override
    public final <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, FeatureFlagSet requiredFeatures) {
        return Registree.super.registerMenu(registryName, factory, requiredFeatures);
    }

    @Override
    public final <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, FeatureFlag requiredFeature, FeatureFlag... requiredFeatures) {
        return Registree.super.registerMenu(registryName, factory, requiredFeature, requiredFeatures);
    }

    @Override
    public final <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, FeatureFlag requiredFeature) {
        return Registree.super.registerMenu(registryName, factory, requiredFeature);
    }

    @Override
    public final <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory) {
        return Registree.super.registerMenu(registryName, factory);
    }

    @Override
    public final <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory, FeatureFlagSet requiredFeatures) {
        return Registree.super.registerMenu(registryName, factory, requiredFeatures);
    }

    @Override
    public final <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory, FeatureFlag requiredFeature, FeatureFlag... requiredFeatures) {
        return Registree.super.registerMenu(registryName, factory, requiredFeature, requiredFeatures);
    }

    @Override
    public final <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory, FeatureFlag requiredFeature) {
        return Registree.super.registerMenu(registryName, factory, requiredFeature);
    }

    @Override
    public final <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory) {
        return Registree.super.registerMenu(registryName, factory);
    }

    @Override
    public final <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory, FeatureFlag requiredFeature, FeatureFlag... requiredFeatures) {
        return Registree.super.registerMenu(registryName, factory, screenFactory, requiredFeature, requiredFeatures);
    }

    @Override
    public final <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory, FeatureFlag requiredFeature) {
        return Registree.super.registerMenu(registryName, factory, screenFactory, requiredFeature);
    }

    @Override
    public final <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory) {
        return Registree.super.registerMenu(registryName, factory, screenFactory);
    }

    @Override
    public final <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory, FeatureFlagSet requiredFeatures) {
        return Registree.super.registerMenu(registryName, factory, screenFactory, requiredFeatures);
    }

    @Override
    public final <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory, FeatureFlag requiredFeature, FeatureFlag... requiredFeatures) {
        return Registree.super.registerMenu(registryName, factory, screenFactory, requiredFeature, requiredFeatures);
    }

    @Override
    public final <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory, FeatureFlag requiredFeature) {
        return Registree.super.registerMenu(registryName, factory, screenFactory, requiredFeature);
    }

    @Override
    public final <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory) {
        return Registree.super.registerMenu(registryName, factory, screenFactory);
    }

    @Override
    public final <TFluidType extends FluidType> DeferredFluidType<TFluidType> registerFluidType(String registryName, Function<FluidType.Properties, TFluidType> factory, Supplier<FluidType.Properties> propertiesFactory) {
        return Registree.super.registerFluidType(registryName, factory, propertiesFactory);
    }

    @Override
    public final <TFluidType extends FluidType> DeferredFluidType<TFluidType> registerFluidType(String registryName, Function<FluidType.Properties, TFluidType> factory, UnaryOperator<FluidType.Properties> propertiesMutator) {
        return Registree.super.registerFluidType(registryName, factory, propertiesMutator);
    }

    @Override
    public final <TFluidType extends FluidType> DeferredFluidType<TFluidType> registerFluidType(String registryName, Function<FluidType.Properties, TFluidType> factory, FluidType.Properties properties) {
        return Registree.super.registerFluidType(registryName, factory, properties);
    }

    @Override
    public final <TFluidType extends FluidType> DeferredFluidType<TFluidType> registerFluidType(String registryName, Function<FluidType.Properties, TFluidType> factory) {
        return Registree.super.registerFluidType(registryName, factory);
    }

    @Override
    public final DeferredFluidType<FluidType> registerSimpleFluidType(String registryName, Supplier<FluidType.Properties> propertiesFactory) {
        return Registree.super.registerSimpleFluidType(registryName, propertiesFactory);
    }

    @Override
    public final DeferredFluidType<FluidType> registerSimpleFluidType(String registryName, UnaryOperator<FluidType.Properties> propertiesMutator) {
        return Registree.super.registerSimpleFluidType(registryName, propertiesMutator);
    }

    @Override
    public final DeferredFluidType<FluidType> registerSimpleFluidType(String registryName, FluidType.Properties properties) {
        return Registree.super.registerSimpleFluidType(registryName, properties);
    }

    @Override
    public final DeferredFluidType<FluidType> registerSimpleFluidType(String registryName) {
        return Registree.super.registerSimpleFluidType(registryName);
    }

    @Override
    public final <TFluid extends Fluid> DeferredFluid<TFluid> registerFluid(String registryName, Supplier<TFluid> factory) {
        return Registree.super.registerFluid(registryName, factory);
    }

    @Override
    public final <TRecipe extends Recipe<?>> DeferredRecipeSerializer<TRecipe> registerRecipeSerializer(String registryName, MapCodec<TRecipe> codec, StreamCodec<RegistryFriendlyByteBuf, TRecipe> streamCodec) {
        return Registree.super.registerRecipeSerializer(registryName, codec, streamCodec);
    }

    @Override
    public final <TParticle extends ParticleOptions, TParticleType extends ParticleType<TParticle>> DeferredParticleType<TParticle, TParticleType> registerParticle(String registryName, Supplier<ParticleType<TParticle>> factory) {
        return Registree.super.registerParticle(registryName, factory);
    }

    @Override
    public final <TParticle extends ParticleOptions> DeferredParticleType<TParticle, ParticleType<TParticle>> registerParticle(String registryName, boolean overrideLimiter, MapCodec<TParticle> codec, StreamCodec<? super RegistryFriendlyByteBuf, TParticle> streamCodec) {
        return Registree.super.registerParticle(registryName, overrideLimiter, codec, streamCodec);
    }

    @Override
    public final DeferredParticleType<SimpleParticleType, SimpleParticleType> registerSimpleParticle(String registryName, boolean overrideLimiter) {
        return Registree.super.registerSimpleParticle(registryName, overrideLimiter);
    }

    @Override
    public final <TValue extends GameRules.Value<TValue>> GameRules.Key<TValue> registerGameRule(String registryName, GameRules.Category category, GameRules.Type<TValue> type) {
        return Registree.super.registerGameRule(registryName, category, type);
    }

    @Override
    public final GameRules.Key<GameRules.BooleanValue> registerBooleanGameRule(String registryName, GameRules.Category category, boolean defaultValue, BiConsumer<MinecraftServer, GameRules.BooleanValue> changeListener) {
        return Registree.super.registerBooleanGameRule(registryName, category, defaultValue, changeListener);
    }

    @Override
    public final GameRules.Key<GameRules.BooleanValue> registerBooleanGameRule(String registryName, GameRules.Category category, boolean defaultValue) {
        return Registree.super.registerBooleanGameRule(registryName, category, defaultValue);
    }

    @Override
    public final GameRules.Key<GameRules.IntegerValue> registerIntegerGameRule(String registryName, GameRules.Category category, int defaultValue, BiConsumer<MinecraftServer, GameRules.IntegerValue> changeListener) {
        return Registree.super.registerIntegerGameRule(registryName, category, defaultValue, changeListener);
    }

    @Override
    public final GameRules.Key<GameRules.IntegerValue> registerIntegerGameRule(String registryName, GameRules.Category category, int defaultValue) {
        return Registree.super.registerIntegerGameRule(registryName, category, defaultValue);
    }
    // endregion
}
