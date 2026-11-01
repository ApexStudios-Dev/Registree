package dev.apexstudios.registree.api.holders;

import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class DeferredFluidType<TFluidType extends FluidType> extends DeferredHolder<FluidType, TFluidType> {
    public DeferredFluidType(ResourceKey<FluidType> registryKey) {
        super(registryKey);
    }
}
