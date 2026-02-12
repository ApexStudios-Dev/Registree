package dev.apexstudios.registree.registrar;

import dev.apexstudios.registree.Registree;
import dev.apexstudios.registree.builder.FluidTypeBuilder;
import dev.apexstudios.registree.holder.DeferredFluidType;
import java.util.function.Consumer;
import java.util.function.Function;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.commons.lang3.function.Consumers;

public class FluidTypeRegistrar extends Registrar<FluidType> {
    public FluidTypeRegistrar(Registree registree) {
        super(registree, NeoForgeRegistries.Keys.FLUID_TYPES);
    }

    public <TFluidType extends FluidType> FluidTypeBuilder<TFluidType> builder(String identifier, Function<FluidType.Properties, TFluidType> factory) {
        return new FluidTypeBuilder<>(this, identifier, factory);
    }

    public FluidTypeBuilder<FluidType> builder(String identifier) {
        return builder(identifier, FluidType::new);
    }

    public <TFluidType extends FluidType> DeferredFluidType<TFluidType> register(String identifier, Function<FluidType.Properties, TFluidType> factory, Consumer<FluidType.Properties> propertiesModifier) {
        return registerForHolder(identifier, () -> {
            var properties = FluidType.Properties.create();
            propertiesModifier.accept(properties);
            return factory.apply(properties);
        }, DeferredFluidType::createFluidType);
    }

    public <TFluidType extends FluidType> DeferredFluidType<TFluidType> register(String identifier, Function<FluidType.Properties, TFluidType> factory) {
        return register(identifier, factory, Consumers.nop());
    }

    public DeferredFluidType<FluidType> register(String identifier, Consumer<FluidType.Properties> propertiesModifier) {
        return register(identifier, FluidType::new, propertiesModifier);
    }

    public DeferredFluidType<FluidType> register(String identifier) {
        return register(identifier, Consumers.nop());
    }
}
