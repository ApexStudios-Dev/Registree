package dev.apexstudios.registree;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.apexstudios.registree.builder.BlockBuilder;
import dev.apexstudios.registree.builder.BlockEntityBuilder;
import dev.apexstudios.registree.builder.EntityBuilder;
import dev.apexstudios.registree.builder.FluidBuilder;
import dev.apexstudios.registree.builder.FluidTypeBuilder;
import dev.apexstudios.registree.builder.GameRuleBuilder;
import dev.apexstudios.registree.builder.ItemBuilder;
import dev.apexstudios.registree.builder.MenuBuilder;
import dev.apexstudios.registree.builder.ParticleBuilder;
import dev.apexstudios.registree.builder.RecipeTypeBuilder;
import dev.apexstudios.registree.holder.DeferredDataComponent;
import dev.apexstudios.registree.holder.DeferredRecipeBookCategory;
import dev.apexstudios.registree.holder.DeferredRecipeSerializer;
import dev.apexstudios.registree.holder.Holders;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.gamerules.GameRuleEntryFactory;
import net.neoforged.neoforge.client.gamerules.RegisterGameRuleEntryFactoryEvent;
import net.neoforged.neoforge.event.RegisterGameRuleCategoryEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import org.apache.commons.lang3.function.Consumers;
import org.jspecify.annotations.Nullable;

public class BaseRegistree<TSelf extends BaseRegistree<TSelf>> {
    protected final String namespace;

    private final Table<ResourceKey<? extends Registry<?>>, String, Function<Identifier, ?>> factories = TreeBasedTable.create();
    private final Table<ResourceKey<? extends Registry<?>>, String, ? super Holder.Reference<?>> holders = TreeBasedTable.create();
    private final Table<ResourceKey<? extends Registry<?>>, String, Object> values = TreeBasedTable.create();
    private final Multimap<ResourceKey<? extends Registry<?>>, Consumer<? extends Registry<?>>> listeners = MultimapBuilder.linkedHashKeys().linkedListValues().build();
    private final Set<ResourceKey<? extends Registry<?>>> registered = new LinkedHashSet<>();
    private @Nullable IEventBus modBus = null;
    private Consumer<IEventBus> deferredEvents = this::registerEvents;
    private final Set<GameRuleCategory> gameRuleCategories = new LinkedHashSet<>();
    private final Map<GameRuleType, GameRuleEntryFactory<?>> gameRuleEntryFactories = new LinkedHashMap<>();
    private final Set<Registry<?>> registries = new LinkedHashSet<>();
    private final Map<String, DynamicRegistry<?>> dynamicRegistries = new LinkedHashMap<>();
    private final Set<DataMapType<?, ?>> dataMapTypes = new LinkedHashSet<>();

    protected BaseRegistree(String namespace) {
        this.namespace = namespace;
    }

    public String namespace() {
        return namespace;
    }

    public Identifier registryName(String identifier) {
        return Identifier.fromNamespaceAndPath(namespace(), identifier);
    }

    public String registryIdentifier(String identifier) {
        return namespace() + Identifier.NAMESPACE_SEPARATOR + identifier;
    }

    public <TRegistry> ResourceKey<TRegistry> registryKey(ResourceKey<? extends Registry<TRegistry>> registryType, String identifier) {
        return ResourceKey.create(registryType, registryName(identifier));
    }

    public <TRegistry> TagKey<TRegistry> tag(ResourceKey<? extends Registry<TRegistry>> registryType, String path) {
        return TagKey.create(registryType, registryName(path));
    }

    // region Lookups
    @SuppressWarnings("unchecked")
    public <TRegistry> Holder.@Nullable Reference<TRegistry> getHolder(ResourceKey<? extends Registry<TRegistry>> registryType, String identifier) {
        var holder = holders.get(registryType, identifier);
        return holder == null ? null : (Holder.Reference<TRegistry>) holder;
    }

    public <TRegistry> Optional<Holder.Reference<TRegistry>> getHolderOptional(ResourceKey<? extends Registry<TRegistry>> registryType, String identifier) {
        return Optional.ofNullable(getHolder(registryType, identifier));
    }

    public <TRegistry> Holder.Reference<TRegistry> getHolderOrThrow(ResourceKey<? extends Registry<TRegistry>> registryType, String identifier) {
        return Objects.requireNonNull(getHolder(registryType, identifier));
    }

