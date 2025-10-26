package dev.apexstudios.registree.api.holder;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;

public final class DeferredDataComponent<TData> extends ApexDeferredHolder<DataComponentType<?>, DataComponentType<TData>> {
    public DeferredDataComponent(ResourceKey<DataComponentType<?>> registryKey) {
        super(registryKey);
    }
}
