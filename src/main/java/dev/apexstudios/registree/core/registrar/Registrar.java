package dev.apexstudios.registree.core.registrar;

import com.google.common.collect.Maps;
import dev.apexstudios.registree.api.IRegistree;
import dev.apexstudios.registree.api.holder.DeferredHolder;
import dev.apexstudios.registree.api.registrar.IRegistrar;
import dev.apexstudios.registree.core.Deferred;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

public class Registrar<TRegistry> implements IRegistrar<TRegistry> {
    private final IRegistree registree;
    private final ResourceKey<? extends Registry<TRegistry>> registryType;
    private final Map<String, TRegistry> valueById = Maps.newHashMap();
    private final Map<String, Function<Identifier, TRegistry>> factories = Maps.newHashMap();
    private final Deferred<Registry<TRegistry>> registry = new Deferred<>();

    public Registrar(IRegistree registree, ResourceKey<? extends Registry<TRegistry>> registryType) {
        this.registree = registree;
        this.registryType = registryType;

        registree.event(EventPriority.HIGH, RegisterEvent.class, event -> event.register(registryType, $ -> {
            var registry = Objects.requireNonNull(event.getRegistry(registryType));

            factories.forEach((identifier, factory) -> registerValue(registry, registryKey(identifier), factory.apply(registryName(identifier))));
            factories.clear();
        }));

        registree.event(EventPriority.LOW, RegisterEvent.class, event -> event.register(registryType, $ -> registry.invoke(Objects.requireNonNull(event.getRegistry(registryType)))));
    }

    @MustBeInvokedByOverriders
    protected Holder.Reference<TRegistry> registerValue(Registry<TRegistry> registry, ResourceKey<TRegistry> registryKey, TRegistry value) {
        var holder = Registry.registerForHolder(registry, registryKey, value);

        if(valueById.putIfAbsent(registryKey.identifier().getPath(), value) != null) {
            throw Util.pauseInIde(new IllegalStateException("Illegal " + registryType().identifier() + " registration: " + registryKey.identifier() + " (Duplicate entry)"));
        }

        return holder;
    }

    protected void defer(Consumer<Registry<TRegistry>> listener) {
        registry.defer(listener);
    }

    @Override
    public IRegistree registree() {
        return registree;
    }

    @Override
    public ResourceKey<? extends Registry<TRegistry>> registryType() {
        return registryType;
    }

    @Override
    public @Nullable TRegistry get(String identifier) {
        return valueById.get(identifier);
    }

    @Override
    public Collection<TRegistry> values() {
        return Collections.unmodifiableCollection(valueById.values());
    }

    @Override
    public Set<String> keySet() {
        return Collections.unmodifiableSet(valueById.keySet());
    }

    @Override
    public boolean containsKey(String identifier) {
        return valueById.containsKey(identifier);
    }

    @Override
    public boolean containsValue(TRegistry value) {
        return valueById.containsValue(value);
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void onRegister(ResourceKey<TRegistry> registryKey, Consumer<TRegistry> listener) {
        defer(registry -> {
            var value = registry.getValue(registryKey);

            if(value != null) {
                listener.accept(value);
            }
        });
    }

    @Override
    public ResourceKey<TRegistry> register(String identifier, Function<Identifier, TRegistry> factory) {
        if(containsKey(identifier) || factories.putIfAbsent(identifier, factory) != null) {
            throw Util.pauseInIde(new IllegalStateException("Illegal " + registryType().identifier() + " registration: " + registryName(identifier) + " (Duplicate entry)"));
        }

        return registryKey(identifier);
    }

    public static class WithHolder<TRegistry, THolderType extends DeferredHolder<TRegistry, ? extends TRegistry>> extends Registrar<TRegistry> implements IRegistrar.WithHolder<TRegistry, THolderType> {
        private final Map<String, Holder.Reference<TRegistry>> holderById = Maps.newHashMap();
        private final Function<ResourceKey<TRegistry>, THolderType> holderFactory;

        public WithHolder(IRegistree registree, ResourceKey<? extends Registry<TRegistry>> registryType, Function<ResourceKey<TRegistry>, THolderType> holderFactory) {
            super(registree, registryType);

            this.holderFactory = holderFactory;
        }

        @Override
        protected Holder.Reference<TRegistry> registerValue(Registry<TRegistry> registry, ResourceKey<TRegistry> registryKey, TRegistry value) {
            var holder = super.registerValue(registry, registryKey, value);

            if(holderById.putIfAbsent(registryKey.identifier().getPath(), holder) != null) {
                throw Util.pauseInIde(new IllegalStateException("Illegal " + registryType().identifier() + " registration: " + registryKey.identifier() + " (Duplicate entry)"));
            }

            return holder;
        }

        @Override
        public boolean containsKey(String identifier) {
            return super.containsKey(identifier) || holderById.containsKey(identifier);
        }

        @Override
        public Holder.@Nullable Reference<TRegistry> getHolder(String identifier) {
            return holderById.get(identifier);
        }

        @Override
        public Collection<Holder.Reference<TRegistry>> holders() {
            return Collections.unmodifiableCollection(holderById.values());
        }

        @Override
        public THolderType holder(ResourceKey<TRegistry> registryKey) {
            return holderFactory.apply(registryKey);
        }
    }
}
