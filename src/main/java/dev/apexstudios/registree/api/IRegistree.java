package dev.apexstudios.registree.api;

import dev.apexstudios.registree.api.registrar.IBlockEntityTypeRegistrar;
import dev.apexstudios.registree.api.registrar.IBlockRegistrar;
import dev.apexstudios.registree.api.registrar.ICreativeModeTabRegistrar;
import dev.apexstudios.registree.api.registrar.IEntityTypeRegistrar;
import dev.apexstudios.registree.api.registrar.IGameRuleRegistrar;
import dev.apexstudios.registree.api.registrar.IItemRegistrar;
import dev.apexstudios.registree.api.registrar.IMenuTypeRegistrar;
import dev.apexstudios.registree.api.registrar.IRegistrar;
import dev.apexstudios.registree.core.Registree;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.IModBusEvent;

public interface IRegistree {
    String namespace();

    default String registryId(String identifier) {
        return namespace() + Identifier.NAMESPACE_SEPARATOR + identifier;
    }

    default Identifier registryName(String identifier) {
        return Identifier.fromNamespaceAndPath(namespace(), identifier);
    }

    <TRegistry> IRegistrar<TRegistry> registrar(ResourceKey<? extends Registry<TRegistry>> registryType);

    default IBlockRegistrar blocks() {
        return (IBlockRegistrar) registrar(Registries.BLOCK);
    }

    default IItemRegistrar items() {
        return (IItemRegistrar) registrar(Registries.ITEM);
    }

    default IBlockEntityTypeRegistrar blockEntityTypes() {
        return (IBlockEntityTypeRegistrar) registrar(Registries.BLOCK_ENTITY_TYPE);
    }

    default IEntityTypeRegistrar entityTypes() {
        return (IEntityTypeRegistrar) registrar(Registries.ENTITY_TYPE);
    }

    default IMenuTypeRegistrar menuTypes() {
        return (IMenuTypeRegistrar) registrar(Registries.MENU);
    }

    default IGameRuleRegistrar gameRules() {
        return (IGameRuleRegistrar) registrar(Registries.GAME_RULE);
    }

    default ICreativeModeTabRegistrar creativeModeTabs() {
        return (ICreativeModeTabRegistrar) registrar(Registries.CREATIVE_MODE_TAB);
    }

    void register(IEventBus modBus);

    <TEvent extends Event & IModBusEvent> void event(EventPriority priority, boolean receiveCanceled, Class<TEvent> eventType, Consumer<TEvent> listener);

    <TEvent extends Event & IModBusEvent> void event(EventPriority priority, boolean receiveCanceled, Consumer<TEvent> listener);

    default <TEvent extends Event & IModBusEvent> void event(boolean receiveCanceled, Class<TEvent> eventType, Consumer<TEvent> listener) {
        event(EventPriority.NORMAL, receiveCanceled, eventType, listener);
    }

    default <TEvent extends Event & IModBusEvent> void event(boolean receiveCanceled, Consumer<TEvent> listener) {
        event(EventPriority.NORMAL, receiveCanceled, listener);
    }

    default <TEvent extends Event & IModBusEvent> void event(EventPriority priority, Class<TEvent> eventType, Consumer<TEvent> listener) {
        event(priority, false, eventType, listener);
    }

    default <TEvent extends Event & IModBusEvent> void event(EventPriority priority, Consumer<TEvent> listener) {
        event(priority, false, listener);
    }

    default <TEvent extends Event & IModBusEvent> void event(Class<TEvent> eventType, Consumer<TEvent> listener) {
        event(EventPriority.NORMAL, false, eventType, listener);
    }

    default <TEvent extends Event & IModBusEvent> void event(Consumer<TEvent> listener) {
        event(EventPriority.NORMAL, false, listener);
    }

    static IRegistree create(String namespace) {
        return new Registree(namespace);
    }
}
