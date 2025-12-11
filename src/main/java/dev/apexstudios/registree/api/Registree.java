package dev.apexstudios.registree.api;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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
import dev.apexstudios.registree.api.holder.DeferredGameRule;
import dev.apexstudios.registree.api.holder.DeferredItem;
import dev.apexstudios.registree.api.holder.DeferredMenu;
import dev.apexstudios.registree.api.holder.DeferredParticleType;
import dev.apexstudios.registree.api.holder.DeferredRecipeSerializer;
import dev.apexstudios.registree.impl.SimpleRegistree;
import dev.apexstudios.registree.impl.type.SimpleRecipeSerializer;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
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
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/// Base interface for all Registree types
///
/// Not to be directly implemented, extend {@link SimpleRegistree} instead
@ApiStatus.NonExtendable
public interface Registree {
    /// Registers all necessary event listeners needed to register all requested elements
    ///
    /// @param modBus Your mods [IEventBus]
    /// @apiNote This MUST be called during your mods common entry point
    void registerEvents(IEventBus modBus);

    /// {@return Namespace associated with this Registree}
    String namespace();

    /// {@return Registry identifier matching the following format `[namespace]:[registryName]`}
    default String registryIdentifier(String registryName) {
        return namespace() + Identifier.NAMESPACE_SEPARATOR + registryName;
    }

    /// {@return Registry name matching the following format `[namespace]:[registryName]`}
    default Identifier registryName(String registryName) {
        return Identifier.fromNamespaceAndPath(namespace(), registryName);
    }

    /// {@return Registry key of the given registry type matching the following format `[namespace]:[registryName]`}
    default <TRegistry> ResourceKey<TRegistry> registryKey(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName) {
        return ResourceKey.create(registryType, registryName(registryName));
    }

    /// {@return Tag key of the given registry type matching the following format `[namespace]:[registryName]`}
    default <TRegistry> TagKey<TRegistry> tag(ResourceKey<? extends Registry<TRegistry>> registryType, String tagPath) {
        return TagKey.create(registryType, registryName(tagPath));
    }

    /// {@return Lists all registry types associated with this Registree}
    Stream<ResourceKey<? extends Registry<?>>> listRegistries();

    // region: Registry
    /// {@return Optional holding the matching Holder for the given registry name or empty}
    <TRegistry> Optional<Holder.Reference<TRegistry>> get(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName);

    /// {@return Holder matching the given registry name}
    default <TRegistry> Holder.Reference<TRegistry> getOrThrow(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName) {
        return get(registryType, registryName).orElseThrow(() -> new NoSuchElementException("Missing key in '" + registryType.identifier() + "': '" + namespace() + ':' + registryName + "'"));
    }

    /// {@return Registered value matching the given registry name or null}
    @Nullable
    default <TRegistry> TRegistry getValue(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName) {
        return get(registryType, registryName).map(Holder::value).orElse(null);
    }

    /// {@return Optional holding the matching registered instance for the given registry name or empty}
    default <TRegistry> Optional<TRegistry> getOptional(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName) {
        return get(registryType, registryName).map(Holder::value);
    }

    /// {@return Registered value matching the given registry name}
    default <TRegistry> TRegistry getValueOrThrow(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName) {
        return getOrThrow(registryType, registryName).value();
    }

    /// {@return Lists all Holders associated with this Registree}
    <TRegistry> Stream<Holder.Reference<TRegistry>> listElements(ResourceKey<? extends Registry<TRegistry>> registryType);

    /// {@return Lists all registered values associated with this Registree}
    default <TRegistry> Stream<TRegistry> stream(ResourceKey<? extends Registry<TRegistry>> registryType) {
        return listElements(registryType).map(Holder::value);
    }

    /// {@return True if this Registree contains the given registry name}
    <TRegistry> boolean containsKey(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName);

    /// Enqueues the given listener to be invoked when the given registry name is registered
    <TRegistry> void listenFor(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Consumer<? super TRegistry> listener);

    /// {@return True if the given registry type has been processed}
    boolean isRegistered(ResourceKey<? extends Registry<?>> registryType);

    /// {@return True if this Registree has been processed}
    boolean isRegistered();

    /// {@return Converts this Registree into a RegistryLookup allowing lookups into this Registree only}
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
                return registryKey.isFor(registryType) && registryKey.identifier().getNamespace().equals(namespace()) ? Registree.this.get(registryType, registryKey.identifier().getPath()) : Optional.empty();
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
    /// Enqueues a new registration for the given registry type, name and factory
    ///
    /// @return {@link ResourceKey} pointing towards the queued registration
    <TRegistry> ResourceKey<TRegistry> register(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Function<Identifier, ? extends TRegistry> factory);

    /// Enqueues a new registration for the given registry type, name and factory
    ///
    /// @return {@link ResourceKey} pointing towards the queued registration
    /// @see #register(ResourceKey, String, Function)
    default <TRegistry> ResourceKey<TRegistry> register(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Supplier<? extends TRegistry> factory) {
        return register(registryType, registryName, $ -> factory.get());
    }

    /// Enqueues a new registration for the given registry type, name and factory
    ///
    /// @return The value enqueued to be registered
    /// @see #register(ResourceKey, String, Function)
    default <TRegistry, TElement extends TRegistry> TElement registerElement(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Function<Identifier, TElement> factory) {
        var element = factory.apply(registryName(registryName));
        register(registryType, registryName, () -> element);
        return element;
    }

    /// Enqueues a new registration for the given registry type, name and factory
    ///
    /// @see #registerElement(ResourceKey, String, Function)
    default <TRegistry, TElement extends TRegistry> TElement registerElement(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Supplier<TElement> factory) {
        return registerElement(registryType, registryName, $ -> factory.get());
    }

    /// Enqueues a new registration for the given registry type, name and factory
    ///
    /// @return The {@link Holder} holding the enqueued registration, built via the given holder factory
    /// @see #registerElement(ResourceKey, String, Function)
    default <TRegistry, THolder extends Holder<TRegistry>> THolder registerForHolder(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Function<Identifier, ? extends TRegistry> elementFactory, Function<ResourceKey<TRegistry>, THolder> holderFactory) {
        var registryKey = register(registryType, registryName, elementFactory);
        return holderFactory.apply(registryKey);
    }

