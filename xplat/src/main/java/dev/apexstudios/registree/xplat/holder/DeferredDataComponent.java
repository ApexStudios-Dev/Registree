package dev.apexstudios.registree.xplat.holder;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;

public final class DeferredDataComponent<TData> extends DeferredHolder<DataComponentType<?>, DataComponentType<TData>> {
    public DeferredDataComponent(ResourceKey<DataComponentType<?>> registryKey) {
        super(registryKey);
    }
}
