package dev.apexstudios.registree.registrar;

import com.google.common.base.Suppliers;
import dev.apexstudios.registree.Registree;
import dev.apexstudios.registree.holder.DeferredFluid;
import dev.apexstudios.registree.holder.FluidPair;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import org.apache.commons.lang3.function.Consumers;

public class FluidRegistrar extends Registrar<Fluid> {
    public static final String FLOWING_PREFIX = "flowing_";

    public FluidRegistrar(Registree registree) {
        super(registree, Registries.FLUID);
    }

    public <TFluid extends Fluid> DeferredFluid<TFluid> register(String identifier, Supplier<TFluid> factory) {
        return registerForHolder(identifier, factory, DeferredFluid::createFluid);
    }

    public <TSource extends BaseFlowingFluid.Source, TFlowing extends BaseFlowingFluid.Flowing> FluidPair<TSource, TFlowing> registerFlowing(String identifier, Holder<FluidType> fluidType, Function<BaseFlowingFluid.Properties, TSource> sourceFactory, Function<BaseFlowingFluid.Properties, TFlowing> flowingFactory, Consumer<BaseFlowingFluid.Properties> propertiesModifier) {
        var flowingIdentifier = FLOWING_PREFIX + identifier;

        var source = DeferredFluid.<TSource>createFluid(registryKey(identifier));
        var flowing = DeferredFluid.<TFlowing>createFluid(registryKey(flowingIdentifier));

        var properties = Suppliers.memoize(() -> {
            var props = new BaseFlowingFluid.Properties(fluidType::value, source::value, flowing::value);
            propertiesModifier.accept(props);
            return props;
        });

        registerBasic(identifier, () -> sourceFactory.apply(properties.get()));
        registerBasic(flowingIdentifier, () -> flowingFactory.apply(properties.get()));

        return new FluidPair<>(source, flowing);
    }

    public <TSource extends BaseFlowingFluid.Source, TFlowing extends BaseFlowingFluid.Flowing> FluidPair<TSource, TFlowing> registerFlowing(String identifier, Holder<FluidType> fluidType, Function<BaseFlowingFluid.Properties, TSource> sourceFactory, Function<BaseFlowingFluid.Properties, TFlowing> flowingFactory) {
        return registerFlowing(identifier, fluidType, sourceFactory, flowingFactory, Consumers.nop());
    }

    public FluidPair<BaseFlowingFluid.Source, BaseFlowingFluid.Flowing> registerFlowing(String identifier, Holder<FluidType> fluidType, Consumer<BaseFlowingFluid.Properties> propertiesModifier) {
        return registerFlowing(identifier, fluidType, BaseFlowingFluid.Source::new, BaseFlowingFluid.Flowing::new, propertiesModifier);
    }

    public FluidPair<BaseFlowingFluid.Source, BaseFlowingFluid.Flowing> registerFlowing(String identifier, Holder<FluidType> fluidType) {
        return registerFlowing(identifier, fluidType, Consumers.nop());
    }
}
