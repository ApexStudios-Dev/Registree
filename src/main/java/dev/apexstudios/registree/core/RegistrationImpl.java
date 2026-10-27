package dev.apexstudios.registree.core;

import dev.apexstudios.registree.api.Registrar;
import dev.apexstudios.registree.api.Registration;
import dev.apexstudios.registree.api.RegistrationFactory;
import java.util.Objects;
import net.minecraft.resources.ResourceKey;

final class RegistrationImpl<TRegistry, TValue extends TRegistry> implements Registration<TRegistry, TValue> {
    private final Registrar<TRegistry> registrar;
    private final ResourceKey<TRegistry> registryKey;
    private final TValue value;

    RegistrationImpl(Registrar<TRegistry> registrar, String identifier, RegistrationFactory<TRegistry, TValue> factory) {
        this.registrar = registrar;
        registryKey = registrar.registryKey(identifier);
        value = factory.register(this);
    }

    @Override
    public Registrar<TRegistry> registrar() {
        return registrar;
    }

    @Override
    public ResourceKey<TRegistry> registryKey() {
        return registryKey;
    }

    @Override
    public TValue value() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(registrar, registryKey);
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }

        if(obj instanceof Registration<?, ?> other){
            return registrar.equals(other.registrar()) && registryKey.equals(other.registryKey());
        }

        return false;
    }

    @Override
    public String toString() {
        return "Registration{" + registryKey + '}';
    }
}