    @SuppressWarnings("unchecked")
    public <TRegistry> Collection<Holder.Reference<TRegistry>> getHolders(ResourceKey<? extends Registry<TRegistry>> registryType) {
        if(!isRegistered(registryType)) {
            return Collections.emptyList();
        }

        // yet again stupid java wont like it if i use var here
        // i could double cast but thats even uglier
        Collection<?> values = holders.row(registryType).values();
        return Collections.unmodifiableCollection((Collection<? extends Holder.Reference<TRegistry>>) values);
    }

    @SuppressWarnings("unchecked")
    public <TRegistry> Stream<Holder.Reference<TRegistry>> holders(ResourceKey<? extends Registry<TRegistry>> registryType) {
        // stupid java wont allow this to work even though idea says its fine
        // return isRegistered(registryType) ? (Stream<Holder.Reference<TRegistry>>) holders.row(registryType).values().stream() : Stream.empty();

        if(!isRegistered(registryType)) {
            return Stream.empty();
        }

        return holders.row(registryType).values().stream().map(holder -> (Holder.Reference<TRegistry>) holder);
    }

    @SuppressWarnings("unchecked")
    public <TRegistry> @Nullable TRegistry getValue(ResourceKey<? extends Registry<TRegistry>> registryType, String identifier) {
        var value = values.get(registryType, identifier);
        return value == null ? null : (TRegistry) value;
    }

    public <TRegistry> Optional<TRegistry> getValueOptional(ResourceKey<? extends Registry<TRegistry>> registryType, String identifier) {
        return Optional.ofNullable(getValue(registryType, identifier));
    }

    public <TRegistry> TRegistry getValueOrThrow(ResourceKey<? extends Registry<TRegistry>> registryType, String identifier) {
        return Objects.requireNonNull(getValue(registryType, identifier));
    }

    @SuppressWarnings("unchecked")
    public <TRegistry> Collection<TRegistry> getValues(ResourceKey<? extends Registry<TRegistry>> registryType) {
        if(!isRegistered(registryType)) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableCollection((Collection<TRegistry>) values.row(registryType).values());
    }

    @SuppressWarnings("unchecked")
    public <TRegistry> Stream<TRegistry> values(ResourceKey<? extends Registry<TRegistry>> registryType) {
        if (!isRegistered(registryType)) {
            return Stream.empty();
        }

        return (Stream<TRegistry>) values.row(registryType).values().stream();
    }

    public <TRegistry> boolean isRegistered(ResourceKey<? extends Registry<TRegistry>> registryType, String identifier) {
        if(!isRegistered(registryType)) {
            return factories.contains(registryType, identifier);
        }

        return holders.contains(registryType, identifier) || values.contains(registryType, identifier);
    }

    public boolean isRegistered(ResourceKey<? extends Registry<?>> registryType) {
        return registered.contains(registryType);
    }

    public Stream<ResourceKey<? extends Registry<?>>> registries() {
        return holders.rowKeySet().stream();
    }

    @CanIgnoreReturnValue
    public <TRegistry> TSelf whenRegistryAvailable(ResourceKey<? extends Registry<TRegistry>> registryType, Consumer<Registry<TRegistry>> action) {
        if(isRegistered(registryType)) {
            action.accept(getRegistry(registryType));
        } else {
            listeners.put(registryType, action);
        }

        return self();
    }

    @CanIgnoreReturnValue
    public <TRegistry> TSelf whenAvailable(ResourceKey<TRegistry> registryKey, Consumer<? super TRegistry> action) {
        return whenAvailable(registryKey.registryKey(), registryKey.identifier(), action);
    }

    @CanIgnoreReturnValue
    public <TRegistry> TSelf whenAvailable(ResourceKey<? extends Registry<TRegistry>> registryType, Identifier registryName, Consumer<? super TRegistry> action) {
        return whenRegistryAvailable(registryType, registry -> {
            var value = registry.getValue(registryName);

            if(value != null) {
                action.accept(value);
            }
        });
    }

    @CanIgnoreReturnValue
    public <TRegistry> TSelf whenAvailable(ResourceKey<? extends Registry<TRegistry>> registryType, String identifier, Consumer<? super TRegistry> action) {
        return whenAvailable(registryType, registryName(identifier), action);
    }

    @CanIgnoreReturnValue
    public <TRegistry> TSelf whenAvailable(Holder.Reference<TRegistry> holder, Consumer<? super TRegistry> action) {
        return whenAvailable(holder.key(), action);
    }

