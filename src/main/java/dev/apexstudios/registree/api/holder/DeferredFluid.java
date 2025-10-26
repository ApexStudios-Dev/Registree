package dev.apexstudios.registree.api.holder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public final class DeferredFluid<TFluid extends Fluid> extends ApexDeferredHolder<Fluid, TFluid> {
    public DeferredFluid(ResourceKey<Fluid> registryKey) {
        super(registryKey);
    }

    public boolean is(FluidType fluidType) {
        return value().getFluidType() == fluidType;
    }

    public boolean is(FluidState fluidState) {
        return fluidState.is(value());
    }

    public boolean is(FluidStack stack) {
        return stack.is(this);
    }

    public FluidStack toStack(int amount) {
        return new FluidStack(this, amount);
    }

    public FluidStack toStack() {
        return toStack(FluidType.BUCKET_VOLUME);
    }
}
