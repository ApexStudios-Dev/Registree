package dev.apexstudios.registree.holder;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class DeferredDataComponent<TType> extends DeferredHolder<DataComponentType<?>, DataComponentType<TType>> {
    DeferredDataComponent(ResourceKey<DataComponentType<?>> registryKey) {
        super(registryKey);
    }
}
