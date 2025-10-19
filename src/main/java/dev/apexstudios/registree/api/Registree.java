package dev.apexstudios.registree.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
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
import dev.apexstudios.registree.impl.SimpleRegistree;
import dev.apexstudios.registree.impl.type.SimpleRecipeSerializer;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
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
import net.minecraft.world.flag.FeatureFlags;
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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.NonExtendable
public interface Registree {
    void registerEvents(IEventBus modBus);

    String namespace();

    default String registryIdentifier(String registryName) {
        return namespace() + ResourceLocation.NAMESPACE_SEPARATOR + registryName;
    }

    default ResourceLocation registryName(String registryName) {
        return ResourceLocation.fromNamespaceAndPath(namespace(), registryName);
    }

    default <TRegistry> ResourceKey<TRegistry> registryKey(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName) {
        return ResourceKey.create(registryType, registryName(registryName));
    }

    default <TRegistry> TagKey<TRegistry> tag(ResourceKey<? extends Registry<TRegistry>> registryType, String tagPath) {
        return TagKey.create(registryType, registryName(tagPath));
    }

    Stream<ResourceKey<? extends Registry<?>>> listRegistries();

    // region: Registry
    <TRegistry> Optional<Holder.Reference<TRegistry>> get(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName);

    default <TRegistry> Holder.Reference<TRegistry> getOrThrow(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName) {
        return get(registryType, registryName).orElseThrow(() -> new NoSuchElementException("Missing key in '" + registryType.location() + "': '" + namespace() + ':' + registryName + "'"));
    }

    @Nullable
    default <TRegistry> TRegistry getValue(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName) {
        return get(registryType, registryName).map(Holder::value).orElse(null);
    }

    default <TRegistry> Optional<TRegistry> getOptional(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName) {
        return get(registryType, registryName).map(Holder::value);
    }

    default <TRegistry> TRegistry getValueOrThrow(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName) {
        return getOrThrow(registryType, registryName).value();
    }

    <TRegistry> Stream<Holder.Reference<TRegistry>> listElements(ResourceKey<? extends Registry<TRegistry>> registryType);

    default <TRegistry> Stream<TRegistry> stream(ResourceKey<? extends Registry<TRegistry>> registryType) {
        return listElements(registryType).map(Holder::value);
    }

    <TRegistry> boolean containsKey(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName);

    <TRegistry> void listenFor(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Consumer<? super TRegistry> listener);

    boolean isRegistered(ResourceKey<? extends Registry<?>> registryType);

    boolean isRegistered();

    default <TRegistry> HolderLookup.RegistryLookup<TRegistry> asLookup(ResourceKey<? extends Registry<TRegistry>> registryType) {
        return new HolderLookup.RegistryLookup<>() {
            @Override
            public ResourceKey<? extends Registry<? extends TRegistry>> key() {
                return registryType;
            }

            @Override
            public Lifecycle registryLifecycle() {
                return Lifecycle.stable();
            }

            @Override
            public Stream<Holder.Reference<TRegistry>> listElements() {
                return Registree.this.listElements(registryType);
            }

            @Override
            public Stream<HolderSet.Named<TRegistry>> listTags() {
                return Stream.empty();
            }

            @Override
            public Optional<Holder.Reference<TRegistry>> get(ResourceKey<TRegistry> registryKey) {
                return registryKey.isFor(registryType) && registryKey.location().getNamespace().equals(namespace()) ? Registree.this.get(registryType, registryKey.location().getPath()) : Optional.empty();
            }

            @Override
            public Optional<HolderSet.Named<TRegistry>> get(TagKey<TRegistry> tag) {
                return Optional.empty();
            }

            @Override
            public boolean canSerializeIn(HolderOwner<TRegistry> owner) {
                return false;
            }
        };
    }
    // endregion

    // region: Registrar
    // region: Generic
    <TRegistry> ResourceKey<TRegistry> register(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Function<ResourceLocation, ? extends TRegistry> factory);

    default <TRegistry> ResourceKey<TRegistry> register(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Supplier<? extends TRegistry> factory) {
        return register(registryType, registryName, $ -> factory.get());
    }

    default <TRegistry, TElement extends TRegistry> TElement registerElement(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Function<ResourceLocation, TElement> factory) {
        var element = factory.apply(registryName(registryName));
        register(registryType, registryName, () -> element);
        return element;
    }

