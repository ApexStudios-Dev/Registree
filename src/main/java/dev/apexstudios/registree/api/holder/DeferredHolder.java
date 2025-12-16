package dev.apexstudios.registree.api.holder;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class DeferredHolder<TRegistry, TElement extends TRegistry> extends net.neoforged.neoforge.registries.DeferredHolder<TRegistry, TElement> {
    protected DeferredHolder(ResourceKey<TRegistry> registryKey) {
        super(registryKey);
    }

    public boolean is(TRegistry value) {
        return value() == value;
    }

    public <TOther, TOtherElement extends TOther> DeferredHolder<TOther, TOtherElement> asSibling(ResourceKey<? extends Registry<TOther>> registryType) {
        return Holders.create(registryType, getId());
    }
}
