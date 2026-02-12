package dev.apexstudios.registree;

import com.google.common.collect.Maps;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.apexstudios.registree.registrar.BlockEntityTypeRegistrar;
import dev.apexstudios.registree.registrar.BlockRegistrar;
import dev.apexstudios.registree.registrar.CreativeModeTabRegistrar;
import dev.apexstudios.registree.registrar.DataComponentTypeRegistrar;
import dev.apexstudios.registree.registrar.EntityTypeRegistrar;
import dev.apexstudios.registree.registrar.FluidRegistrar;
import dev.apexstudios.registree.registrar.FluidTypeRegistrar;
import dev.apexstudios.registree.registrar.GameRuleRegistrar;
import dev.apexstudios.registree.registrar.ItemRegistrar;
import dev.apexstudios.registree.registrar.MenuTypeRegistrar;
import dev.apexstudios.registree.registrar.RecipeBookCategoryRegistrar;
import dev.apexstudios.registree.registrar.RecipeSerializerRegistrar;
import dev.apexstudios.registree.registrar.RecipeTypeRegistrar;
import dev.apexstudios.registree.registrar.Registrar;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.commons.lang3.function.Consumers;
import org.jspecify.annotations.Nullable;

public class Registree {
    private final String namespace;
    private final Map<ResourceKey<? extends Registry<?>>, Registrar<?>> registrars = Maps.newHashMap();
    private final Deferred.Notifiable<IEventBus> modBus = Deferred.create();

    public Registree(String namespace, Consumer<Registrars> registrarsConsumer) {
        this.namespace = namespace;

        createRegistrars(this, registrars -> {
            registrars.with(Registries.ITEM, ItemRegistrar::new);
            registrars.with(Registries.BLOCK, BlockRegistrar::new);
            registrars.with(Registries.BLOCK_ENTITY_TYPE, BlockEntityTypeRegistrar::new);
            registrars.with(Registries.GAME_RULE, GameRuleRegistrar::new);
            registrars.with(Registries.ENTITY_TYPE, EntityTypeRegistrar::new);
            registrars.with(Registries.MENU, MenuTypeRegistrar::new);
            registrars.with(Registries.DATA_COMPONENT_TYPE, DataComponentTypeRegistrar::new);
            registrars.with(Registries.CREATIVE_MODE_TAB, CreativeModeTabRegistrar::new);
            registrars.with(Registries.RECIPE_SERIALIZER, RecipeSerializerRegistrar::new);
            registrars.with(NeoForgeRegistries.Keys.FLUID_TYPES, FluidTypeRegistrar::new);
            registrars.with(Registries.FLUID, FluidRegistrar::new);
            registrars.with(Registries.RECIPE_TYPE, RecipeTypeRegistrar::new);
            registrars.with(Registries.RECIPE_BOOK_CATEGORY, RecipeBookCategoryRegistrar::new);

            registrarsConsumer.accept(registrars);
        });

        event(EventPriority.HIGH, RegisterEvent.class, event -> registrars.values().forEach(registrar -> registrar.onRegister(event, true)));
        event(EventPriority.LOW, RegisterEvent.class, event -> registrars.values().forEach(registrar -> registrar.onRegister(event, false)));
    }

    public String namespace() {
        return namespace;
    }

    public Identifier registryName(String identifier) {
        return Identifier.fromNamespaceAndPath(namespace(), identifier);
    }

    public String registryId(String identifier) {
        return namespace() + Identifier.NAMESPACE_SEPARATOR + identifier;
    }

    public <TRegistry> Registrar<TRegistry> registrarOrCreate(ResourceKey<? extends Registry<TRegistry>> registryType) {
        var registrar = registrarOrNull(registryType);

        if(registrar == null) {
            registrar = new Registrar<>(this, registryType);
            registrars.put(registryType, registrar);
        }

        return registrar;
    }

    @SuppressWarnings("unchecked")
    public <TRegistry> @Nullable Registrar<TRegistry> registrarOrNull(ResourceKey<? extends Registry<TRegistry>> registryType) {
        return (Registrar<TRegistry>) registrars.get(registryType);
    }

    public <TRegistry> Registrar<TRegistry> registrarOrThrow(ResourceKey<? extends Registry<TRegistry>> registryType) {
        return Objects.requireNonNull(registrarOrNull(registryType));
    }

    public ItemRegistrar items() {
        return (ItemRegistrar) registrarOrThrow(Registries.ITEM);
    }

    public BlockRegistrar blocks() {
        return (BlockRegistrar) registrarOrThrow(Registries.BLOCK);
    }

    public BlockEntityTypeRegistrar blockEntityTypes() {
        return (BlockEntityTypeRegistrar) registrarOrThrow(Registries.BLOCK_ENTITY_TYPE);
    }

    public GameRuleRegistrar gameRules() {
        return (GameRuleRegistrar) registrarOrThrow(Registries.GAME_RULE);
    }

