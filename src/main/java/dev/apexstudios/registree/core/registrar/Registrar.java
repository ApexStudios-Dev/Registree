package dev.apexstudios.registree.core.registrar;

import com.google.common.collect.Maps;
import dev.apexstudios.registree.api.IRegistree;
import dev.apexstudios.registree.api.registrar.IRegistrar;
import dev.apexstudios.registree.core.Deferred;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.registries.RegisterEvent;
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

        registree.event(EventPriority.HIGH, RegisterEvent.class, event -> event.register(registryType, helper -> {
                for(var entry : factories.entrySet()) {
                var identifier = entry.getKey();
                var registryName = registryName(identifier);
                var value = entry.getValue().apply(registryName);

                helper.register(registryName, value);

                if(valueById.putIfAbsent(identifier, value) != null) {
                    throw Util.pauseInIde(new IllegalStateException("Illegal " + registryType().identifier() + " registration: " + registryName + " (Duplicate entry)"));
                }
            }

            factories.clear();
        }));

        registree.event(EventPriority.LOW, RegisterEvent.class, event -> event.register(registryType, helper -> registry.invoke(Objects.requireNonNull(event.getRegistry(registryType)))));
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
        if(valueById.containsKey(identifier) || factories.putIfAbsent(identifier, factory) != null) {
            throw Util.pauseInIde(new IllegalStateException("Illegal " + registryType().identifier() + " registration: " + registryName(identifier) + " (Duplicate entry)"));
        }

        return registryKey(identifier);
    }
}
