package dev.apexstudios.registree.api;

import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface Registration<TRegistry, TValue> {
    Registrar<TRegistry> registrar();

    ResourceKey<TRegistry> registryKey();

    TValue value();
}