    @CanIgnoreReturnValue
    public <TRegistry, TValue extends TRegistry> TSelf whenAvailable(DeferredHolder<TRegistry, TValue> holder, Consumer<TValue> action) {
        return whenRegistryAvailable(holder.getKey().registryKey(), registry -> action.accept(holder.value()));
    }
    // endregion

    // region Registration
    public <TRegistry, TValue extends TRegistry> ResourceKey<TRegistry> register(ResourceKey<? extends Registry<TRegistry>> registryType, String identifier, Function<Identifier, TValue> factory) {
        if(factories.put(registryType, identifier, factory) != null) {
            throw new IllegalStateException("Duplicate " + registryType.identifier() + " registration: " + registryName(identifier));
        }

        return registryKey(registryType, identifier);
    }

    public <TRegistry, TValue extends TRegistry> ResourceKey<TRegistry> register(ResourceKey<? extends Registry<TRegistry>> registryType, String identifier, Supplier<TValue> factory) {
        return register(registryType, identifier, registryName -> factory.get());
    }

    public <TRegistry, TValue extends TRegistry, THolder extends DeferredHolder<TRegistry, TValue>> THolder registerForHolder(ResourceKey<? extends Registry<TRegistry>> registryType, String identifier, Function<Identifier, TValue> factory, Function<ResourceKey<TRegistry>, THolder> holderFactory) {
        var registryKey = register(registryType, identifier, factory);
        return holderFactory.apply(registryKey);
    }

    public <TRegistry, TValue extends TRegistry, THolder extends DeferredHolder<TRegistry, TValue>> THolder registerForHolder(ResourceKey<? extends Registry<TRegistry>> registryType, String identifier, Supplier<TValue> factory, Function<ResourceKey<TRegistry>, THolder> holderFactory) {
        return registerForHolder(registryType, identifier, registryName -> factory.get(), holderFactory);
    }

    public <TRegistry, TValue extends TRegistry> DeferredHolder<TRegistry, TValue> registerForHolder(ResourceKey<? extends Registry<TRegistry>> registryType, String identifier, Function<Identifier, TValue> factory) {
        return registerForHolder(registryType, identifier, factory, Holders::create);
    }

    public <TRegistry, TValue extends TRegistry> DeferredHolder<TRegistry, TValue> registerForHolder(ResourceKey<? extends Registry<TRegistry>> registryType, String identifier, Supplier<TValue> factory) {
        return registerForHolder(registryType, identifier, registryName -> factory.get());
    }
    // endregion

    // region Item
    public <TItem extends Item> ItemBuilder<TItem> item(String identifier, Function<Item.Properties, TItem> factory) {
        return new ItemBuilder<>(this, identifier, factory);
    }

    public ItemBuilder<Item> item(String identifier) {
        return item(identifier, Item::new);
    }
    // endregion

    // region Block
    public <TBlock extends Block> BlockBuilder<TBlock> block(String identifier, Function<BlockBehaviour.Properties, TBlock> factory) {
        return new BlockBuilder<>(this, identifier, factory);
    }

    public BlockBuilder<Block> block(String identifier) {
        return block(identifier, Block::new);
    }
    // endregion

    // region BlockEntity
    public <TBlockEntity extends BlockEntity> BlockEntityBuilder<TBlockEntity> blockEntity(String identifier, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory) {
        return new BlockEntityBuilder<>(this, identifier, factory);
    }
    // endregion

    // region Entity
    public <TEntity extends Entity> EntityBuilder<TEntity> entity(String identifier, EntityType.EntityFactory<TEntity> factory, MobCategory category) {
        return new EntityBuilder<>(this, identifier, factory, category);
    }
    // endregion

    // region GameRule
    private <TRuleType> GameRuleBuilder<TRuleType> gameRule(String identifier, GameRuleType gameRuleType, ArgumentType<TRuleType> argumentType, GameRules.VisitorCaller<TRuleType> visitorCaller, Codec<TRuleType> codec, ToIntFunction<TRuleType> commandResult, Supplier<TRuleType> defaultValue) {
        return new GameRuleBuilder<>(this, identifier, gameRuleType, argumentType, visitorCaller, codec, commandResult, defaultValue);
    }

    public <TRuleType> GameRuleBuilder<TRuleType> gameRule(String identifier, GameRuleType gameRuleType, ArgumentType<TRuleType> argumentType, Codec<TRuleType> codec, ToIntFunction<TRuleType> commandResult, Supplier<TRuleType> defaultValue) {
        return gameRule(identifier, gameRuleType, argumentType, GameRuleTypeVisitor::visit, codec, commandResult, defaultValue);
    }

