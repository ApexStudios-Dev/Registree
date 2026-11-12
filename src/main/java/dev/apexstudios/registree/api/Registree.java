package dev.apexstudios.registree.api;

import dev.apexstudios.registree.core.RegistreeImpl;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.bus.api.IEventBus;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface Registree {
    String namespace();

    default Identifier identifier(String identifier) {
        return Identifier.fromNamespaceAndPath(namespace(), identifier);
    }

    default String id(String identifier) {
        return namespace() + Identifier.NAMESPACE_SEPARATOR + identifier;
    }

    default <TRegistry> ResourceKey<TRegistry> registryKey(ResourceKey<? extends Registry<TRegistry>> registryType, String identifier) {
        return ResourceKey.create(registryType, identifier(identifier));
    }

    default <TRegistry> TagKey<TRegistry> tag(ResourceKey<? extends Registry<TRegistry>> registryType, String identifier) {
        return TagKey.create(registryType, identifier(identifier));
    }

    <TRegistry> Registrar<TRegistry> registrar(ResourceKey<? extends Registry<TRegistry>> registryType);

    ModEventBus events();

    void register(IEventBus modBus);

    static Registree create(String namespace) {
        return RegistreeImpl.create(namespace);
    }
}
