package dev.apexstudios.registree.core.builder;

import dev.apexstudios.registree.api.builder.IBuilder;
import dev.apexstudios.registree.api.builder.IBuilderContext;
import dev.apexstudios.registree.api.holder.DeferredHolder;
import dev.apexstudios.registree.api.registrar.IRegistrar;
import dev.apexstudios.registree.core.Deferred;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceKey;

public abstract class Builder<TRegistrar extends IRegistrar<TRegistry>, TRegistry, TRegistered extends TRegistry, TResult, TSelf extends IBuilder<TRegistry, TRegistered, TResult, TSelf>> implements IBuilder<TRegistry, TRegistered, TResult, TSelf> {
    protected final TRegistrar registrar;
    protected final String identifier;

    public Builder(TRegistrar registrar, String identifier) {
        this.registrar = registrar;
        this.identifier = identifier;
    }

    protected abstract TRegistry createElement(IBuilderContext<TRegistry> context);

    public static abstract class Basic<TRegistrar extends IRegistrar<TRegistry>, TRegistry, TSelf extends IBuilder.Basic<TRegistry, TSelf>> extends Builder<TRegistrar, TRegistry, TRegistry, ResourceKey<TRegistry>, TSelf> implements IBuilder.Basic<TRegistry, TSelf> {
        public Basic(TRegistrar registrar, String identifier) {
            super(registrar, identifier);
        }

        @SuppressWarnings("unchecked")
        @Override
        public TSelf onRegister(Consumer<TRegistry> listener) {
            registrar.onRegister(identifier, listener);
            return (TSelf) this;
        }

        @Override
        public ResourceKey<TRegistry> register() {
            return registrar.register(identifier, registryName -> createElement(IBuilderContext.create(registrar.registryKey(registryName))));
        }
    }

    public static abstract class WithHolder<TRegistrar extends IRegistrar.WithHolder<TRegistry, ? super THolder>, TRegistry, TElement extends TRegistry, THolder extends DeferredHolder<TRegistry, TElement>, TSelf extends IBuilder.WithHolder<TRegistry, TElement, THolder, TSelf>> extends Builder<TRegistrar, TRegistry, TElement, THolder, TSelf> implements IBuilder.WithHolder<TRegistry, TElement, THolder, TSelf> {
        private final Deferred<IBuilderContext.WithValue<TRegistry, TElement>> deferred = new Deferred<>();

        public WithHolder(TRegistrar registrar, String identifier) {
            super(registrar, identifier);
        }

        @SuppressWarnings("unchecked")
        protected TSelf defer(Consumer<IBuilderContext.WithValue<TRegistry, TElement>> listener) {
            deferred.defer(listener);
            return (TSelf) this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public TSelf onRegister(Consumer<TElement> listener) {
            registrar.onRegister(identifier, value -> listener.accept((TElement) value));
            return (TSelf) this;
        }

        @Override
        public THolder register() {
            var context = IBuilderContext.create(registrar.registryKey(identifier));
            var holder = registrar.<TElement, THolder>registerForHolder(identifier, () -> createElement(context));

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
}