    public GameRuleBuilder<Integer> integerGameRule(String identifier, int minimumValue, int maximumValue, int defaultValue) {
        return gameRule(identifier, GameRuleType.INT, IntegerArgumentType.integer(minimumValue, maximumValue), GameRuleTypeVisitor::visitInteger, Codec.intRange(minimumValue, maximumValue), value -> value, () -> defaultValue);
    }

    public GameRuleBuilder<Boolean> booleanGameRule(String identifier, boolean defaultValue) {
        return gameRule(identifier, GameRuleType.BOOL, BoolArgumentType.bool(), GameRuleTypeVisitor::visitBoolean, Codec.BOOL, value -> value ? Command.SINGLE_SUCCESS : 0, () -> defaultValue);
    }

    public GameRuleCategory gameRuleCategory(String identifier) {
        var category = new GameRuleCategory(registryName(identifier));

        if(!gameRuleCategories.add(category)) {
            throw new IllegalStateException("Duplicate GameRuleCategory registration: " + category);
        }

        return category;
    }

    public <TRuleType> TSelf gameRuleEntryFactory(GameRuleType gameRuleType, GameRuleEntryFactory<TRuleType> entryFactory) {
        if(gameRuleEntryFactories.putIfAbsent(gameRuleType, entryFactory) != null) {
            throw new IllegalStateException("Duplicate GameRuleEntryFactory registration of type: " + gameRuleType);
        }

        return self();
    }
    // endregion

    // region FluidType
    public <TFluidType extends FluidType> FluidTypeBuilder<TFluidType> fluidType(String identifier, Function<FluidType.Properties, TFluidType> factory) {
        return new FluidTypeBuilder<>(this, identifier, factory);
    }

    public FluidTypeBuilder<FluidType> fluidType(String identifier) {
        return fluidType(identifier, FluidType::new);
    }
    // endregion

    // region Fluid
    public <TFluid extends Fluid> FluidBuilder<TFluid> fluid(String identifier, Supplier<TFluid> factory) {
        return new FluidBuilder<>(this, identifier, factory);
    }
    // endregion

    // region RecipeSerializer
    public <TRecipe extends Recipe<?>> DeferredRecipeSerializer<TRecipe> recipeSerializer(String identifier, MapCodec<TRecipe> codec, StreamCodec<RegistryFriendlyByteBuf, TRecipe> streamCodec) {
        return registerForHolder(Registries.RECIPE_SERIALIZER, identifier, () -> new RecipeSerializer<>(codec, streamCodec), Holders::createRecipeSerializer);
    }
    // endregion

    // region DataComponent
    public <TType> DeferredDataComponent<TType> dataComponent(String identifier, UnaryOperator<DataComponentType.Builder<TType>> properties) {
        return registerForHolder(Registries.DATA_COMPONENT_TYPE, identifier, () -> properties.apply(DataComponentType.builder()).build(), Holders::createDataComponent);
    }
    // endregion

    // region CreativeModeTab
    public ResourceKey<CreativeModeTab> creativeModeTab(String identifier, UnaryOperator<CreativeModeTab.Builder> modifier) {
        return register(Registries.CREATIVE_MODE_TAB, identifier, registryName -> modifier.apply(CreativeModeTab
                .builder()
                .title(Component.translatable(registryName.toLanguageKey("itemGroup")))
                .displayItems((parameters, output) -> values(Registries.ITEM).forEach(output::accept))
        ).build());
    }

    public ResourceKey<CreativeModeTab> creativeModeTab(String identifier) {
        return creativeModeTab(identifier, UnaryOperator.identity());
    }
    // endregion

    // region Menu
    public <TMenu extends AbstractContainerMenu> MenuBuilder<TMenu> menu(String identifier, MenuType.MenuSupplier<TMenu> factory) {
        return new MenuBuilder<>(this, identifier, factory);
    }
    // endregion

    // region RecipeType
    public <TRecipe extends Recipe<TInput>, TInput extends RecipeInput> RecipeTypeBuilder<TRecipe, TInput> recipeType(String identifier) {
        return new RecipeTypeBuilder<>(this, identifier);
    }
    // endregion

    // region RecipeBookCategory
    public DeferredRecipeBookCategory recipeBookCategory(String identifier) {
        return registerForHolder(Registries.RECIPE_BOOK_CATEGORY, identifier, RecipeBookCategory::new, Holders::createRecipeBookCategory);
    }
    // endregion