    default <TRegistry, TElement extends TRegistry> TElement registerElement(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Supplier<TElement> factory) {
        return registerElement(registryType, registryName, $ -> factory.get());
    }

    default <TRegistry, THolder extends Holder<TRegistry>> THolder registerForHolder(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Function<ResourceLocation, ? extends TRegistry> elementFactory, Function<ResourceKey<TRegistry>, THolder> holderFactory) {
        var registryKey = register(registryType, registryName, elementFactory);
        return holderFactory.apply(registryKey);
    }

    default <TRegistry, THolder extends Holder<TRegistry>> THolder registerForHolder(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Supplier<? extends TRegistry> elementFactory, Function<ResourceKey<TRegistry>, THolder> holderFactory) {
        return registerForHolder(registryType, registryName, $ -> elementFactory.get(), holderFactory);
    }

    default <TRegistry, TElement extends TRegistry> ApexDeferredHolder<TRegistry, TElement> registerForHolder(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Function<ResourceLocation, ? extends TRegistry> factory) {
        return registerForHolder(registryType, registryName, factory, ApexDeferredHolder::new);
    }

    default <TRegistry, TElement extends TRegistry> ApexDeferredHolder<TRegistry, TElement> registerForHolder(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Supplier<? extends TRegistry> factory) {
        return registerForHolder(registryType, registryName, $ -> factory.get());
    }
    // endregion

    // region: Item
    default <TItem extends Item> DeferredItem<TItem> registerItem(String registryName, Function<Item.Properties, TItem> factory, Supplier<Item.Properties> propertiesFactory) {
        return registerForHolder(Registries.ITEM, registryName, $ -> factory.apply(propertiesFactory.get().setId(registryKey(Registries.ITEM, registryName))), DeferredItem::new);
    }

    default <TItem extends Item> DeferredItem<TItem> registerItem(String registryName, Function<Item.Properties, TItem> factory, UnaryOperator<Item.Properties> propertiesMutator) {
        return registerItem(registryName, factory, () -> propertiesMutator.apply(new Item.Properties()));
    }

    default <TItem extends Item> DeferredItem<TItem> registerItem(String registryName, Function<Item.Properties, TItem> factory, Item.Properties properties) {
        return registerItem(registryName, factory, () -> properties);
    }

    default <TItem extends Item> DeferredItem<TItem> registerItem(String registryName, Function<Item.Properties, TItem> factory) {
        return registerItem(registryName, factory, Item.Properties::new);
    }

    default DeferredItem<Item> registerSimpleItem(String registryName, Supplier<Item.Properties> propertiesFactory) {
        return registerItem(registryName, Item::new, propertiesFactory);
    }

    default DeferredItem<Item> registerSimpleItem(String registryName, UnaryOperator<Item.Properties> propertiesMutator) {
        return registerSimpleItem(registryName, () -> propertiesMutator.apply(new Item.Properties()));
    }

    default DeferredItem<Item> registerSimpleItem(String registryName, Item.Properties properties) {
        return registerSimpleItem(registryName, () -> properties);
    }

    default DeferredItem<Item> registerSimpleItem(String registryName) {
        return registerSimpleItem(registryName, Item.Properties::new);
    }

    default <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(String registryName, Supplier<TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, Supplier<Item.Properties> propertiesFactory) {
        return registerItem(registryName, properties -> factory.apply(block.get(), properties.useBlockDescriptionPrefix()), propertiesFactory);
    }

    default <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(String registryName, Supplier<TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, UnaryOperator<Item.Properties> propertiesMutator) {
        return registerBlockItem(registryName, block, factory, () -> propertiesMutator.apply(new Item.Properties()));
    }

    default <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(String registryName, Supplier<TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, Item.Properties properties) {
        return registerBlockItem(registryName, block, factory, () -> properties);
    }

    default <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(String registryName, Supplier<TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory) {
        return registerBlockItem(registryName, block, factory, Item.Properties::new);
    }

    default DeferredItem<BlockItem> registerSimpleBlockItem(String registryName, Supplier<? extends Block> block, Supplier<Item.Properties> propertiesFactory) {
        return registerBlockItem(registryName, block, BlockItem::new, propertiesFactory);
    }

