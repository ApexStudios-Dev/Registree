package dev.apexstudios.registree.holder;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class DeferredFluidType<TFluidType extends FluidType> extends DeferredHolder<FluidType, TFluidType> {
    protected DeferredFluidType(ResourceKey<FluidType> registryKey) {
        super(registryKey);
    }

    public static <TFluidType extends FluidType> DeferredFluidType<TFluidType> createFluidType(ResourceKey<FluidType> registryKey) {
        return new DeferredFluidType<>(registryKey);
    }

    public static <TFluidType extends FluidType> DeferredFluidType<TFluidType> createFluidType(Identifier registryName) {
        return createFluidType(ResourceKey.create(NeoForgeRegistries.Keys.FLUID_TYPES, registryName));
    }
}