    // region Particle
    public <TParticleType extends ParticleType<TOptions>, TOptions extends ParticleOptions> ParticleBuilder<TParticleType, TOptions> particle(String identifier, Supplier<TParticleType> factory) {
        return new ParticleBuilder<>(this, identifier, factory);
    }

    public <TOptions extends ParticleOptions> ParticleBuilder<ParticleType<TOptions>, TOptions> particle(String identifier, MapCodec<TOptions> mapCodec, StreamCodec<RegistryFriendlyByteBuf, TOptions> streamCodec, boolean overrideLimiter) {
        return particle(identifier, () -> new ParticleType<>(overrideLimiter) {
            @Override
            public MapCodec<TOptions> codec() {
                return mapCodec;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, TOptions> streamCodec() {
                return streamCodec;
            }
        });
    }

    public <TOptions extends ParticleOptions> ParticleBuilder<ParticleType<TOptions>, TOptions> particle(String identifier, MapCodec<TOptions> mapCodec, StreamCodec<RegistryFriendlyByteBuf, TOptions> streamCodec) {
        return particle(identifier, mapCodec, streamCodec, false);
    }

    public ParticleBuilder<SimpleParticleType, SimpleParticleType> particle(String identifier, boolean overrideLimiter) {
        return particle(identifier, () -> new SimpleParticleType(overrideLimiter));
    }

    public ParticleBuilder<SimpleParticleType, SimpleParticleType> particle(String identifier) {
        return particle(identifier, false);
    }
    // endregion

    // region Registry
    public <TRegistry> Registry<TRegistry> newRegistry(String identifier, Consumer<RegistryBuilder<TRegistry>> action) {
        var builder = new RegistryBuilder<TRegistry>(ResourceKey.createRegistryKey(registryName(identifier)));
        action.accept(builder);
        var registry = builder.create();
        registries.add(registry);
        return registry;
    }

    public <TRegistry> Registry<TRegistry> newRegistry(String identifier) {
        return newRegistry(identifier, Consumers.nop());
    }

    private <TRegistry> ResourceKey<Registry<TRegistry>> newDynamicRegistry(String identifier, DynamicRegistry<TRegistry> data) {
        if(dynamicRegistries.putIfAbsent(identifier, data) != null) {
            throw new IllegalStateException("Duplicate DynamicRegistry definition: " + registryName(identifier));
        }

        return ResourceKey.createRegistryKey(registryName(identifier));
    }

    public <TRegistry> ResourceKey<Registry<TRegistry>> newDynamicRegistry(String identifier, Codec<TRegistry> codec, Codec<TRegistry> networkCodec, Consumer<RegistryBuilder<TRegistry>> action) {
        return newDynamicRegistry(identifier, new DynamicRegistry<>(codec, networkCodec, action));
    }

    public <TRegistry> ResourceKey<Registry<TRegistry>> newDynamicRegistry(String identifier, Codec<TRegistry> codec, Codec<TRegistry> networkCodec) {
        return newDynamicRegistry(identifier, codec, networkCodec, Consumers.nop());
    }

    public <TRegistry> ResourceKey<Registry<TRegistry>> newDynamicRegistry(String identifier, Codec<TRegistry> codec, Consumer<RegistryBuilder<TRegistry>> action) {
        return newDynamicRegistry(identifier, new DynamicRegistry<>(codec, null, action));
    }

    public <TRegistry> ResourceKey<Registry<TRegistry>> newDynamicRegistry(String identifier, Codec<TRegistry> codec) {
        return newDynamicRegistry(identifier, codec, Consumers.nop());
    }
    // endregion

    // region DataMap
    // exposed for future proofing, the builder maybe expanded in future to have more than just `.synced(Codec<TData>, boolean)`
    public <TRegistry, TData> DataMapType<TRegistry, TData> dataMap(String identifier, ResourceKey<Registry<TRegistry>> registryType, Codec<TData> codec, UnaryOperator<DataMapType.Builder<TData, TRegistry>> action) {
        var dataMapType = action.apply(DataMapType.builder(registryName(identifier), registryType, codec)).build();
        dataMapTypes.add(dataMapType);
        return dataMapType;
    }

    public <TRegistry, TData> DataMapType<TRegistry, TData> dataMap(String identifier, ResourceKey<Registry<TRegistry>> registryType, Codec<TData> codec) {
        return dataMap(identifier, registryType, codec, UnaryOperator.identity());
    }

    public <TRegistry, TData> DataMapType<TRegistry, TData> dataMap(String identifier, ResourceKey<Registry<TRegistry>> registryType, Codec<TData> codec, Codec<TData> networkCodec, boolean mandatory) {
        return dataMap(identifier, registryType, codec, builder -> builder.synced(networkCodec, mandatory));
    }
    // endregion

    // region Event
    @CanIgnoreReturnValue
    public <TEvent extends Event & IModBusEvent> TSelf event(Consumer<TEvent> action) {
        return event0(modBus -> modBus.addListener(action));
    }

    @CanIgnoreReturnValue
    public <TEvent extends Event & IModBusEvent> TSelf event(Class<TEvent> eventType, Consumer<TEvent> action) {
        return event0(modBus -> modBus.addListener(eventType, action));
    }

    @CanIgnoreReturnValue
    public <TEvent extends Event & IModBusEvent> TSelf event(EventPriority priority, Consumer<TEvent> action) {
        return event0(modBus -> modBus.addListener(priority, action));
    }

    @CanIgnoreReturnValue
    public <TEvent extends Event & IModBusEvent> TSelf event(EventPriority priority, Class<TEvent> eventType, Consumer<TEvent> action) {
        return event0(modBus -> modBus.addListener(priority, eventType, action));
    }

    @CanIgnoreReturnValue
    private TSelf event0(Consumer<IEventBus> action) {
        if(modBus == null) {
            deferredEvents = deferredEvents.andThen(action);
        } else {
            action.accept(modBus);
        }

        return self();
    }
    // endregion

    public final void register(IEventBus modBus) {
        deferredEvents.accept(modBus);
        this.modBus = modBus;
    }

    // region Internal
    private void registerEvents(IEventBus modBus) {
        modBus.addListener(EventPriority.HIGH, RegisterEvent.class, event -> register(event.getRegistry()));
        modBus.addListener(EventPriority.LOW, RegisterEvent.class, event -> notifyListeners(event.getRegistry()));
        modBus.addListener(RegisterGameRuleCategoryEvent.class, event -> gameRuleCategories.forEach(event::register));
        modBus.addListener(RegisterGameRuleEntryFactoryEvent.class, event -> gameRuleEntryFactories.forEach(event::register));
        modBus.addListener(RegisterDataMapTypesEvent.class, event -> dataMapTypes.forEach(event::register));
        modBus.addListener(NewRegistryEvent.class, event -> registries.forEach(event::register));

        modBus.addListener(DataPackRegistryEvent.NewRegistry.class, event -> dynamicRegistries.forEach((identifier, registry) -> registry.register(
                event,
                ResourceKey.createRegistryKey(registryName(identifier))
        )));
    }

    @SuppressWarnings("unchecked")
    private <TRegistry> void register(Registry<TRegistry> registry) {
        var registryType = registry.key();
        var itr = factories.row(registryType).entrySet().iterator();

        while(itr.hasNext()) {
            var entry = itr.next();
            var identifier = entry.getKey();
            var registryName = registryName(identifier);
            var value = (TRegistry) entry.getValue().apply(registryName);
            var holder = Registry.registerForHolder(registry, registryName, value);

            holders.put(registryType, identifier, holder);
            values.put(registryType, identifier, value);

            itr.remove();
        }
    }

    @SuppressWarnings("unchecked")
    private <TRegistry> void notifyListeners(Registry<TRegistry> registry) {
        var registryType = registry.key();
        listeners.removeAll(registryType).forEach(listener -> ((Consumer<Registry<TRegistry>>) listener).accept(registry));
        registered.add(registryType);
    }

    @SuppressWarnings("unchecked")
    private TSelf self() {
        return (TSelf) this;
    }
    // endregion

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <TRegistry> Registry<TRegistry> getRegistry(ResourceKey<? extends Registry<TRegistry>> registryType) {
        return (Registry<TRegistry>) BuiltInRegistries.REGISTRY.getValueOrThrow((ResourceKey) registryType);
    }

    record DynamicRegistry<TRegistry>(
            Codec<TRegistry> codec,
            @Nullable Codec<TRegistry> networkRegistry,
            Consumer<RegistryBuilder<TRegistry>> action
    ) {
        private void register(DataPackRegistryEvent.NewRegistry event, ResourceKey<Registry<TRegistry>> registryType) {
            event.dataPackRegistry(
                    registryType,
                    codec,
                    networkRegistry,
                    action
            );
        }
    }
}