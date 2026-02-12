package dev.apexstudios.registree.holder;

import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.TypedInstance;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;

public record FluidPair<TSource extends BaseFlowingFluid.Source, TFlowing extends BaseFlowingFluid.Flowing>(
        DeferredFluid<TSource> source,
        DeferredFluid<TFlowing> flowing
) {
    public boolean is(TagKey<Fluid> tag) {
        return is(holder -> holder.is(tag));
    }

    public boolean is(HolderSet<Fluid> holders) {
        return is(holders::contains);
    }

    public boolean is(Fluid fluid) {
        return is(holder -> fluid.isSame(holder.value()));
    }

    public boolean is(Holder<Fluid> other) {
        return is(other::is);
    }

    public boolean is(ResourceKey<Fluid> registryKey) {
        return is(holder -> holder.is(registryKey));
    }

    public boolean is(FluidType fluidType) {
        return is(holder -> holder.value().getFluidType() == fluidType);
    }

    public boolean is(TypedInstance<Fluid> instance) {
        return is(instance::is);
    }

    public boolean is(Predicate<Holder<Fluid>> test) {
        return test.test(source) || test.test(flowing);
    }
}