    /// Enqueues a new registration for the given registry type, name and factory
    ///
    /// @return The {@link Holder} holding the enqueued registration, built via the given holder factory
    /// @see #registerForHolder(ResourceKey, String, Function, Function)
    default <TRegistry, THolder extends Holder<TRegistry>> THolder registerForHolder(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Supplier<? extends TRegistry> elementFactory, Function<ResourceKey<TRegistry>, THolder> holderFactory) {
        return registerForHolder(registryType, registryName, $ -> elementFactory.get(), holderFactory);
    }

    /// Enqueues a new registration for the given registry type, name and factory
    ///
    /// @return The {@link ApexDeferredHolder} holding the enqueued registration
    /// @see #registerForHolder(ResourceKey, String, Function, Function)
    default <TRegistry, TElement extends TRegistry> ApexDeferredHolder<TRegistry, TElement> registerForHolder(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Function<Identifier, ? extends TRegistry> factory) {
        return registerForHolder(registryType, registryName, factory, ApexDeferredHolder::new);
    }

    /// Enqueues a new registration for the given registry type, name and factory
    ///
    /// @return The {@link ApexDeferredHolder} holding the enqueued registration
    /// @see #registerForHolder(ResourceKey, String, Function)
    default <TRegistry, TElement extends TRegistry> ApexDeferredHolder<TRegistry, TElement> registerForHolder(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Supplier<? extends TRegistry> factory) {
        return registerForHolder(registryType, registryName, $ -> factory.get());
    }
    // endregion

    // region: Item
    /// Enqueues a new {@link Item} registration for the given registry name
    ///
    /// @return The {@link DeferredItem} holding the enqueued {@link Item} registration
    /// @see #registerForHolder(ResourceKey, String, Function, Function)
    default <TItem extends Item> DeferredItem<TItem> registerItem(String registryName, Function<Item.Properties, TItem> factory, Supplier<Item.Properties> propertiesFactory) {
        return registerForHolder(Registries.ITEM, registryName, $ -> factory.apply(propertiesFactory.get().setId(registryKey(Registries.ITEM, registryName))), DeferredItem::new);
    }

    /// Enqueues a new {@link Item} registration for the given registry name
    ///
    /// @return The {@link DeferredItem} holding the enqueued {@link Item} registration
    /// @see #registerItem(String, Function, Supplier)
    default <TItem extends Item> DeferredItem<TItem> registerItem(String registryName, Function<Item.Properties, TItem> factory, UnaryOperator<Item.Properties> propertiesMutator) {
        return registerItem(registryName, factory, () -> propertiesMutator.apply(new Item.Properties()));
    }

    /// Enqueues a new {@link Item} registration for the given registry name
    ///
    /// @return The {@link DeferredItem} holding the enqueued {@link Item} registration
    /// @see #registerItem(String, Function, Supplier)
    default <TItem extends Item> DeferredItem<TItem> registerItem(String registryName, Function<Item.Properties, TItem> factory, Item.Properties properties) {
        return registerItem(registryName, factory, () -> properties);
    }

    /// Enqueues a new {@link Item} registration for the given registry name
    ///
    /// @return The {@link DeferredItem} holding the enqueued {@link Item} registration
    /// @see #registerItem(String, Function, Supplier)
    default <TItem extends Item> DeferredItem<TItem> registerItem(String registryName, Function<Item.Properties, TItem> factory) {
        return registerItem(registryName, factory, Item.Properties::new);
    }

    /// Enqueues a new {@link Item} registration for the given registry name
    ///
    /// @return The {@link DeferredItem} holding the enqueued {@link Item} registration
    /// @see #registerItem(String, Function, Supplier)
    default DeferredItem<Item> registerSimpleItem(String registryName, Supplier<Item.Properties> propertiesFactory) {
        return registerItem(registryName, Item::new, propertiesFactory);
    }

    /// Enqueues a new {@link Item} registration for the given registry name
    ///
    /// @return The {@link DeferredItem} holding the enqueued {@link Item} registration
    /// @see #registerSimpleItem(String, Supplier)
    default DeferredItem<Item> registerSimpleItem(String registryName, UnaryOperator<Item.Properties> propertiesMutator) {
        return registerSimpleItem(registryName, () -> propertiesMutator.apply(new Item.Properties()));
    }

    /// Enqueues a new {@link Item} registration for the given registry name
    ///
    /// @return The {@link DeferredItem} holding the enqueued {@link Item} registration
    /// @see #registerSimpleItem(String, Supplier)
    default DeferredItem<Item> registerSimpleItem(String registryName, Item.Properties properties) {
        return registerSimpleItem(registryName, () -> properties);
    }

    /// Enqueues a new {@link Item} registration for the given registry name
    ///
    /// @return The {@link DeferredItem} holding the enqueued {@link Item} registration
    /// @see #registerSimpleItem(String, Supplier)
    default DeferredItem<Item> registerSimpleItem(String registryName) {
        return registerSimpleItem(registryName, Item.Properties::new);
    }

    /// Enqueues a new {@link Item} registration for the given registry name
    ///
    /// @return The {@link DeferredItem} holding the enqueued {@link Item} registration
    /// @see #registerItem(String, Function, Supplier)
    default <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(String registryName, Supplier<TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, Supplier<Item.Properties> propertiesFactory) {
        return registerItem(registryName, properties -> factory.apply(block.get(), properties.useBlockDescriptionPrefix()), propertiesFactory);
    }

    /// Enqueues a new {@link Item} registration for the given registry name
    ///
    /// @return The {@link DeferredItem} holding the enqueued {@link Item} registration
    /// @see #registerBlockItem(String, Supplier, BiFunction, Supplier)
    default <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(String registryName, Supplier<TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, UnaryOperator<Item.Properties> propertiesMutator) {
        return registerBlockItem(registryName, block, factory, () -> propertiesMutator.apply(new Item.Properties()));
    }

    /// Enqueues a new {@link Item} registration for the given registry name
    ///
    /// @return The {@link DeferredItem} holding the enqueued {@link Item} registration
    /// @see #registerBlockItem(String, Supplier, BiFunction, Supplier)
    default <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(String registryName, Supplier<TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, Item.Properties properties) {
        return registerBlockItem(registryName, block, factory, () -> properties);
    }

