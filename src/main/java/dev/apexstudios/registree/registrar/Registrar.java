package dev.apexstudios.registree.registrar;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import dev.apexstudios.registree.Registree;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public class Registrar<TRegistry> implements Iterable<TRegistry>, Keyable {
    private final Registree registree;
    private final ResourceKey<? extends Registry<TRegistry>> registryType;
    private final Map<String, Function<Identifier, ? extends TRegistry>> factories = Maps.newHashMap();
    private final Map<String, Holder.Reference<TRegistry>> holders = Maps.newHashMap();
    private HolderLookup.@Nullable RegistryLookup<TRegistry> backend = null;
    private final Multimap<ResourceKey<TRegistry>, Consumer<? super TRegistry>> callbacks = HashMultimap.create();

    public Registrar(Registree registree, ResourceKey<? extends Registry<TRegistry>> registryType) {
        this.registree = registree;
        this.registryType = registryType;
    }

    public Registree registree() {
        return registree;
    }

    public String namespace() {
        return registree().namespace();
    }

    public ResourceKey<? extends Registry<TRegistry>> registryType() {
        return registryType;
    }

    public Identifier registryName(String identifier) {
        return registree().registryName(identifier);
    }

    public String registryId(String identifier) {
        return registree().registryId(identifier);
    }

    public ResourceKey<TRegistry> registryKey(Identifier registryName) {
        return ResourceKey.create(registryType(), registryName);
    }

    public ResourceKey<TRegistry> registryKey(String identifier) {
        return registryKey(registryName(identifier));
    }

    public TagKey<TRegistry> tag(Identifier identifier) {
        return TagKey.create(registryType(), identifier);
    }

    public TagKey<TRegistry> tag(String path) {
        return tag(registryName(path));
    }

    public TagKey<TRegistry> neoforgeTag(String path) {
        return tag(Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, path));
    }

    public TagKey<TRegistry> conventionTag(String path) {
        return tag(Identifier.fromNamespaceAndPath("c", path));
    }

    public int size() {
        return holders.size();
    }

    @Override
    public <TValue> Stream<TValue> keys(DynamicOps<TValue> ops) {
        return keySet().stream().map(ops::createString);
    }

    public @Nullable TRegistry getValue(String identifier) {
        var holder = get(identifier);
        return holder == null ? null : holder.value();
    }

    public Optional<TRegistry> getValueOptional(String identifier) {
        return getOptional(identifier).map(Holder::value);
    }

    public TRegistry getValueOrThrow(String identifier) {
        return getOrThrow(identifier).value();
    }

    public Set<String> keySet() {
        return Collections.unmodifiableSet(holders.keySet());
    }

    public Set<Map.Entry<String, Holder.Reference<TRegistry>>> entrySet() {
        return Collections.unmodifiableSet(holders.entrySet());
    }

    public Stream<TRegistry> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    public boolean containsKey(String identifier) {
        return holders.containsKey(identifier) || factories.containsKey(identifier);
    }

    public boolean containsKey(ResourceKey<TRegistry> registryKey) {
        return registryKey.isFor(registryType()) && containsKey(registryKey);
    }

    public boolean containsKey(Identifier registryName) {
        return registryName.getNamespace().equals(namespace()) && containsKey(registryName.getPath());
    }

    public boolean contains(Holder<TRegistry> holder) {
        var key = holder.getKey();
        return key != null && containsKey(key);
    }

    public Holder.@Nullable Reference<TRegistry> get(String identifier) {
        return holders.get(identifier);
    }

    @SuppressWarnings("NullableProblems")
    public Optional<Holder.Reference<TRegistry>> getOptional(String identifier) {
        return Optional.ofNullable(get(identifier));
    }

    public Holder.Reference<TRegistry> getOrThrow(String identifier) {
        return Objects.requireNonNull(get(identifier), () -> "Missing registration in " + this + ": " + identifier);
    }

    @Override
    public Iterator<TRegistry> iterator() {
        return Iterators.transform(holders.values().iterator(), Holder::value);
    }

    public void listenFor(ResourceKey<TRegistry> registryKey, Consumer<? super TRegistry> action) {
        if(backend == null) {
            callbacks.put(registryKey, action);
        } else {
            backend.get(registryKey).map(Holder::value).ifPresent(action);
        }
    }

    public void listenFor(Identifier registryName, Consumer<? super TRegistry> action) {
        listenFor(registryKey(registryName), action);
    }

    public void listenFor(String identifier, Consumer<? super TRegistry> action) {
        listenFor(registryKey(identifier), action);
    }

    public void listenFor(Holder<TRegistry> holder, Consumer<? super TRegistry> action) {
        if(holder.isBound()) {
            action.accept(holder.value());
        } else {
            var key = holder.getKey();

            if(key != null) {
                listenFor(key, action);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <TValue extends TRegistry> void listenFor(DeferredHolder<TRegistry, TValue> holder, Consumer<TValue> action) {
        if(holder.isBound()) {
            action.accept(holder.value());
        } else {
            listenFor(holder.getKey(), value -> action.accept((TValue) value));
        }
    }

    public void registerBasic(String identifier, Function<Identifier, ? extends TRegistry> factory) {
        if(factories.putIfAbsent(identifier, factory) != null) {
            throw new IllegalStateException("Duplicate " + this + " registration: " + identifier);
        }
    }

    public void registerBasic(String identifier, Supplier<? extends TRegistry> factory) {
        registerBasic(identifier, registryName -> factory.get());
    }

    public ResourceKey<TRegistry> registerForKey(String identifier, Function<Identifier, ? extends TRegistry> factory) {
        registerBasic(identifier, factory);
        return registryKey(identifier);
    }

    public ResourceKey<TRegistry> registerForKey(String identifier, Supplier<? extends TRegistry> factory) {
        return registerForKey(identifier, registryName -> factory.get());
    }

    public <TValue extends TRegistry, THolder extends DeferredHolder<TRegistry, TValue>> THolder registerForHolder(String identifier, Function<Identifier, TValue> factory, Function<ResourceKey<TRegistry>, THolder> holderFactory) {
        return holderFactory.apply(registerForKey(identifier, factory));
    }

    public <TValue extends TRegistry, THolder extends DeferredHolder<TRegistry, TValue>> THolder registerForHolder(String identifier, Supplier<TValue> factory, Function<ResourceKey<TRegistry>, THolder> holderFactory) {
        return registerForHolder(identifier, registryName -> factory.get(), holderFactory);
    }

    public <TValue extends TRegistry> DeferredHolder<TRegistry, TValue> registerForHolder(String identifier, Function<Identifier, TValue> factory) {
        return registerForHolder(identifier, factory, DeferredHolder::create);
    }

    public <TValue extends TRegistry> DeferredHolder<TRegistry, TValue> registerForHolder(String identifier, Supplier<TValue> factory) {
        return registerForHolder(identifier, registryName -> factory.get());
    }

    @ApiStatus.Internal
    public void onRegister(RegisterEvent event, boolean pre) {
        var registry = event.getRegistry(registryType());

        if(registry == null) {
            return;
        }

        if(pre) {
            register(registry);
        } else {
            notifyListeners();
        }
    }

    private void register(Registry<TRegistry> registry) {
        if(backend != null) {
            throw new IllegalStateException(this + " is already frozen!");
        }

        backend = registry;

        if(factories.isEmpty()) {
            return;
        }

        factories.forEach((identifier, factory) -> {
            var registryKey = registryKey(identifier);
            var value = factory.apply(registryKey.identifier());
            var holder = Registry.registerForHolder(registry, registryKey, value);

            if(holders.putIfAbsent(identifier, holder) != null) {
                throw new IllegalStateException("Duplicate " + this + " holder registration: " + identifier);
            }
        });

        factories.clear();
    }

    private void notifyListeners() {
        if(callbacks.isEmpty()) {
            return;
        }

        if(backend == null) {
            throw new IllegalStateException("Attemp to notify listener before registration has finalized: " + this);
        }

        for(var registryKey : callbacks.keySet()) {
            backend.get(registryKey)
                    .map(Holder::value)
                    .ifPresent(value -> callbacks.get(registryKey)
                            .forEach(callback -> callback.accept(value))
                    );
        }

        callbacks.clear();
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }

        if(!(obj instanceof Registrar<?> other)) {
            return false;
        }

        return namespace().equals(other.namespace()) && registryType().equals(other.registryType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace(), registryType());
    }

    @Override
    public String toString() {
        return "registrar(" + namespace() + '#' + registryType().identifier() + ')';
    }
}
