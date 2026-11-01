package dev.apexstudios.registree.api.util;

import dev.apexstudios.registree.api.Registree;
import dev.apexstudios.registree.api.holders.DeferredFluidType;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public interface FluidTypeRegistrations {
    static <TFluidType extends FluidType> DeferredFluidType<TFluidType> register(
            Registree registree,
            String identifier,
            Function<FluidType.Properties, TFluidType> factory,
            UnaryOperator<FluidType.Properties> propertiesModifier
    ) {
        return Registrations.registerForHolder(
                registree,
                NeoForgeRegistries.Keys.FLUID_TYPES,
                identifier,
                () -> propertiesModifier.andThen(factory).apply(FluidType.Properties.create()),
                DeferredFluidType::new
        );
    }

    static <TFluidType extends FluidType> DeferredFluidType<TFluidType> register(
            Registree registree,
            String identifier,
            Function<FluidType.Properties, TFluidType> factory
    ) {
        return register(registree, identifier, factory, UnaryOperator.identity());
    }

    static DeferredFluidType<FluidType> registerSimple(
            Registree registree,
            String identifier,
            UnaryOperator<FluidType.Properties> propertiesModifier
    ) {
        return register(registree, identifier, FluidType::new, propertiesModifier);
    }

    static DeferredFluidType<FluidType> registerSimple(
            Registree registree,
            String identifier
    ) {
        return registerSimple(registree, identifier, UnaryOperator.identity());
    }
}
