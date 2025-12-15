package dev.apexstudios.registree.api.builder;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public interface IBuilderContext<TRegistry> {
    ResourceKey<TRegistry> registryKey();

    default ResourceKey<? extends Registry<TRegistry>> registryType() {
        return registryKey().registryKey();
    }

    default Identifier registryName() {
        return registryKey().identifier();
    }

    default String namespace() {
        return registryName().getNamespace();
    }

    default String identifier() {
        return registryName().getPath();
    }

    default String id() {
        return registryName().toString();
    }

    static <TRegistry> IBuilderContext<TRegistry> create(ResourceKey<TRegistry> registryKey) {
        return () -> registryKey;
    }

    interface WithValue<TRegistry, TElement extends TRegistry> extends IBuilderContext<TRegistry> {
        TElement value();
    }
}