    default DeferredItem<BlockItem> registerSimpleBlockItem(String registryName, Supplier<? extends Block> block, UnaryOperator<Item.Properties> propertiesMutator) {
        return registerSimpleBlockItem(registryName, block, () -> propertiesMutator.apply(new Item.Properties()));
    }

    default DeferredItem<BlockItem> registerSimpleBlockItem(String registryName, Supplier<? extends Block> block, Item.Properties properties) {
        return registerSimpleBlockItem(registryName, block, () -> properties);
    }

    default DeferredItem<BlockItem> registerSimpleBlockItem(String registryName, Supplier<? extends Block> block) {
        return registerSimpleBlockItem(registryName, block, Item.Properties::new);
    }

    default <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(DeferredHolder<Block, TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, Supplier<Item.Properties> propertiesFactory) {
        return registerBlockItem(block.getId().getPath(), block, factory, propertiesFactory);
    }

    default <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(DeferredHolder<Block, TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, UnaryOperator<Item.Properties> propertiesMutator) {
        return registerBlockItem(block, factory, () -> propertiesMutator.apply(new Item.Properties()));
    }

    default <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(DeferredHolder<Block, TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, Item.Properties properties) {
        return registerBlockItem(block, factory, () -> properties);
    }

    default <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(DeferredHolder<Block, TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory) {
        return registerBlockItem(block, factory, Item.Properties::new);
    }

    default DeferredItem<BlockItem> registerSimpleBlockItem(DeferredHolder<Block, ? extends Block> block, Supplier<Item.Properties> propertiesFactory) {
        return registerSimpleBlockItem(block.getId().getPath(), block, propertiesFactory);
    }

    default DeferredItem<BlockItem> registerSimpleBlockItem(DeferredHolder<Block, ? extends Block> block, UnaryOperator<Item.Properties> propertiesMutator) {
        return registerSimpleBlockItem(block, () -> propertiesMutator.apply(new Item.Properties()));
    }

    default DeferredItem<BlockItem> registerSimpleBlockItem(DeferredHolder<Block, ? extends Block> block, Item.Properties properties) {
        return registerSimpleBlockItem(block, () -> properties);
    }

    default DeferredItem<BlockItem> registerSimpleBlockItem(DeferredHolder<Block, ? extends Block> block) {
        return registerSimpleBlockItem(block, Item.Properties::new);
    }
    // endregion

    // region: Block
    default <TBlock extends Block> DeferredBlock<TBlock> registerBlock(String registryName, Function<BlockBehaviour.Properties, TBlock> factory, Supplier<BlockBehaviour.Properties> propertiesFactory) {
        return registerForHolder(Registries.BLOCK, registryName, $ -> factory.apply(propertiesFactory.get().setId(registryKey(Registries.BLOCK, registryName))), DeferredBlock::new);
    }

    default <TBlock extends Block> DeferredBlock<TBlock> registerBlock(String registryName, Function<BlockBehaviour.Properties, TBlock> factory, BlockBehaviour.Properties properties) {
        return registerBlock(registryName, factory, () -> properties);
    }

    default DeferredBlock<Block> registerSimpleBlock(String registryName, Supplier<BlockBehaviour.Properties> propertiesFactory) {
        return registerBlock(registryName, Block::new, propertiesFactory);
    }

    default DeferredBlock<Block> registerSimpleBlock(String registryName, BlockBehaviour.Properties properties) {
        return registerSimpleBlock(registryName, () -> properties);
    }
    // endregion

    // region: BlockEntity
    default <TBlockEntity extends BlockEntity> DeferredBlockEntity<TBlockEntity> registerBlockEntity(String registryName, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory, Supplier<? extends Block>... validBlocks) {
        return registerForHolder(Registries.BLOCK_ENTITY_TYPE, registryName, () -> {
            var blocks = Stream.of(validBlocks).map(Supplier::get).collect(Collectors.<Block>toSet());
            return new BlockEntityType<>(factory, blocks);
        }, DeferredBlockEntity::new);
    }

    default <TBlockEntity extends BlockEntity> DeferredBlockEntity<TBlockEntity> registerBlockEntity(DeferredHolder<Block, ?> block, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory, Supplier<? extends Block>... validBlocks) {
        return registerBlockEntity(block.getId().getPath(), factory, ArrayUtils.add(validBlocks, block));
    }

