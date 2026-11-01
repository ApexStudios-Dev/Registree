package dev.apexstudios.registree.api.holders;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.registries.DeferredHolder;

public class DeferredFluid<TFluid extends Fluid> extends DeferredHolder<Fluid, TFluid> {
    public DeferredFluid(ResourceKey<Fluid> registryKey) {
        super(registryKey);
    }
}
