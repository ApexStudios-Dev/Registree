package dev.apexstudios.registree.builder;

import dev.apexstudios.registree.holder.DeferredFluidType;
import dev.apexstudios.registree.registrar.FluidTypeRegistrar;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.FluidType;
import org.jspecify.annotations.Nullable;

public class FluidTypeBuilder<TFluidType extends FluidType> extends Builder<FluidTypeRegistrar, FluidType, TFluidType, DeferredFluidType<TFluidType>, FluidTypeBuilder.Context<TFluidType>, FluidTypeBuilder<TFluidType>> {
    private final Function<FluidType.Properties, TFluidType> factory;
    private BiConsumer<Context<TFluidType>, FluidType.Properties> propertiesModifier = (context, properties) -> { };
    private @Nullable Supplier<Supplier<IClientFluidTypeExtensions>> clientExtensions = null;

    public FluidTypeBuilder(FluidTypeRegistrar registrar, String identifier, Function<FluidType.Properties, TFluidType> factory) {
        super(registrar, identifier, DeferredFluidType::createFluidType, Context::new);

        this.factory = factory;
    }

    public FluidTypeBuilder<TFluidType> properties(BiConsumer<Context<TFluidType>, FluidType.Properties> propertiesModifier) {
        this.propertiesModifier = this.propertiesModifier.andThen(propertiesModifier);
        return this;
    }

    public FluidTypeBuilder<TFluidType> properties(Consumer<FluidType.Properties> propertiesModifier) {
        return properties((context, properties) -> propertiesModifier.accept(properties));
    }

    public FluidTypeBuilder<TFluidType> clientExtensions(Supplier<Supplier<IClientFluidTypeExtensions>> clientExtensions) {
        this.clientExtensions = clientExtensions;
        return this;
    }

    @Override
    protected TFluidType compile(Context<TFluidType> context) {
        var properties = FluidType.Properties.create();
        propertiesModifier.accept(context, properties);
        return factory.apply(properties);
    }

    @Override
    protected void finalize(Context<TFluidType> context) {
        if(clientExtensions != null) {
            context.registree().event(RegisterClientExtensionsEvent.class, event -> {
                event.registerFluidType(clientExtensions.get().get(), context.get());
                clientExtensions = null;
            });
        }
    }

    public static final class Context<TFluidType extends FluidType> extends Builder.Context<FluidTypeRegistrar, FluidType, TFluidType, DeferredFluidType<TFluidType>> {
        private Context(FluidTypeRegistrar registrar, DeferredFluidType<TFluidType> holder) {
            super(registrar, holder);
        }
    }
}
