package dev.apexstudios.registree.core;

import com.google.common.collect.Maps;
import dev.apexstudios.registree.api.IRegistree;
import dev.apexstudios.registree.api.holder.Holders;
import dev.apexstudios.registree.api.registrar.IRegistrar;
import dev.apexstudios.registree.core.registrar.BlockEntityTypeRegistrar;
import dev.apexstudios.registree.core.registrar.BlockRegistrar;
import dev.apexstudios.registree.core.registrar.EntityTypeRegistrar;
import dev.apexstudios.registree.core.registrar.GameRuleRegistrar;
import dev.apexstudios.registree.core.registrar.ItemRegistrar;
import dev.apexstudios.registree.core.registrar.MenuTypeRegistrar;
import dev.apexstudios.registree.core.registrar.Registrar;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.IModBusEvent;
import org.jspecify.annotations.Nullable;

public class Registree implements IRegistree {
    private final String namespace;
    private final Map<ResourceKey<? extends Registry<?>>, LazyRegistrar<?>> registrars = Maps.newHashMap();
    private final Deferred<IEventBus> modBus = new Deferred<>();

    public Registree(String namespace) {
        this.namespace = namespace;

        setRegistrar(Registries.BLOCK, () -> new BlockRegistrar(this));
        setRegistrar(Registries.ITEM, () -> new ItemRegistrar(this));
        setRegistrar(Registries.BLOCK_ENTITY_TYPE, () -> new BlockEntityTypeRegistrar(this));
        setRegistrar(Registries.ENTITY_TYPE, () -> new EntityTypeRegistrar(this));
        setRegistrar(Registries.MENU, () -> new MenuTypeRegistrar(this));
        setRegistrar(Registries.GAME_RULE, () -> new GameRuleRegistrar(this));
    }

    protected <TRegistry> void setRegistrar(ResourceKey<? extends Registry<TRegistry>> registryType, Supplier<IRegistrar<TRegistry, ?>> registrar) {
        getRegistrar(registryType).factory = registrar;
    }

    protected void defer(Consumer<IEventBus> listener) {
        modBus.defer(listener);
    }

    @Override
    public String namespace() {
        return namespace;
    }

    @Override
    public <TRegistry> IRegistrar<TRegistry, ?> registrar(ResourceKey<? extends Registry<TRegistry>> registryType) {
        return getRegistrar(registryType).get(registryType);
    }

    @Override
    public void register(IEventBus modBus) {
        this.modBus.invoke(modBus);
    }

    @Override
    public <TEvent extends Event & IModBusEvent> void event(EventPriority priority, boolean receiveCanceled, Class<TEvent> eventType, Consumer<TEvent> listener) {
        defer(modBus -> modBus.addListener(priority, receiveCanceled, eventType, listener));
    }

    @Override
    public <TEvent extends Event & IModBusEvent> void event(EventPriority priority, boolean receiveCanceled, Consumer<TEvent> listener) {
        defer(modBus -> modBus.addListener(priority, receiveCanceled, listener));
    }

    @SuppressWarnings("unchecked")
    private <TRegistry> LazyRegistrar<TRegistry> getRegistrar(ResourceKey<? extends Registry<TRegistry>> registryType) {
        return (LazyRegistrar<TRegistry>) registrars.computeIfAbsent(registryType, $ -> new LazyRegistrar<>());
    }

    // wrapper class to allow people to override our default registrar types
    // registrar instances are constructed on first call to #registrar
    // use #setRegistrar in your constructor to override the backing type
    // and overload the desired #registrar methods to return the correct types
    // for example custom BlockRegistrar
    // Call in constructor: `setRegistrar(Registries.BLOCK, () -> new MyBlockRegistrar(this));`
    // Overload: ``public MyBlockRegistrar blocks() { return (MyBlockRegistrar) IRegistree.super.blocks(); }
    private final class LazyRegistrar<TRegistry> {
        @Nullable private IRegistrar<TRegistry, ?> registrar = null;
        @Nullable private Supplier<IRegistrar<TRegistry, ?>> factory;

        public IRegistrar<TRegistry, ?> get(ResourceKey<? extends Registry<TRegistry>> registryType) {
            if(registrar == null) {
                if(factory == null) {
                    registrar = new Registrar<>(Registree.this, registryType, Holders::create);
                } else {
                    registrar = factory.get();
                    factory = null;
                }
            }

            return registrar;
        }
    }
}