    default <TBlockEntity extends BlockEntity> DeferredBlockEntity<TBlockEntity> registerBlockEntity(DeferredHolder<Block, ?> block, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory) {
        return registerBlockEntity(block.getId().getPath(), factory, block);
    }
    // endregion

    // region: Entity
    default <TEntity extends Entity> DeferredEntity<TEntity> registerEntity(String registryName, EntityType.EntityFactory<TEntity> factory, MobCategory category, UnaryOperator<EntityType.Builder<TEntity>> propertiesMutator) {
        return registerForHolder(Registries.ENTITY_TYPE, registryName, $ -> propertiesMutator.apply(EntityType.Builder.of(factory, category)).build(registryKey(Registries.ENTITY_TYPE, registryName)), DeferredEntity::new);
    }

    default <TEntity extends Entity> DeferredEntity<TEntity> registerEntity(String registryName, EntityType.EntityFactory<TEntity> factory, MobCategory category) {
        return registerEntity(registryName, factory, category, UnaryOperator.identity());
    }

    default DeferredItem<SpawnEggItem> registerSpawnEggItem(DeferredHolder<EntityType<?>, EntityType<?>> entityType, Supplier<Item.Properties> propertiesFactory) {
        return registerItem(entityType.getId().getPath() + "_spawn_egg", properties -> new SpawnEggItem(properties.spawnEgg(entityType.value())), propertiesFactory);
    }

    default DeferredItem<SpawnEggItem> registerSpawnEggItem(DeferredHolder<EntityType<?>, EntityType<?>> entityType, UnaryOperator<Item.Properties> propertiesMutator) {
        return registerSpawnEggItem(entityType, () -> propertiesMutator.apply(new Item.Properties()));
    }

    default DeferredItem<SpawnEggItem> registerSpawnEggItem(DeferredHolder<EntityType<?>, EntityType<?>> entityType, Item.Properties properties) {
        return registerSpawnEggItem(entityType, () -> properties);
    }

    default DeferredItem<SpawnEggItem> registerSpawnEggItem(DeferredHolder<EntityType<?>, EntityType<?>> entityType) {
        return registerSpawnEggItem(entityType, Item.Properties::new);
    }
    // endregion

    // region: DataComponent
    default <TData> DeferredDataComponent<TData> registerDataComponent(String registryName, UnaryOperator<DataComponentType.Builder<TData>> builder) {
        return registerForHolder(Registries.DATA_COMPONENT_TYPE, registryName, () -> builder.apply(DataComponentType.builder()).build(), DeferredDataComponent::new);
    }

    default <TData> DeferredDataComponent<TData> registerDataComponent(String registryName, Codec<TData> codec, StreamCodec<RegistryFriendlyByteBuf, TData> streamCodec) {
        return registerDataComponent(registryName, builder -> builder.persistent(codec).networkSynchronized(streamCodec));
    }

