package dev.apexstudios.registree.api.util;

import com.google.common.base.Suppliers;
import dev.apexstudios.registree.api.Registree;
import dev.apexstudios.registree.api.holders.DeferredFluid;
import dev.apexstudios.registree.api.holders.DeferredFluidPair;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;

public interface FluidRegistrations {
    static <TFluid extends Fluid> DeferredFluid<TFluid> register(
            Registree registree,
            String identifier,
            Supplier<TFluid> factory
    ) {
        return Registrations.registerForHolder(
                registree,
                Registries.FLUID,
                identifier,
                factory,
                DeferredFluid::new
        );
    }

    static <TFluid extends BaseFlowingFluid> DeferredFluidPair<TFluid> registerFlowing(
            Registree registree,
            String sourceIdentifier,
            String flowingIdentifier,
            Function<BaseFlowingFluid.Properties, ? extends TFluid> sourceFluid,
            Function<BaseFlowingFluid.Properties, ? extends TFluid> flowingFluid,
            Supplier<? extends FluidType> fluidType,
            UnaryOperator<BaseFlowingFluid.Properties> propertiesModifier
    ) {
        var properties = Suppliers.memoize(() -> propertiesModifier.apply(new BaseFlowingFluid.Properties(
                fluidType,
                () -> registree.registrar(Registries.FLUID).getValueOrThrow(sourceIdentifier),
                () -> registree.registrar(Registries.FLUID).getValueOrThrow(flowingIdentifier)

        )));

        return new DeferredFluidPair<>(
                register(registree, sourceIdentifier, () -> sourceFluid.apply(properties.get())),
                register(registree, flowingIdentifier, () -> flowingFluid.apply(properties.get()))
        );
    }

    static <TFluid extends BaseFlowingFluid> DeferredFluidPair<TFluid> registerFlowing(
            Registree registree,
            String sourceIdentifier,
            Function<BaseFlowingFluid.Properties, ? extends TFluid> sourceFluid,
            Function<BaseFlowingFluid.Properties, ? extends TFluid> flowingFluid,
            Supplier<? extends FluidType> fluidType,
            UnaryOperator<BaseFlowingFluid.Properties> propertiesModifier
    ) {
        return registerFlowing(
                registree,
                sourceIdentifier,
                defaultFlowingIdentifier(sourceIdentifier),
                sourceFluid,
                flowingFluid,
                fluidType,
                propertiesModifier
        );
    }

    static <TFluid extends BaseFlowingFluid> DeferredFluidPair<TFluid> registerFlowing(
            Registree registree,
            String sourceIdentifier,
            String flowingIdentifier,
            Function<BaseFlowingFluid.Properties, ? extends TFluid> sourceFluid,
            Function<BaseFlowingFluid.Properties, ? extends TFluid> flowingFluid,
            Supplier<? extends FluidType> fluidType
    ) {
        return registerFlowing(
                registree,
                sourceIdentifier,
                flowingIdentifier,
                sourceFluid,
                flowingFluid,
                fluidType,
                UnaryOperator.identity()
        );
    }

    static <TFluid extends BaseFlowingFluid> DeferredFluidPair<TFluid> registerFlowing(
            Registree registree,
            String sourceIdentifier,
            Function<BaseFlowingFluid.Properties, ? extends TFluid> sourceFluid,
            Function<BaseFlowingFluid.Properties, ? extends TFluid> flowingFluid,
            Supplier<? extends FluidType> fluidType
    ) {
        return registerFlowing(
                registree,
                sourceIdentifier,
                defaultFlowingIdentifier(sourceIdentifier),
                sourceFluid,
                flowingFluid,
                fluidType,
                UnaryOperator.identity()
        );
    }

    static String defaultFlowingIdentifier(String sourceIdentifier){
        return "flowing_" + sourceIdentifier;
    }
}
