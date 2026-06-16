package dev.apexstudios.registree.holder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class DeferredFluid<TFluid extends Fluid> extends DeferredHolder<Fluid, TFluid> {
    DeferredFluid(ResourceKey<Fluid> registryKey) {
        super(registryKey);
    }
}