    default <TData> DeferredDataComponent<TData> registerDataComponent(String registryName, Codec<TData> codec, Codec<TData> networkCodec) {
        return registerDataComponent(registryName, builder -> builder.persistent(codec).networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(networkCodec)));
    }

    default <TData> DeferredDataComponent<TData> registerDataComponent(String registryName, Codec<TData> codec) {
        return registerDataComponent(registryName, builder -> builder.persistent(codec));
    }
    // endregion

    // region: CreativeModeTab
    default ResourceKey<CreativeModeTab> registerCreativeModeTab(String registryName, UnaryOperator<CreativeModeTab.Builder> builder) {
        return register(Registries.CREATIVE_MODE_TAB, registryName, () -> builder.apply(CreativeModeTab.builder().title(Component.translatable(creativeModeTabKey(registryName(registryName))))).build());
    }

    default ResourceKey<CreativeModeTab> registerCreativeModeTab(String registryName, Supplier<ItemStack> icon, CreativeModeTab.DisplayItemsGenerator itemsGenerator) {
        return registerCreativeModeTab(registryName, builder -> builder.icon(icon).displayItems(itemsGenerator));
    }
    // endregion

    // region: Menu
    default <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, FeatureFlagSet requiredFeatures) {
        return registerForHolder(Registries.MENU, registryName, () -> new MenuType<>(factory, requiredFeatures), DeferredMenu::new);
    }

    default <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, FeatureFlag requiredFeature, FeatureFlag... requiredFeatures) {
        return registerMenu(registryName, factory, FeatureFlagSet.of(requiredFeature, requiredFeatures));
    }

    default <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, FeatureFlag requiredFeature) {
        return registerMenu(registryName, factory, FeatureFlagSet.of(requiredFeature));
    }

    default <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory) {
        return registerMenu(registryName, factory, FeatureFlags.VANILLA_SET);
    }

    default <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory, FeatureFlagSet requiredFeatures) {
        return registerMenu(registryName, (MenuType.MenuSupplier<TMenu>) factory, requiredFeatures);
    }

    default <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory, FeatureFlag requiredFeature, FeatureFlag... requiredFeatures) {
        return registerMenu(registryName, factory, FeatureFlagSet.of(requiredFeature, requiredFeatures));
    }

    default <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory, FeatureFlag requiredFeature) {
        return registerMenu(registryName, factory, FeatureFlagSet.of(requiredFeature));
    }

    default <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory) {
        return registerMenu(registryName, factory, FeatureFlags.VANILLA_SET);
    }

    <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory, FeatureFlagSet requiredFeatures);

    default <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory, FeatureFlag requiredFeature, FeatureFlag... requiredFeatures) {
        return registerMenu(registryName, factory, screenFactory, FeatureFlagSet.of(requiredFeature, requiredFeatures));
    }

    default <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory, FeatureFlag requiredFeature) {
        return registerMenu(registryName, factory, screenFactory, FeatureFlagSet.of(requiredFeature));
    }

    default <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory) {
        return registerMenu(registryName, factory, screenFactory, FeatureFlags.VANILLA_SET);
    }

    default <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory, FeatureFlagSet requiredFeatures) {
        return registerMenu(registryName, (MenuType.MenuSupplier<TMenu>) factory, screenFactory, requiredFeatures);
    }

    default <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory, FeatureFlag requiredFeature, FeatureFlag... requiredFeatures) {
        return registerMenu(registryName, factory, screenFactory, FeatureFlagSet.of(requiredFeature, requiredFeatures));
    }

    default <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory, FeatureFlag requiredFeature) {
        return registerMenu(registryName, factory, screenFactory, FeatureFlagSet.of(requiredFeature));
    }

    default <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory) {
        return registerMenu(registryName, factory, screenFactory, FeatureFlags.VANILLA_SET);
    }
    // endregion

    // region: FluidType
    default <TFluidType extends FluidType> DeferredFluidType<TFluidType> registerFluidType(String registryName, Function<FluidType.Properties, TFluidType> factory, Supplier<FluidType.Properties> propertiesFactory) {
        return registerForHolder(NeoForgeRegistries.Keys.FLUID_TYPES, registryName, $ -> factory.apply(propertiesFactory.get()), DeferredFluidType::new);
    }

    default <TFluidType extends FluidType> DeferredFluidType<TFluidType> registerFluidType(String registryName, Function<FluidType.Properties, TFluidType> factory, UnaryOperator<FluidType.Properties> propertiesMutator) {
        return registerFluidType(registryName, factory, () -> propertiesMutator.apply(FluidType.Properties.create()));
    }

    default <TFluidType extends FluidType> DeferredFluidType<TFluidType> registerFluidType(String registryName, Function<FluidType.Properties, TFluidType> factory, FluidType.Properties properties) {
        return registerFluidType(registryName, factory, () -> properties);
    }

    default <TFluidType extends FluidType> DeferredFluidType<TFluidType> registerFluidType(String registryName, Function<FluidType.Properties, TFluidType> factory) {
        return registerFluidType(registryName, factory, FluidType.Properties::create);
    }

    default DeferredFluidType<FluidType> registerSimpleFluidType(String registryName, Supplier<FluidType.Properties> propertiesFactory) {
        return registerFluidType(registryName, FluidType::new, propertiesFactory);
    }

    default DeferredFluidType<FluidType> registerSimpleFluidType(String registryName, UnaryOperator<FluidType.Properties> propertiesMutator) {
        return registerSimpleFluidType(registryName, () -> propertiesMutator.apply(FluidType.Properties.create()));
    }

    default DeferredFluidType<FluidType> registerSimpleFluidType(String registryName, FluidType.Properties properties) {
        return registerSimpleFluidType(registryName, () -> properties);
    }

    default DeferredFluidType<FluidType> registerSimpleFluidType(String registryName) {
        return registerSimpleFluidType(registryName, FluidType.Properties::create);
    }
    // endregion

    // region: Fluid
    default <TFluid extends Fluid> DeferredFluid<TFluid> registerFluid(String registryName, Supplier<TFluid> factory) {
        return registerForHolder(Registries.FLUID, registryName, factory, DeferredFluid::new);
    }
    // endregion

    // region: RecipeSerializer
    default <TRecipe extends Recipe<?>> DeferredRecipeSerializer<TRecipe> registerRecipeSerializer(String registryName, MapCodec<TRecipe> codec, StreamCodec<RegistryFriendlyByteBuf, TRecipe> streamCodec) {
        return registerForHolder(Registries.RECIPE_SERIALIZER, registryName, () -> new SimpleRecipeSerializer<>(codec, streamCodec), DeferredRecipeSerializer::new);
    }
    // endregion

    // region: ParticleType
    default <TParticle extends ParticleOptions, TParticleType extends ParticleType<TParticle>> DeferredParticleType<TParticle, TParticleType> registerParticle(String registryName, Supplier<ParticleType<TParticle>> factory) {
        return registerForHolder(Registries.PARTICLE_TYPE, registryName, factory, DeferredParticleType::new);
    }

    default <TParticle extends ParticleOptions> DeferredParticleType<TParticle, ParticleType<TParticle>> registerParticle(String registryName, boolean overrideLimiter, MapCodec<TParticle> codec, StreamCodec<? super RegistryFriendlyByteBuf, TParticle> streamCodec) {
        return registerParticle(registryName, () -> new ParticleType<>(overrideLimiter) {
            @Override
            public MapCodec<TParticle> codec() {
                return codec;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, TParticle> streamCodec() {
                return streamCodec;
            }
        });
    }

    default DeferredParticleType<SimpleParticleType, SimpleParticleType> registerSimpleParticle(String registryName, boolean overrideLimiter) {
        return registerParticle(registryName, () -> new SimpleParticleType(overrideLimiter));
    }
    // endregion
    // endregion

    // region: GameRules
    default <TValue extends GameRules.Value<TValue>> GameRules.Key<TValue> registerGameRule(String registryName, GameRules.Category category, GameRules.Type<TValue> type) {
        return GameRules.register(registryIdentifier(registryName), category, type);
    }

    // region: Boolean
    default GameRules.Key<GameRules.BooleanValue> registerBooleanGameRule(String registryName, GameRules.Category category, boolean defaultValue, BiConsumer<MinecraftServer, GameRules.BooleanValue> changeListener) {
        return registerGameRule(registryName, category, GameRules.BooleanValue.create(defaultValue, changeListener));
    }

    default GameRules.Key<GameRules.BooleanValue> registerBooleanGameRule(String registryName, GameRules.Category category, boolean defaultValue) {
        return registerBooleanGameRule(registryName, category, defaultValue, (server, value) -> { });
    }
    // endregion

    // region: Integer
    default GameRules.Key<GameRules.IntegerValue> registerIntegerGameRule(String registryName, GameRules.Category category, int defaultValue, BiConsumer<MinecraftServer, GameRules.IntegerValue> changeListener) {
        return registerGameRule(registryName, category, GameRules.IntegerValue.create(defaultValue, changeListener));
    }

    default GameRules.Key<GameRules.IntegerValue> registerIntegerGameRule(String registryName, GameRules.Category category, int defaultValue) {
        return registerIntegerGameRule(registryName, category, defaultValue, (server, value) -> { });
    }
    // endregion
    // endregion

    static Registree create(String namespace) {
        return new SimpleRegistree(namespace);
    }

    static String creativeModeTabKey(ResourceKey<CreativeModeTab> registryKey) {
        return creativeModeTabKey(registryKey.location());
    }

    static String creativeModeTabKey(ResourceLocation registryName) {
        return registryName.toLanguageKey("itemGroup");
    }
}