    /// Enqueues a new {@link Item} registration for the given registry name
    ///
    /// @return The {@link DeferredItem} holding the enqueued {@link Item} registration
    /// @see #registerBlockItem(String, Supplier, BiFunction, Supplier)
    default <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(String registryName, Supplier<TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory) {
        return registerBlockItem(registryName, block, factory, Item.Properties::new);
    }

    /// Enqueues a new {@link BlockItem} registration for the given registry name
    ///
    /// @return The {@link DeferredItem} holding the enqueued {@link BlockItem} registration
    /// @see #registerBlockItem(String, Supplier, BiFunction, Supplier)
    default DeferredItem<BlockItem> registerSimpleBlockItem(String registryName, Supplier<? extends Block> block, Supplier<Item.Properties> propertiesFactory) {
        return registerBlockItem(registryName, block, BlockItem::new, propertiesFactory);
    }

    /// Enqueues a new {@link BlockItem} registration for the given registry name
    ///
    /// @return The {@link DeferredItem} holding the enqueued {@link BlockItem} registration
    /// @see #registerSimpleBlockItem(String, Supplier, Supplier)
    default DeferredItem<BlockItem> registerSimpleBlockItem(String registryName, Supplier<? extends Block> block, UnaryOperator<Item.Properties> propertiesMutator) {
        return registerSimpleBlockItem(registryName, block, () -> propertiesMutator.apply(new Item.Properties()));
    }

    /// Enqueues a new {@link BlockItem} registration for the given registry name
    ///
    /// @return The {@link DeferredItem} holding the enqueued {@link BlockItem} registration
    /// @see #registerSimpleBlockItem(String, Supplier, Supplier)
    default DeferredItem<BlockItem> registerSimpleBlockItem(String registryName, Supplier<? extends Block> block, Item.Properties properties) {
        return registerSimpleBlockItem(registryName, block, () -> properties);
    }

    /// Enqueues a new {@link BlockItem} registration for the given registry name
    ///
    /// @return The {@link DeferredItem} holding the enqueued {@link BlockItem} registration
    /// @see #registerSimpleBlockItem(String, Supplier, Supplier)
    default DeferredItem<BlockItem> registerSimpleBlockItem(String registryName, Supplier<? extends Block> block) {
        return registerSimpleBlockItem(registryName, block, Item.Properties::new);
    }

    /// Enqueues a new {@link Item} registration for the given registry name
    ///
    /// @return The {@link DeferredItem} holding the enqueued {@link Item} registration
    /// @see #registerBlockItem(String, Supplier, BiFunction, Supplier)
    default <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(DeferredHolder<Block, TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, Supplier<Item.Properties> propertiesFactory) {
        return registerBlockItem(block.getId().getPath(), block, factory, propertiesFactory);
    }

    /// Enqueues a new {@link Item} registration for the given registry name
    ///
    /// @return The {@link DeferredItem} holding the enqueued {@link Item} registration
    /// @see #registerBlockItem(DeferredHolder, BiFunction, Supplier)
    default <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(DeferredHolder<Block, TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, UnaryOperator<Item.Properties> propertiesMutator) {
        return registerBlockItem(block, factory, () -> propertiesMutator.apply(new Item.Properties()));
    }

    /// Enqueues a new {@link Item} registration for the given registry name
    ///
    /// @return The {@link DeferredItem} holding the enqueued {@link Item} registration
    /// @see #registerBlockItem(DeferredHolder, BiFunction, Supplier)
    default <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(DeferredHolder<Block, TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory, Item.Properties properties) {
        return registerBlockItem(block, factory, () -> properties);
    }

    /// Enqueues a new {@link Item} registration for the given registry name
    ///
    /// @return The {@link DeferredItem} holding the enqueued {@link Item} registration
    /// @see #registerBlockItem(DeferredHolder, BiFunction, Supplier)
    default <TItem extends Item, TBlock extends Block> DeferredItem<TItem> registerBlockItem(DeferredHolder<Block, TBlock> block, BiFunction<TBlock, Item.Properties, TItem> factory) {
        return registerBlockItem(block, factory, Item.Properties::new);
    }

    /// Enqueues a new {@link BlockItem} registration for the given registry name
    ///
    /// @return The {@link DeferredItem} holding the enqueued {@link BlockItem} registration
    /// @see #registerSimpleBlockItem(String, Supplier, Supplier)
    default DeferredItem<BlockItem> registerSimpleBlockItem(DeferredHolder<Block, ? extends Block> block, Supplier<Item.Properties> propertiesFactory) {
        return registerSimpleBlockItem(block.getId().getPath(), block, propertiesFactory);
    }

    /// Enqueues a new {@link BlockItem} registration for the given registry name
    ///
    /// @return The {@link DeferredItem} holding the enqueued {@link BlockItem} registration
    /// @see #registerSimpleBlockItem(DeferredHolder, Supplier)
    default DeferredItem<BlockItem> registerSimpleBlockItem(DeferredHolder<Block, ? extends Block> block, UnaryOperator<Item.Properties> propertiesMutator) {
        return registerSimpleBlockItem(block, () -> propertiesMutator.apply(new Item.Properties()));
    }

    /// Enqueues a new {@link BlockItem} registration for the given registry name
    ///
    /// @return The {@link DeferredItem} holding the enqueued {@link BlockItem} registration
    /// @see #registerSimpleBlockItem(DeferredHolder, Supplier)
    default DeferredItem<BlockItem> registerSimpleBlockItem(DeferredHolder<Block, ? extends Block> block, Item.Properties properties) {
        return registerSimpleBlockItem(block, () -> properties);
    }

    /// Enqueues a new {@link BlockItem} registration for the given registry name
    ///
    /// @return The {@link DeferredItem} holding the enqueued {@link BlockItem} registration
    /// @see #registerSimpleBlockItem(DeferredHolder, Supplier)
    default DeferredItem<BlockItem> registerSimpleBlockItem(DeferredHolder<Block, ? extends Block> block) {
        return registerSimpleBlockItem(block, Item.Properties::new);
    }
    // endregion

