package dev.apexstudios.registree.holder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.registries.DeferredHolder;

public class DeferredFluid<TFluid extends Fluid> extends DeferredHolder<Fluid, TFluid> {
    protected DeferredFluid(ResourceKey<Fluid> registryKey) {
        super(registryKey);
    }

    public static <TFluid extends Fluid> DeferredFluid<TFluid> createFluid(ResourceKey<Fluid> registryKey) {
        return new DeferredFluid<>(registryKey);
    }

    public static <TFluid extends Fluid> DeferredFluid<TFluid> createFluid(Identifier registryName) {
        return createFluid(ResourceKey.create(Registries.FLUID, registryName));
    }
}
