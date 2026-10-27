package dev.apexstudios.registree.api.holders;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;

public class DeferredDataComponentType<TValue> extends DeferredHolder<DataComponentType<?>, DataComponentType<TValue>> {
    public DeferredDataComponentType(ResourceKey<DataComponentType<?>> registryKey) {
        super(registryKey);
    }
}
