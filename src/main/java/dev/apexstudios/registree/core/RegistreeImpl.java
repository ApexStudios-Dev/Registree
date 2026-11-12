package dev.apexstudios.registree.core;

import dev.apexstudios.registree.api.ModEventBus;
import dev.apexstudios.registree.api.Registrar;
import dev.apexstudios.registree.api.RegistrationFactory;
import dev.apexstudios.registree.api.Registree;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class RegistreeImpl implements Registree {
    private static final Map<String, Registree> REGISTRY = new ConcurrentHashMap<>();

    private final String namespace;
    private final ModEventBusImpl modBus = new ModEventBusImpl();
    private final Map<ResourceKey<? extends Registry<?>>, Registrar<?>> registrars = new HashMap<>();
    private boolean frozen = false;

    private RegistreeImpl(String namespace) {
        this.namespace = namespace;

        modBus.addListener(EventPriority.HIGH, RegisterEvent.class, event -> register(event.getRegistry()));

        modBus.addListener(EventPriority.LOW, RegisterEvent.class, event -> {
            var registrar = registrars.get(event.getRegistryKey());

            if(registrar != null) {
                ((RegistrarImpl<?>) registrar).notifyListeners();
            }
        });
    }

    @Override
    public String namespace() {
        return namespace;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <TRegistry> Registrar<TRegistry> registrar(ResourceKey<? extends Registry<TRegistry>> registryType) {
        return (Registrar<TRegistry>) registrars.computeIfAbsent(registryType, _ -> new RegistrarImpl<>(this, registryType));
    }

    @Override
    public ModEventBus events() {
        return modBus;
    }

    @Override
    public void register(IEventBus modBus) {
        if(frozen) {
            throw new IllegalStateException("Registree[" + namespace + "] is already frozen");
        }

        this.modBus.register(modBus);
        frozen = true;
    }

    @Override
    public int hashCode() {
        return namespace.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }

        if(obj instanceof Registree other) {
            return namespace.equals(other.namespace());
        }

        return false;
    }

    @Override
    public String toString() {
        return "Registree{" + namespace + '}';
    }

    @SuppressWarnings("unchecked")
    private <TRegistry> void register(Registry<TRegistry> registry) {
        var registrar = registrars.get(registry.key());

        if(registrar != null) {
            ((RegistrarImpl<TRegistry>) registrar).register(registry);
        }
    }

    public static Registree create(String namespace) {
        return REGISTRY.computeIfAbsent(namespace, RegistreeImpl::new);
    }

    public static <TRegistry> void register(Registree registree, ResourceKey<? extends Registry<TRegistry>> registryType, String identifier, RegistrationFactory<TRegistry, ? extends TRegistry> factory) {
        ((RegistrarImpl<TRegistry>) registree.registrar(registryType)).register(identifier, factory);
    }
}
