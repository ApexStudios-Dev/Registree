package dev.apexstudios.registree.api.holder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public final class DeferredFluidType<TFluidType extends FluidType> extends ApexDeferredHolder<FluidType, TFluidType> {
    public DeferredFluidType(ResourceKey<FluidType> registryKey) {
        super(registryKey);
    }

    public boolean is(FluidState fluidState) {
        return fluidState.getFluidType() == value();
    }

    public boolean is(FluidStack stack) {
        return stack.is(value());
    }

    public boolean isFor(Fluid fluid) {
        return fluid.getFluidType() == value();
    }
}
