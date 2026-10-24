package dev.apexstudios.registree.core;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import dev.apexstudios.registree.api.Registrar;
import dev.apexstudios.registree.api.RegistrationFactory;
import dev.apexstudios.registree.api.Registree;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

final class RegistrarImpl<TRegistry> implements Registrar<TRegistry> {
    private final Registree registree;
    private final ResourceKey<? extends Registry<TRegistry>> registryType;
    private final Map<String, Holder.Reference<TRegistry>> holderById = new LinkedHashMap<>();
    private final Map<String, TRegistry> valueById = new LinkedHashMap<>();
    private final Map<String, RegistrationFactory<TRegistry, ? extends TRegistry>> registrations = new LinkedHashMap<>();
    private final Multimap<ResourceKey<TRegistry>, Consumer<? super TRegistry>> listeners = MultimapBuilder.linkedHashKeys().linkedHashSetValues().build();
    private @Nullable Registry<TRegistry> registry = null;

    RegistrarImpl(Registree registree, ResourceKey<? extends Registry<TRegistry>> registryType) {
        this.registree = registree;
        this.registryType = registryType;
    }

    @Override
    public Registree registree() {
        return registree;
    }

    @Override
    public ResourceKey<? extends Registry<TRegistry>> registryType() {
        return registryType;
    }

    @Override
    public Holder.@Nullable Reference<TRegistry> getHolderOrNull(String identifier) {
        return holderById.get(identifier);
    }

    @Override
    public @Nullable TRegistry getValueOrNull(String identifier) {
        return valueById.get(identifier);
    }

    @Override
    public Collection<Holder.Reference<TRegistry>> holders() {
        return Collections.unmodifiableCollection(holderById.values());
    }

    @Override
    public Collection<? extends TRegistry> values() {
        return Collections.unmodifiableCollection(valueById.values());
    }

    @Override
    public Set<String> keys() {
        return Collections.unmodifiableSet(holderById.keySet());
    }

    @Override
    public Registrar<TRegistry> whenAvailable(ResourceKey<TRegistry> registryKey, Consumer<? super TRegistry> action) {
        if(registry == null) {
            listeners.put(registryKey, action);
        } else {
            var value = registry.getValue(registryKey);

            if(value != null) {
                action.accept(value);
            }
        }

        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(registree, registryType);
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }

        if(obj instanceof Registrar<?> other) {
            return registree.equals(other.registree()) && registryType.equals(other.registryType());
        }

        return false;
    }

    @Override
    public String toString() {
        return "Registrar{" + registryType + '}';
    }

    public <TValue extends TRegistry> void register(String identifier, RegistrationFactory<TRegistry, TValue> factory) {
        if(registry != null) {
            throw new IllegalStateException("Registrar[" + registryType.identifier() + "] is already frozen (trying to add key " + registree.id(identifier) + ')');
        }

        if(registrations.put(identifier, factory) != null) {
            throw new IllegalStateException("Adding duplicate key '" + registree.id(identifier) + "' to registrar '" + registryType.identifier() + "'");
        }
    }

    public void register(Registry<TRegistry> registry) {
        if(this.registry != null) {
            throw new IllegalStateException("Registrar[" + registryType.identifier() + "] is already frozen");
        }

        for(var entry : registrations.entrySet()) {
            var identifier = entry.getKey();
            var registration = new RegistrationImpl<>(this, identifier, entry.getValue());
            var value = registration.value();
            var holder = Registry.registerForHolder(registry, registration.registryKey(), value);

            if(holderById.putIfAbsent(identifier, holder) != null) {
                throw new IllegalStateException("Adding duplicate key '" + registration.registryKey().identifier() + "' to registrar '" + registryType.identifier() + "'");
            }

            if(valueById.putIfAbsent(identifier, value) != null) {
                throw new IllegalStateException("Adding duplicate key '" + registration.registryKey().identifier() + "' to registrar '" + registryType.identifier() + "'");
            }
        }

        registrations.clear();
        this.registry = registry;
    }

    public void notifyListeners() {
        if(registry == null) {
            throw new IllegalStateException("Attempt to notify registrar listeners before registration has occurred (" + registryType.identifier() + ')');
        }

        for(var registryKey : listeners.keySet()) {
            var value = registry.getValue(registryKey);

            if(value == null) {
                continue;
            }

            for(var listener : listeners.get(registryKey)) {
                listener.accept(value);
            }
        }

        listeners.clear();
    }
}
