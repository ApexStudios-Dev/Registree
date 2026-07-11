package dev.apexstudios.registree.builder;

import dev.apexstudios.registree.BaseRegistree;
import dev.apexstudios.registree.holder.DeferredFluidType;
import dev.apexstudios.registree.holder.Holders;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.resources.ResourceKey;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public final class FluidTypeBuilder<TFluidType extends FluidType> extends AbstractBuilder<FluidType, TFluidType, DeferredFluidType<TFluidType>, FluidTypeBuilder<TFluidType>> {
    private final Function<FluidType.Properties, TFluidType> factory;
    private Supplier<FluidType.Properties> initialProperties = FluidType.Properties::create;
    private Function<FluidType.Properties, FluidType.Properties> propertiesModifier = Function.identity();
    private @Nullable Supplier<Supplier<IClientFluidTypeExtensions>> clientExtension = null;

    @ApiStatus.Internal
    public FluidTypeBuilder(BaseRegistree<?> registree, String identifier, Function<FluidType.Properties, TFluidType> factory) {
        super(registree, NeoForgeRegistries.Keys.FLUID_TYPES, identifier, Holders::createFluidType);

        this.factory = factory;
    }

    public FluidTypeBuilder<TFluidType> initialProperties(Supplier<FluidType.Properties> initialProperties) {
        this.initialProperties = initialProperties;
        return this;
    }

    public FluidTypeBuilder<TFluidType> properties(UnaryOperator<FluidType.Properties> propertiesModifier) {
        this.propertiesModifier = this.propertiesModifier.andThen(propertiesModifier);
        return this;
    }

    public FluidTypeBuilder<TFluidType> clientExtension(Supplier<Supplier<IClientFluidTypeExtensions>> clientExtension) {
        this.clientExtension = clientExtension;
        return this;
    }

    @Override
    protected TFluidType createValue(ResourceKey<FluidType> registryKey) {
        return propertiesModifier.andThen(factory).apply(initialProperties.get());
    }

    @Override
    protected void registerEvents() {
        super.registerEvents();

        if(FMLEnvironment.getDist().isClient()) {
            RegistryClientEventHelper.registerFluidTypeClientExtension(registree, this::value, clientExtension);
        }
    }
}
