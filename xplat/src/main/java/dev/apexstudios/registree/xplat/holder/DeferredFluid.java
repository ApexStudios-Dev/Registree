package dev.apexstudios.registree.xplat.holder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.material.Fluid;

public final class DeferredFluid<TFluid extends Fluid> extends DeferredHolder<Fluid, TFluid> {
    public DeferredFluid(ResourceKey<Fluid> registryKey) {
        super(registryKey);
    }
}