    // region: Block
    /// Enqueues a new {@link Block} registration for the given registry name
    ///
    /// @return The {@link DeferredBlock} holding the enqueued {@link Block} registration
    /// @see #registerForHolder(ResourceKey, String, Function, Function)
    default <TBlock extends Block> DeferredBlock<TBlock> registerBlock(String registryName, Function<BlockBehaviour.Properties, TBlock> factory, Supplier<BlockBehaviour.Properties> propertiesFactory) {
        return registerForHolder(Registries.BLOCK, registryName, $ -> factory.apply(propertiesFactory.get().setId(registryKey(Registries.BLOCK, registryName))), DeferredBlock::new);
    }

    /// Enqueues a new {@link Block} registration for the given registry name
    ///
    /// @return The {@link DeferredBlock} holding the enqueued {@link Block} registration
    /// @see #registerBlock(String, Function, Supplier)
    default <TBlock extends Block> DeferredBlock<TBlock> registerBlock(String registryName, Function<BlockBehaviour.Properties, TBlock> factory, BlockBehaviour.Properties properties) {
        return registerBlock(registryName, factory, () -> properties);
    }

    /// Enqueues a new {@link Block} registration for the given registry name
    ///
    /// @return The {@link DeferredBlock} holding the enqueued {@link Block} registration
    /// @see #registerBlock(String, Function, Supplier)
    default DeferredBlock<Block> registerSimpleBlock(String registryName, Supplier<BlockBehaviour.Properties> propertiesFactory) {
        return registerBlock(registryName, Block::new, propertiesFactory);
    }

    /// Enqueues a new {@link Block} registration for the given registry name
    ///
    /// @return The {@link DeferredBlock} holding the enqueued {@link Block} registration
    /// @see #registerSimpleBlock(String, Supplier)
    default DeferredBlock<Block> registerSimpleBlock(String registryName, BlockBehaviour.Properties properties) {
        return registerSimpleBlock(registryName, () -> properties);
    }
    // endregion

    // region: BlockEntity
    /// Enqueues a new {@link BlockEntityType} registration for the given registry name
    ///
    /// @return The {@link DeferredBlockEntity} holding the enqueued {@link BlockEntityType} registration
    /// @see #registerForHolder(ResourceKey, String, Supplier, Function)
    default <TBlockEntity extends BlockEntity> DeferredBlockEntity<TBlockEntity> registerBlockEntity(String registryName, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory, Supplier<? extends Block>... validBlocks) {
        return registerForHolder(Registries.BLOCK_ENTITY_TYPE, registryName, () -> {
            var blocks = Stream.of(validBlocks).map(Supplier::get).collect(Collectors.<Block>toSet());
            return new BlockEntityType<>(factory, blocks);
        }, DeferredBlockEntity::new);
    }

    /// Enqueues a new {@link BlockEntityType} registration for the given registry name
    ///
    /// @return The {@link DeferredBlockEntity} holding the enqueued {@link BlockEntityType} registration
    /// @see #registerBlockEntity(String, BlockEntityType.BlockEntitySupplier, Supplier[])
    default <TBlockEntity extends BlockEntity> DeferredBlockEntity<TBlockEntity> registerBlockEntity(DeferredHolder<Block, ?> block, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory, Supplier<? extends Block>... validBlocks) {
        return registerBlockEntity(block.getId().getPath(), factory, ArrayUtils.add(validBlocks, block));
    }

    /// Enqueues a new {@link BlockEntityType} registration for the given registry name
    ///
    /// @return The {@link DeferredBlockEntity} holding the enqueued {@link BlockEntityType} registration
    /// @see #registerBlockEntity(String, BlockEntityType.BlockEntitySupplier, Supplier[])
    default <TBlockEntity extends BlockEntity> DeferredBlockEntity<TBlockEntity> registerBlockEntity(DeferredHolder<Block, ?> block, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory) {
        return registerBlockEntity(block.getId().getPath(), factory, block);
    }
    // endregion

    // region: Entity
    /// Enqueues a new {@link EntityType} registration for the given registry name
    ///
    /// @return The {@link DeferredEntity} holding the enqueued {@link EntityType} registration
    /// @see #registerForHolder(ResourceKey, String, Function, Function)
    default <TEntity extends Entity> DeferredEntity<TEntity> registerEntity(String registryName, EntityType.EntityFactory<TEntity> factory, MobCategory category, UnaryOperator<EntityType.Builder<TEntity>> propertiesMutator) {
        return registerForHolder(Registries.ENTITY_TYPE, registryName, $ -> propertiesMutator.apply(EntityType.Builder.of(factory, category)).build(registryKey(Registries.ENTITY_TYPE, registryName)), DeferredEntity::new);
    }

    /// Enqueues a new {@link EntityType} registration for the given registry name
    ///
    /// @return The {@link DeferredEntity} holding the enqueued {@link EntityType} registration
    /// @see #registerEntity(String, EntityType.EntityFactory, MobCategory, UnaryOperator)
    default <TEntity extends Entity> DeferredEntity<TEntity> registerEntity(String registryName, EntityType.EntityFactory<TEntity> factory, MobCategory category) {
        return registerEntity(registryName, factory, category, UnaryOperator.identity());
    }

    /// Enqueues a new {@link SpawnEggItem} registration for the given registry name
    ///
    /// @return The {@link DeferredItem} holding the enqueued {@link SpawnEggItem} registration
    /// @see #registerItem(String, Function, Supplier)
    default DeferredItem<SpawnEggItem> registerSpawnEggItem(DeferredHolder<EntityType<?>, EntityType<?>> entityType, Supplier<Item.Properties> propertiesFactory) {
        return registerItem(entityType.getId().getPath() + "_spawn_egg", properties -> new SpawnEggItem(properties.spawnEgg(entityType.value())), propertiesFactory);
    }

    /// Enqueues a new {@link SpawnEggItem} registration for the given registry name
    ///
    /// @return The {@link DeferredItem} holding the enqueued {@link SpawnEggItem} registration
    /// @see #registerSpawnEggItem(DeferredHolder, Supplier)
    default DeferredItem<SpawnEggItem> registerSpawnEggItem(DeferredHolder<EntityType<?>, EntityType<?>> entityType, UnaryOperator<Item.Properties> propertiesMutator) {
        return registerSpawnEggItem(entityType, () -> propertiesMutator.apply(new Item.Properties()));
    }

