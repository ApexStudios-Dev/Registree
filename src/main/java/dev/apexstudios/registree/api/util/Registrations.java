package dev.apexstudios.registree.api.util;

import dev.apexstudios.registree.api.RegistrationFactory;
import dev.apexstudios.registree.api.Registree;
import dev.apexstudios.registree.core.RegistreeImpl;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;

public interface Registrations {
    static <TRegistry> ResourceKey<TRegistry> register(Registree registree, ResourceKey<? extends Registry<TRegistry>> registryType, String identifier, RegistrationFactory<TRegistry, ? extends TRegistry> factory) {
        RegistreeImpl.register(registree, registryType, identifier, factory);
        return registree.registryKey(registryType, identifier);
    }

    static <TRegistry> ResourceKey<TRegistry> register(Registree registree, ResourceKey<? extends Registry<TRegistry>> registryType, String identifier, Supplier<? extends TRegistry> factory) {
        return register(registree, registryType, identifier, _ -> factory.get());
    }

    static <TRegistry, TValue extends TRegistry, THolder extends DeferredHolder<TRegistry, TValue>> THolder registerForHolder(Registree registree, ResourceKey<? extends Registry<TRegistry>> registryType, String identifier, RegistrationFactory<TRegistry, TValue> factory, Function<ResourceKey<TRegistry>, THolder> holderFactory) {
        var registryKey = register(registree, registryType, identifier, factory);
        return holderFactory.apply(registryKey);
    }

    static <TRegistry, TValue extends TRegistry, THolder extends DeferredHolder<TRegistry, TValue>> THolder registerForHolder(Registree registree, ResourceKey<? extends Registry<TRegistry>> registryType, String identifier, Supplier<TValue> factory, Function<ResourceKey<TRegistry>, THolder> holderFactory) {
        return registerForHolder(registree, registryType, identifier, _ -> factory.get(), holderFactory);
    }

    static <TRegistry, TValue extends TRegistry> DeferredHolder<TRegistry, TValue> registerForHolder(Registree registree, ResourceKey<? extends Registry<TRegistry>> registryType, String identifier, RegistrationFactory<TRegistry, TValue> factory) {
        return registerForHolder(registree, registryType, identifier, factory, DeferredHolder::create);
    }

    static <TRegistry, TValue extends TRegistry> DeferredHolder<TRegistry, TValue> registerForHolder(Registree registree, ResourceKey<? extends Registry<TRegistry>> registryType, String identifier, Supplier<TValue> factory) {
        return registerForHolder(registree, registryType, identifier, factory, DeferredHolder::create);
    }
}
