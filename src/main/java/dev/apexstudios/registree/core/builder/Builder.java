package dev.apexstudios.registree.core.builder;

import dev.apexstudios.registree.api.builder.IBuilder;
import dev.apexstudios.registree.api.builder.IBuilderContext;
import dev.apexstudios.registree.api.registrar.IRegistrar;
import dev.apexstudios.registree.core.Deferred;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;

public abstract class Builder<TRegistry, TElement extends TRegistry, THolder extends DeferredHolder<TRegistry, TElement>, TSelf extends IBuilder<TRegistry, TElement, THolder, TSelf>> implements IBuilder<TRegistry, TElement, THolder, TSelf> {
    protected final IRegistrar<TRegistry> registrar;
    private final String identifier;
    private final Deferred<IBuilderContext.WithValue<TRegistry, TElement>> deferred = new Deferred<>();
    private final Function<ResourceKey<TRegistry>, THolder> holderFactory;

    public Builder(IRegistrar<TRegistry> registrar, String identifier, Function<ResourceKey<TRegistry>, THolder> holderFactory) {
        this.registrar = registrar;
        this.identifier = identifier;
        this.holderFactory = holderFactory;
    }

    @SuppressWarnings("unchecked")
    protected TSelf defer(Consumer<IBuilderContext.WithValue<TRegistry, TElement>> listener) {
        deferred.defer(listener);
        return (TSelf) this;
    }

    protected abstract TElement createElement(IBuilderContext<TRegistry> context);

    @SuppressWarnings("unchecked")
    @Override
    public TSelf onRegister(Consumer<TElement> listener) {
        registrar.onRegister(identifier, value -> listener.accept((TElement) value));
        return (TSelf) this;
    }

    @Override
    public THolder register() {
        var context = IBuilderContext.create(registrar.registryKey(identifier));
        var holder = registrar.registerForHolder(identifier, () -> createElement(context), holderFactory);

        deferred.invoke(new IBuilderContext.WithValue<>() {
            @Override
            public TElement value() {
                return holder.value();
            }

            @Override
            public ResourceKey<TRegistry> registryKey() {
                return context.registryKey();
            }
        });

        return holder;
    }
}
