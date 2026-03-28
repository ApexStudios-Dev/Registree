package dev.apexstudios.registree.xplat;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public abstract class SimpleRegistree implements Registree {
    protected final String namespace;

    private final Table<ResourceKey<? extends Registry<?>>, String, Holder.Reference<?>> holders = HashBasedTable.create();
    private final Table<ResourceKey<? extends Registry<?>>, String, Function<Identifier, ?>> factories = HashBasedTable.create();
    private final Table<ResourceKey<? extends Registry<?>>, String, Consumer<?>> listeners = HashBasedTable.create();
    private final Set<ResourceKey<? extends Registry<?>>> registered = Sets.newHashSet();
    private final Set<ResourceKey<? extends Registry<?>>> finalized = Sets.newHashSet();
    protected boolean frozen = false;

    protected SimpleRegistree(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String namespace() {
        return namespace;
    }

    @Override
    public Stream<ResourceKey<? extends Registry<?>>> listRegistries() {
        return holders.rowKeySet().stream();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <TRegistry> Optional<Holder.Reference<TRegistry>> get(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName) {
        return Optional.ofNullable((Holder.Reference<TRegistry>) holders.get(registryType, registryName));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <TRegistry> Stream<Holder.Reference<TRegistry>> listElements(ResourceKey<? extends Registry<TRegistry>> registryType) {
        return holders.row(registryType).values().stream().map(holder -> (Holder.Reference<TRegistry>) holder);
    }

    @Override
    public <TRegistry> boolean containsKey(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName) {
        return holders.contains(registryType, registryName) || factories.contains(registryType, registryName);
    }

    @Override
    public <TRegistry> void listenFor(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Consumer<? super TRegistry> listener) {
        if(finalized.contains(registryType))
            getOptional(registryType, registryName).ifPresent(listener);
        else
            listeners.put(registryType, registryName, listener);
    }

    @Override
    public boolean isRegistered(ResourceKey<? extends Registry<?>> registryType) {
        return frozen || registered.contains(registryType) || finalized.contains(registryType);
    }

    @Override
    public boolean isRegistered() {
        return frozen;
    }

    @Override
    public <TRegistry> ResourceKey<TRegistry> register(ResourceKey<? extends Registry<TRegistry>> registryType, String registryName, Function<Identifier, ? extends TRegistry> factory) {
        if(registered.contains(registryType))
            throw new IllegalStateException("Registree is already frozen: " + namespace + '#' + registryType.identifier());
        if(factories.put(registryType, registryName, factory) != null)
            throw new IllegalStateException("Duplicate registration: " + registryName + " in registry: " + namespace + '#' + registryType.identifier());

        return registryKey(registryType, registryName);
    }

    @SuppressWarnings("unchecked")
    protected <TRegistry> void register(Registry<TRegistry> registry) {
        var registryType = registry.key();

        if(!registered.add(registryType))
            throw new IllegalStateException("Duplicate registry registration: " + namespace + '#' + registryType.identifier());

        factories.row(registryType).forEach((registryName, factory) -> {
            var fullName = registryName(registryName);
            var holder = Registry.registerForHolder(registry, fullName, (TRegistry) factory.apply(fullName));
            holders.put(registryType, registryName, holder);
        });
    }

    @SuppressWarnings("unchecked")
    protected <TRegistry> void invokeListeners(Registry<TRegistry> registry) {
        var registryType = registry.key();

        if(!registered.contains(registryType))
            throw new IllegalStateException("Can not finalize registry before elements are registered: " + namespace + '#' + registryType.identifier());
        if(!finalized.add(registryType))
            throw new IllegalStateException("Duplicate registry finalization: " + namespace + '#' + registryType.identifier());

        listeners.row(registryType).forEach((registryName, listener) -> getOptional(registryType, registryName).ifPresent((Consumer<? super TRegistry>) listener));
    }
}
