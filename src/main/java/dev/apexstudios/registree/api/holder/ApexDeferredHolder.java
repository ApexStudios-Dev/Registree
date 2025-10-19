package dev.apexstudios.registree.api.holder;

import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ApexDeferredHolder<TRegistry, TElement extends TRegistry> extends DeferredHolder<TRegistry, TElement> {
    public ApexDeferredHolder(ResourceKey<TRegistry> registryKey) {
        super(registryKey);
    }

    public boolean is(TRegistry other) {
        return value() == other;
    }
}
