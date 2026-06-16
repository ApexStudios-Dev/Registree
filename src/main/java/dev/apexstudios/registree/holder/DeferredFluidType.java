package dev.apexstudios.registree.holder;

import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class DeferredFluidType<TFluidType extends FluidType> extends DeferredHolder<FluidType, TFluidType> {
    DeferredFluidType(ResourceKey<FluidType> registryKey) {
        super(registryKey);
    }
}