    /// Enqueues a new {@link SpawnEggItem} registration for the given registry name
    ///
    /// @return The {@link DeferredItem} holding the enqueued {@link SpawnEggItem} registration
    /// @see #registerSpawnEggItem(DeferredHolder, Supplier)
    default DeferredItem<SpawnEggItem> registerSpawnEggItem(DeferredHolder<EntityType<?>, EntityType<?>> entityType, Item.Properties properties) {
        return registerSpawnEggItem(entityType, () -> properties);
    }

    /// Enqueues a new {@link SpawnEggItem} registration for the given registry name
    ///
    /// @return The {@link DeferredItem} holding the enqueued {@link SpawnEggItem} registration
    /// @see #registerSpawnEggItem(DeferredHolder, Supplier)
    default DeferredItem<SpawnEggItem> registerSpawnEggItem(DeferredHolder<EntityType<?>, EntityType<?>> entityType) {
        return registerSpawnEggItem(entityType, Item.Properties::new);
    }
    // endregion

    // region: DataComponent
    /// Enqueues a new {@link DataComponentType} registration for the given registry name
    ///
    /// @return The {@link DeferredDataComponent} holding the enqueued {@link DataComponentType} registration
    /// @see #registerForHolder(ResourceKey, String, Supplier, Function)
    default <TData> DeferredDataComponent<TData> registerDataComponent(String registryName, UnaryOperator<DataComponentType.Builder<TData>> builder) {
        return registerForHolder(Registries.DATA_COMPONENT_TYPE, registryName, () -> builder.apply(DataComponentType.builder()).build(), DeferredDataComponent::new);
    }

    /// Enqueues a new {@link DataComponentType} registration for the given registry name
    ///
    /// @return The {@link DeferredDataComponent} holding the enqueued {@link DataComponentType} registration
    /// @see #registerDataComponent(String, UnaryOperator)
    default <TData> DeferredDataComponent<TData> registerDataComponent(String registryName, Codec<TData> codec, StreamCodec<RegistryFriendlyByteBuf, TData> streamCodec) {
        return registerDataComponent(registryName, builder -> builder.persistent(codec).networkSynchronized(streamCodec));
    }

