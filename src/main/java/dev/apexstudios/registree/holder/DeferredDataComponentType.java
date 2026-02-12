package dev.apexstudios.registree.holder;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;

public class DeferredDataComponentType<TValue> extends DeferredHolder<DataComponentType<?>, DataComponentType<TValue>> {
    protected DeferredDataComponentType(ResourceKey<DataComponentType<?>> registryKey) {
        super(registryKey);
    }

    public static <TValue> DeferredDataComponentType<TValue> createDataComponentType(ResourceKey<DataComponentType<?>> registryKey) {
        return new DeferredDataComponentType<>(registryKey);
    }

    public static <TValue> DeferredDataComponentType<TValue> createDataComponentType(Identifier registryName) {
        return createDataComponentType(ResourceKey.create(Registries.DATA_COMPONENT_TYPE, registryName));
    }
}
