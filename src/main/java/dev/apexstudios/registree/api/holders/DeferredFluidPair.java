package dev.apexstudios.registree.api.holders;

import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public record DeferredFluidPair<TBase extends BaseFlowingFluid>(
        DeferredFluid<? extends TBase> source,
        DeferredFluid<? extends TBase> flowing
) {
    public DeferredFluid<? extends TBase> holder(boolean flowing) {
        return flowing ? flowing() : source();
    }

    public TBase value(boolean flowing) {
        return holder(flowing).value();
    }

    public boolean matches(Predicate<Holder<Fluid>> filter) {
        return filter.test(source) || filter.test(flowing);
    }

    public boolean is(Identifier registryName) {
        return matches(holder -> holder.is(registryName));
    }

    public boolean is(ResourceKey<Fluid> registryKey) {
        return matches(holder -> holder.is(registryKey));
    }

    public boolean is(Predicate<ResourceKey<Fluid>> filter) {
        return matches(holder -> holder.is(filter));
    }

    public boolean is(TagKey<Fluid> tag) {
        return matches(holder -> holder.is(tag));
    }

    public boolean is(Holder<Fluid> other) {
        return matches(holder -> holder.is(other));
    }

    public boolean is(TypedInstance<Fluid> instance) {
        return matches(instance::is);
    }

    public boolean is(Fluid fluid) {
        return matches(holder -> fluid == holder.value());
    }

    public ResourceKey<Fluid> key(boolean flowing) {
        return holder(flowing).key();
    }

    public Identifier getId(boolean flowing) {
        return holder(flowing).getId();
    }
}