    public EntityTypeRegistrar entityTypes() {
        return (EntityTypeRegistrar) registrarOrThrow(Registries.ENTITY_TYPE);
    }

    public MenuTypeRegistrar menuTypes() {
        return (MenuTypeRegistrar) registrarOrThrow(Registries.MENU);
    }

    public DataComponentTypeRegistrar dataComponentTypes() {
        return (DataComponentTypeRegistrar) registrarOrThrow(Registries.DATA_COMPONENT_TYPE);
    }

    public CreativeModeTabRegistrar creativeModeTabs() {
        return (CreativeModeTabRegistrar) registrarOrThrow(Registries.CREATIVE_MODE_TAB);
    }

    public RecipeSerializerRegistrar recipeSerializers() {
        return (RecipeSerializerRegistrar) registrarOrThrow(Registries.RECIPE_SERIALIZER);
    }

    public FluidTypeRegistrar fluidTypes() {
        return (FluidTypeRegistrar) registrarOrThrow(NeoForgeRegistries.Keys.FLUID_TYPES);
    }

    public FluidRegistrar fluids() {
        return (FluidRegistrar) registrarOrThrow(Registries.FLUID);
    }

    public RecipeTypeRegistrar recipeTypes() {
        return (RecipeTypeRegistrar) registrarOrThrow(Registries.RECIPE_TYPE);
    }

    public RecipeBookCategoryRegistrar recipeBookCategories() {
        return (RecipeBookCategoryRegistrar) registrarOrThrow(Registries.RECIPE_BOOK_CATEGORY);
    }

    public void registerEvents(IEventBus modBus) {
        this.modBus.notify(modBus);
    }

    public Deferred<IEventBus> eventBus() {
        return modBus.readOnly();
    }

    public <TEvent extends Event & IModBusEvent> void event(Consumer<TEvent> consumer) {
        modBus.whenAvailable(bus -> bus.addListener(consumer));
    }

    public <TEvent extends Event & IModBusEvent> void event(Class<TEvent> eventType, Consumer<TEvent> consumer) {
        eventBus().whenAvailable(bus -> bus.addListener(eventType, consumer));
    }

    public <TEvent extends Event & IModBusEvent> void event(EventPriority priority, Consumer<TEvent> consumer) {
        eventBus().whenAvailable(bus -> bus.addListener(priority, consumer));
    }

    public <TEvent extends Event & IModBusEvent> void event(EventPriority priority, Class<TEvent> eventType, Consumer<TEvent> consumer) {
        eventBus().whenAvailable(bus -> bus.addListener(priority, eventType, consumer));
    }

    public <TEvent extends Event & IModBusEvent> void event(EventPriority priority, boolean receiveCanceled, Consumer<TEvent> consumer) {
        eventBus().whenAvailable(bus -> bus.addListener(priority, receiveCanceled, consumer));
    }

    public <TEvent extends Event & IModBusEvent> void event(EventPriority priority, boolean receiveCanceled, Class<TEvent> eventType, Consumer<TEvent> consumer) {
        eventBus().whenAvailable(bus -> bus.addListener(priority, receiveCanceled, eventType, consumer));
    }

    public <TEvent extends Event & IModBusEvent> void event(boolean receiveCanceled, Consumer<TEvent> consumer) {
        eventBus().whenAvailable(bus -> bus.addListener(receiveCanceled, consumer));
    }

    public <TEvent extends Event & IModBusEvent> void event(boolean receiveCanceled, Class<TEvent> eventType, Consumer<TEvent> consumer) {
        eventBus().whenAvailable(bus -> bus.addListener(receiveCanceled, eventType, consumer));
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }

        if(!(obj instanceof Registree other)) {
            return false;
        }

        return namespace().equals(other.namespace());
    }

    @Override
    public int hashCode() {
        return namespace().hashCode();
    }

    @Override
    public String toString() {
        return "registree(" + namespace() + ')';
    }

    public static Registree create(String namespace, Consumer<Registrars> registrarsConsumer) {
        return new Registree(namespace, registrarsConsumer);
    }

    public static Registree create(String namespace) {
        return create(namespace, Consumers.nop());
    }

    private static void createRegistrars(Registree registree, Consumer<Registrars> registrarsConsumer) {
        var factories = Maps.<ResourceKey<? extends Registry<?>>, Function<Registree, ? extends Registrar<?>>>newHashMap();

        registrarsConsumer.accept(new Registrars() {
            @Override
            public <TRegistry> Registrars with(ResourceKey<? extends Registry<TRegistry>> registryType, Function<Registree, ? extends Registrar<TRegistry>> factory) {
                factories.put(registryType, factory);
                return this;
            }
        });

        factories.forEach((registryType, factory) -> registree.registrars.put(registryType, factory.apply(registree)));
        factories.clear();
    }

    @FunctionalInterface
    public interface Registrars {
        @CanIgnoreReturnValue
        <TRegistry> Registrars with(ResourceKey<? extends Registry<TRegistry>> registryType, Function<Registree, ? extends Registrar<TRegistry>> factory);
    }
}