    /// Enqueues a new {@link DataComponentType} registration for the given registry name
    ///
    /// @return The {@link DeferredDataComponent} holding the enqueued {@link DataComponentType} registration
    /// @see #registerDataComponent(String, UnaryOperator)
    default <TData> DeferredDataComponent<TData> registerDataComponent(String registryName, Codec<TData> codec, Codec<TData> networkCodec) {
        return registerDataComponent(registryName, builder -> builder.persistent(codec).networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(networkCodec)));
    }

    /// Enqueues a new {@link DataComponentType} registration for the given registry name
    ///
    /// @return The {@link DeferredDataComponent} holding the enqueued {@link DataComponentType} registration
    /// @see #registerDataComponent(String, UnaryOperator)
    default <TData> DeferredDataComponent<TData> registerDataComponent(String registryName, Codec<TData> codec) {
        return registerDataComponent(registryName, builder -> builder.persistent(codec));
    }
    // endregion

    // region: CreativeModeTab
    /// Enqueues a new {@link CreativeModeTab} registration for the given registry name
    ///
    /// @return {@link ResourceKey} pointing towards the queued {@link CreativeModeTab} registration
    /// @see #register(ResourceKey, String, Supplier)
    default ResourceKey<CreativeModeTab> registerCreativeModeTab(String registryName, UnaryOperator<CreativeModeTab.Builder> builder) {
        return register(Registries.CREATIVE_MODE_TAB, registryName, () -> builder.apply(CreativeModeTab.builder().title(Component.translatable(creativeModeTabKey(registryName(registryName))))).build());
    }

    /// Enqueues a new {@link CreativeModeTab} registration for the given registry name
    ///
    /// @return {@link ResourceKey} pointing towards the queued {@link CreativeModeTab} registration
    /// @see #registerCreativeModeTab(String, UnaryOperator)
    default ResourceKey<CreativeModeTab> registerCreativeModeTab(String registryName, Supplier<ItemStack> icon, CreativeModeTab.DisplayItemsGenerator itemsGenerator) {
        return registerCreativeModeTab(registryName, builder -> builder.icon(icon).displayItems(itemsGenerator));
    }
    // endregion

    // region: Menu
    /// Enqueues a new {@link MenuType} registration for the given registry name
    ///
    /// @return The {@link DeferredMenu} holding the enqueued {@link MenuType} registration
    /// @see #registerForHolder(ResourceKey, String, Supplier, Function)
    default <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, FeatureFlagSet requiredFeatures) {
        return registerForHolder(Registries.MENU, registryName, () -> new MenuType<>(factory, requiredFeatures), DeferredMenu::new);
    }

    /// Enqueues a new {@link MenuType} registration for the given registry name
    ///
    /// @return The {@link DeferredMenu} holding the enqueued {@link MenuType} registration
    /// @see #registerMenu(String, MenuType.MenuSupplier, FeatureFlag)
    default <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, FeatureFlag requiredFeature, FeatureFlag... requiredFeatures) {
        return registerMenu(registryName, factory, FeatureFlagSet.of(requiredFeature, requiredFeatures));
    }

    /// Enqueues a new {@link MenuType} registration for the given registry name
    ///
    /// @return The {@link DeferredMenu} holding the enqueued {@link MenuType} registration
    /// @see #registerMenu(String, MenuType.MenuSupplier, FeatureFlag)
    default <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, FeatureFlag requiredFeature) {
        return registerMenu(registryName, factory, FeatureFlagSet.of(requiredFeature));
    }

    /// Enqueues a new {@link MenuType} registration for the given registry name
    ///
    /// @return The {@link DeferredMenu} holding the enqueued {@link MenuType} registration
    /// @see #registerMenu(String, MenuType.MenuSupplier, FeatureFlag)
    default <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory) {
        return registerMenu(registryName, factory, FeatureFlags.VANILLA_SET);
    }

    /// Enqueues a new {@link MenuType} registration for the given registry name
    ///
    /// @return The {@link DeferredMenu} holding the enqueued {@link MenuType} registration
    /// @see #registerMenu(String, MenuType.MenuSupplier, FeatureFlag)
    default <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory, FeatureFlagSet requiredFeatures) {
        return registerMenu(registryName, (MenuType.MenuSupplier<TMenu>) factory, requiredFeatures);
    }

    /// Enqueues a new {@link MenuType} registration for the given registry name
    ///
    /// @return The {@link DeferredMenu} holding the enqueued {@link MenuType} registration
    /// @see #registerMenu(String, MenuType.MenuSupplier, FeatureFlag)
    default <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory, FeatureFlag requiredFeature, FeatureFlag... requiredFeatures) {
        return registerMenu(registryName, factory, FeatureFlagSet.of(requiredFeature, requiredFeatures));
    }

    /// Enqueues a new {@link MenuType} registration for the given registry name
    ///
    /// @return The {@link DeferredMenu} holding the enqueued {@link MenuType} registration
    /// @see #registerMenu(String, MenuType.MenuSupplier, FeatureFlag)
    default <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory, FeatureFlag requiredFeature) {
        return registerMenu(registryName, factory, FeatureFlagSet.of(requiredFeature));
    }

    /// Enqueues a new {@link MenuType} registration for the given registry name
    ///
    /// @return The {@link DeferredMenu} holding the enqueued {@link MenuType} registration
    /// @see #registerMenu(String, MenuType.MenuSupplier, FeatureFlag)
    default <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory) {
        return registerMenu(registryName, factory, FeatureFlags.VANILLA_SET);
    }

    /// Enqueues a new {@link MenuType} registration for the given registry name
    ///
    /// Additionally enqueues registration for the given screen factory
    ///
    /// @return The {@link DeferredMenu} holding the enqueued {@link MenuType} registration
    /// @see #registerMenu(String, MenuType.MenuSupplier, FeatureFlag)
    <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory, FeatureFlagSet requiredFeatures);

    /// Enqueues a new {@link MenuType} registration for the given registry name
    ///
    /// Additionally enqueues registration for the given screen factory
    ///
    /// @return The {@link DeferredMenu} holding the enqueued {@link MenuType} registration
    /// @see #registerMenu(String, MenuType.MenuSupplier, Supplier, FeatureFlagSet)
    default <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory, FeatureFlag requiredFeature, FeatureFlag... requiredFeatures) {
        return registerMenu(registryName, factory, screenFactory, FeatureFlagSet.of(requiredFeature, requiredFeatures));
    }

    /// Enqueues a new {@link MenuType} registration for the given registry name
    ///
    /// Additionally enqueues registration for the given screen factory
    ///
    /// @return The {@link DeferredMenu} holding the enqueued {@link MenuType} registration
    /// @see #registerMenu(String, MenuType.MenuSupplier, Supplier, FeatureFlagSet)
    default <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory, FeatureFlag requiredFeature) {
        return registerMenu(registryName, factory, screenFactory, FeatureFlagSet.of(requiredFeature));
    }

    /// Enqueues a new {@link MenuType} registration for the given registry name
    ///
    /// Additionally enqueues registration for the given screen factory
    ///
    /// @return The {@link DeferredMenu} holding the enqueued {@link MenuType} registration
    /// @see #registerMenu(String, MenuType.MenuSupplier, Supplier, FeatureFlagSet)
    default <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory) {
        return registerMenu(registryName, factory, screenFactory, FeatureFlags.VANILLA_SET);
    }

    /// Enqueues a new {@link MenuType} registration for the given registry name
    ///
    /// Additionally enqueues registration for the given screen factory
    ///
    /// @return The {@link DeferredMenu} holding the enqueued {@link MenuType} registration
    /// @see #registerMenu(String, MenuType.MenuSupplier, Supplier, FeatureFlagSet)
    default <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory, FeatureFlagSet requiredFeatures) {
        return registerMenu(registryName, (MenuType.MenuSupplier<TMenu>) factory, screenFactory, requiredFeatures);
    }

    /// Enqueues a new {@link MenuType} registration for the given registry name
    ///
    /// Additionally enqueues registration for the given screen factory
    ///
    /// @return The {@link DeferredMenu} holding the enqueued {@link MenuType} registration
    /// @see #registerMenu(String, MenuType.MenuSupplier, Supplier, FeatureFlagSet)
    default <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory, FeatureFlag requiredFeature, FeatureFlag... requiredFeatures) {
        return registerMenu(registryName, factory, screenFactory, FeatureFlagSet.of(requiredFeature, requiredFeatures));
    }

    /// Enqueues a new {@link MenuType} registration for the given registry name
    ///
    /// Additionally enqueues registration for the given screen factory
    ///
    /// @return The {@link DeferredMenu} holding the enqueued {@link MenuType} registration
    /// @see #registerMenu(String, MenuType.MenuSupplier, Supplier, FeatureFlagSet)
    default <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory, FeatureFlag requiredFeature) {
        return registerMenu(registryName, factory, screenFactory, FeatureFlagSet.of(requiredFeature));
    }

    /// Enqueues a new {@link MenuType} registration for the given registry name
    ///
    /// Additionally enqueues registration for the given screen factory
    ///
    /// @return The {@link DeferredMenu} holding the enqueued {@link MenuType} registration
    /// @see #registerMenu(String, MenuType.MenuSupplier, Supplier, FeatureFlagSet)
    default <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, IContainerFactory<TMenu> factory, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory) {
        return registerMenu(registryName, factory, screenFactory, FeatureFlags.VANILLA_SET);
    }
    // endregion

    // region: FluidType
    /// Enqueues a new {@link FluidType} registration for the given registry name
    ///
    /// @return The {@link DeferredFluidType} holding the enqueued {@link FluidType} registration
    /// @see #registerForHolder(ResourceKey, String, Function, Function)
    default <TFluidType extends FluidType> DeferredFluidType<TFluidType> registerFluidType(String registryName, Function<FluidType.Properties, TFluidType> factory, Supplier<FluidType.Properties> propertiesFactory) {
        return registerForHolder(NeoForgeRegistries.Keys.FLUID_TYPES, registryName, $ -> factory.apply(propertiesFactory.get()), DeferredFluidType::new);
    }

    /// Enqueues a new {@link FluidType} registration for the given registry name
    ///
    /// @return The {@link DeferredFluidType} holding the enqueued {@link FluidType} registration
    /// @see #registerFluidType(String, Function, Supplier)
    default <TFluidType extends FluidType> DeferredFluidType<TFluidType> registerFluidType(String registryName, Function<FluidType.Properties, TFluidType> factory, UnaryOperator<FluidType.Properties> propertiesMutator) {
        return registerFluidType(registryName, factory, () -> propertiesMutator.apply(FluidType.Properties.create()));
    }

    /// Enqueues a new {@link FluidType} registration for the given registry name
    ///
    /// @return The {@link DeferredFluidType} holding the enqueued {@link FluidType} registration
    /// @see #registerFluidType(String, Function, Supplier)
    default <TFluidType extends FluidType> DeferredFluidType<TFluidType> registerFluidType(String registryName, Function<FluidType.Properties, TFluidType> factory, FluidType.Properties properties) {
        return registerFluidType(registryName, factory, () -> properties);
    }

    /// Enqueues a new {@link FluidType} registration for the given registry name
    ///
    /// @return The {@link DeferredFluidType} holding the enqueued {@link FluidType} registration
    /// @see #registerFluidType(String, Function, Supplier)
    default <TFluidType extends FluidType> DeferredFluidType<TFluidType> registerFluidType(String registryName, Function<FluidType.Properties, TFluidType> factory) {
        return registerFluidType(registryName, factory, FluidType.Properties::create);
    }

    /// Enqueues a new {@link FluidType} registration for the given registry name
    ///
    /// @return The {@link DeferredFluidType} holding the enqueued {@link FluidType} registration
    /// @see #registerFluidType(String, Function, Supplier)
    default DeferredFluidType<FluidType> registerSimpleFluidType(String registryName, Supplier<FluidType.Properties> propertiesFactory) {
        return registerFluidType(registryName, FluidType::new, propertiesFactory);
    }

    /// Enqueues a new {@link FluidType} registration for the given registry name
    ///
    /// @return The {@link DeferredFluidType} holding the enqueued {@link FluidType} registration
    /// @see #registerFluidType(String, Function, Supplier)
    default DeferredFluidType<FluidType> registerSimpleFluidType(String registryName, UnaryOperator<FluidType.Properties> propertiesMutator) {
        return registerSimpleFluidType(registryName, () -> propertiesMutator.apply(FluidType.Properties.create()));
    }

    /// Enqueues a new {@link FluidType} registration for the given registry name
    ///
    /// @return The {@link DeferredFluidType} holding the enqueued {@link FluidType} registration
    /// @see #registerFluidType(String, Function, Supplier)
    default DeferredFluidType<FluidType> registerSimpleFluidType(String registryName, FluidType.Properties properties) {
        return registerSimpleFluidType(registryName, () -> properties);
    }

    /// Enqueues a new {@link FluidType} registration for the given registry name
    ///
    /// @return The {@link DeferredFluidType} holding the enqueued {@link FluidType} registration
    /// @see #registerFluidType(String, Function, Supplier)
    default DeferredFluidType<FluidType> registerSimpleFluidType(String registryName) {
        return registerSimpleFluidType(registryName, FluidType.Properties::create);
    }
    // endregion

    // region: Fluid
    /// Enqueues a new {@link Fluid} registration for the given registry name
    ///
    /// @return The {@link DeferredFluid} holding the enqueued {@link Fluid} registration
    /// @see #registerForHolder(ResourceKey, String, Supplier, Function)
    default <TFluid extends Fluid> DeferredFluid<TFluid> registerFluid(String registryName, Supplier<TFluid> factory) {
        return registerForHolder(Registries.FLUID, registryName, factory, DeferredFluid::new);
    }
    // endregion

    // region: RecipeSerializer
    /// Enqueues a new {@link RecipeSerializer} registration for the given registry name
    ///
    /// @return The {@link DeferredRecipeSerializer} holding the enqueued {@link RecipeSerializer} registration
    /// @see #registerForHolder(ResourceKey, String, Supplier, Function)
    default <TRecipe extends Recipe<?>> DeferredRecipeSerializer<TRecipe> registerRecipeSerializer(String registryName, MapCodec<TRecipe> codec, StreamCodec<RegistryFriendlyByteBuf, TRecipe> streamCodec) {
        return registerForHolder(Registries.RECIPE_SERIALIZER, registryName, () -> new SimpleRecipeSerializer<>(codec, streamCodec), DeferredRecipeSerializer::new);
    }
    // endregion

    // region: ParticleType
    /// Enqueues a new {@link ParticleType} registration for the given registry name
    ///
    /// @return The {@link DeferredParticleType} holding the enqueued {@link ParticleType} registration
    /// @see #registerForHolder(ResourceKey, String, Supplier, Function)
    default <TParticle extends ParticleOptions, TParticleType extends ParticleType<TParticle>> DeferredParticleType<TParticle, TParticleType> registerParticle(String registryName, Supplier<ParticleType<TParticle>> factory) {
        return registerForHolder(Registries.PARTICLE_TYPE, registryName, factory, DeferredParticleType::new);
    }

    /// Enqueues a new {@link ParticleType} registration for the given registry name
    ///
    /// @return The {@link DeferredParticleType} holding the enqueued {@link ParticleType} registration
    /// @see #registerParticle(String, Supplier)
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

    /// Enqueues a new {@link ParticleType} registration for the given registry name
    ///
    /// @return The {@link DeferredParticleType} holding the enqueued {@link ParticleType} registration
    /// @see #registerParticle(String, Supplier)
    default DeferredParticleType<SimpleParticleType, SimpleParticleType> registerSimpleParticle(String registryName, boolean overrideLimiter) {
        return registerParticle(registryName, () -> new SimpleParticleType(overrideLimiter));
    }
    // endregion

    // region: GameRules
    /// Registers a new {@link GameRule} for the given registry name and type
    ///
    /// @return The {@link DeferredGameRule} holding the enqueued {@link GameRule} registration
    default <TValue> DeferredGameRule<TValue> registerGameRule(String registryName, GameRuleCategory category, GameRuleType ruleType, ArgumentType<TValue> argumentType, Codec<TValue> codec, TValue defaultValue, FeatureFlagSet requiredFeatures, GameRules.VisitorCaller<TValue> visitor, ToIntFunction<TValue> commandResult) {
        return registerForHolder(Registries.GAME_RULE, registryName, () -> new GameRule<>(category, ruleType, argumentType, visitor, codec, commandResult, defaultValue, requiredFeatures), DeferredGameRule::new);
    }

    /// Registers a new {@link GameRule} for the given registry name and type
    ///
    /// @return The {@link DeferredGameRule} holding the enqueued {@link GameRule} registration
    /// @see #registerGameRule(String, GameRuleCategory, GameRuleType, ArgumentType, Codec, Object, FeatureFlagSet, GameRules.VisitorCaller, ToIntFunction)
    default <TValue> DeferredGameRule<TValue> registerGameRule(String registryName, GameRuleCategory category, GameRuleType ruleType, ArgumentType<TValue> argumentType, Codec<TValue> codec, TValue defaultValue, GameRules.VisitorCaller<TValue> visitor, ToIntFunction<TValue> commandResult) {
        return registerGameRule(registryName, category, ruleType, argumentType, codec, defaultValue, FeatureFlags.DEFAULT_FLAGS, visitor, commandResult);
    }

    // region: Boolean
    /// Registers a new {@link GameRule} for the given registry name and type
    ///
    /// @return The {@link DeferredGameRule} holding the enqueued {@link GameRule} registration
    /// @see #registerGameRule(String, GameRuleCategory, GameRuleType, ArgumentType, Codec, Object, FeatureFlagSet, GameRules.VisitorCaller, ToIntFunction)
    default DeferredGameRule<Boolean> registerBooleanGameRule(String registryName, GameRuleCategory category, boolean defaultValue, FeatureFlagSet requiredFeatures) {
        return registerGameRule(registryName, category, GameRuleType.BOOL, BoolArgumentType.bool(), Codec.BOOL, defaultValue, requiredFeatures, GameRuleTypeVisitor::visitBoolean, value -> value ? Command.SINGLE_SUCCESS : 0);
    }

    /// Registers a new {@link GameRule} for the given registry name and type
    ///
    /// @return The {@link DeferredGameRule} holding the enqueued {@link GameRule} registration
    /// @see #registerBooleanGameRule(String, GameRuleCategory, boolean, FeatureFlagSet)
    default DeferredGameRule<Boolean> registerBooleanGameRule(String registryName, GameRuleCategory category, boolean defaultValue) {
        return registerBooleanGameRule(registryName, category, defaultValue, FeatureFlags.DEFAULT_FLAGS);
    }

    /// Registers a new {@link GameRule} for the given registry name and type
    ///
    /// @return The {@link DeferredGameRule} holding the enqueued {@link GameRule} registration
    /// @see #registerBooleanGameRule(String, GameRuleCategory, boolean, FeatureFlagSet)
    default DeferredGameRule<Boolean> registerBooleanGameRule(String registryName, GameRuleCategory category, FeatureFlagSet requiredFeatures) {
        return registerBooleanGameRule(registryName, category, false, requiredFeatures);
    }

    /// Registers a new {@link GameRule} for the given registry name and type
    ///
    /// @return The {@link DeferredGameRule} holding the enqueued {@link GameRule} registration
    /// @see #registerBooleanGameRule(String, GameRuleCategory, boolean, FeatureFlagSet)
    default DeferredGameRule<Boolean> registerBooleanGameRule(String registryName, GameRuleCategory category) {
        return registerBooleanGameRule(registryName, category, false, FeatureFlags.DEFAULT_FLAGS);
    }
    // endregion

    // region: Integer
    /// Registers a new {@link GameRule} for the given registry name and type
    ///
    /// @return The {@link DeferredGameRule} holding the enqueued {@link GameRule} registration
    /// @see #registerGameRule(String, GameRuleCategory, GameRuleType, ArgumentType, Codec, Object, FeatureFlagSet, GameRules.VisitorCaller, ToIntFunction)
    default DeferredGameRule<Integer> registerIntegerGameRule(String registryName, GameRuleCategory category, int defaultValue, int min, int max, FeatureFlagSet requiredFeatures) {
        return registerGameRule(registryName, category, GameRuleType.INT, IntegerArgumentType.integer(min, max), Codec.intRange(min, max), defaultValue, requiredFeatures, GameRuleTypeVisitor::visitInteger, value -> value);
    }

    /// Registers a new {@link GameRule} for the given registry name and type
    ///
    /// @return The {@link DeferredGameRule} holding the enqueued {@link GameRule} registration
    /// @see #registerIntegerGameRule(String, GameRuleCategory, int, int, int, FeatureFlagSet)
    default DeferredGameRule<Integer> registerIntegerGameRule(String registryName, GameRuleCategory category, int defaultValue, int min, int max) {
        return registerIntegerGameRule(registryName, category, defaultValue, min, max, FeatureFlags.DEFAULT_FLAGS);
    }

    /// Registers a new {@link GameRule} for the given registry name and type
    ///
    /// @return The {@link DeferredGameRule} holding the enqueued {@link GameRule} registration
    /// @see #registerIntegerGameRule(String, GameRuleCategory, int, int, int, FeatureFlagSet)
    default DeferredGameRule<Integer> registerIntegerGameRule(String registryName, GameRuleCategory category, int defaultValue, int min) {
        return registerIntegerGameRule(registryName, category, defaultValue, min, Integer.MAX_VALUE, FeatureFlags.DEFAULT_FLAGS);
    }

    /// Registers a new {@link GameRule} for the given registry name and type
    ///
    /// @return The {@link DeferredGameRule} holding the enqueued {@link GameRule} registration
    /// @see #registerIntegerGameRule(String, GameRuleCategory, int, int, int, FeatureFlagSet)
    default DeferredGameRule<Integer> registerIntegerGameRule(String registryName, GameRuleCategory category, int defaultValue, int min, FeatureFlagSet requiredFeatures) {
        return registerIntegerGameRule(registryName, category, defaultValue, min, Integer.MAX_VALUE, requiredFeatures);
    }
    // endregion
    // endregion
    // endregion

    /// {@return New Registree for the given namespace}
    static Registree create(String namespace) {
        return new SimpleRegistree(namespace);
    }

    /// {@return Default CreativeModeTab translation key}
    static String creativeModeTabKey(ResourceKey<CreativeModeTab> registryKey) {
        return creativeModeTabKey(registryKey.identifier());
    }

    /// {@return Default CreativeModeTab translation key}
    static String creativeModeTabKey(Identifier registryName) {
        return registryName.toLanguageKey("itemGroup");
    }
}
