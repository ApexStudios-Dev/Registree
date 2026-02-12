package dev.apexstudios.registree.builder;

import com.google.errorprone.annotations.ForOverride;
import dev.apexstudios.registree.Registree;
import dev.apexstudios.registree.registrar.Registrar;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.apache.commons.compress.utils.Lists;
import org.jspecify.annotations.Nullable;

public abstract class Builder<
        TRegistrar extends Registrar<TRegistry>,
        TRegistry,
        TValue extends TRegistry,
        THolder extends DeferredHolder<TRegistry, TValue>,
        TContext extends Builder.Context<TRegistrar, TRegistry, TValue, THolder>,
        TSelf extends Builder<TRegistrar, TRegistry, TValue, THolder, TContext, TSelf>
> {
    private final TRegistrar registrar;
    private final String identifier;
    private final Function<ResourceKey<TRegistry>, THolder> holderFactory;
    private final BiFunction<TRegistrar, THolder, TContext> contextFactory;
    private final List<Function<TContext, ? extends Builder<?, ?, ?, ?, ?, ?>>> children = Lists.newArrayList();
    private @Nullable Consumer<TValue> listener = null;

    protected Builder(TRegistrar registrar, String identifier, Function<ResourceKey<TRegistry>, THolder> holderFactory, BiFunction<TRegistrar, THolder, TContext> contextFactory) {
        this.registrar = registrar;
        this.identifier = identifier;
        this.holderFactory = holderFactory;
        this.contextFactory = contextFactory;
    }

    @SuppressWarnings("unchecked")
    public TSelf listenFor(Consumer<TValue> listener) {
        this.listener = this.listener == null ? listener : this.listener.andThen(listener);
        return (TSelf) this;
    }

    @ForOverride
    protected void finalize(TContext context) {

    }

    protected abstract TValue compile(TContext context);

    protected final <TBuilder extends Builder<?, ?, ?, ?, ?, TBuilder>> void child(Function<TContext, TBuilder> childFactory) {
        children.add(childFactory);
    }

    public final THolder register() {
        var holder = holderFactory.apply(registrar.registryKey(identifier));
        var context = contextFactory.apply(registrar, holder);

        if(listener != null) {
            registrar.listenFor(holder, listener);
            listener = null;
        }

        children.forEach(factory -> factory.apply(context).register());
        children.clear();

        finalize(context);
        context.registrar().registerBasic(identifier, () -> compile(context));
        return holder;
    }

    public static class Context<
            TRegistrar extends Registrar<TRegistry>,
            TRegistry,
            TValue extends TRegistry,
            THolder extends DeferredHolder<TRegistry, TValue>
    > implements Supplier<TValue> {
        private final TRegistrar registrar;
        private final THolder holder;

        public Context(TRegistrar registrar, THolder holder) {
            this.registrar = registrar;
            this.holder = holder;
        }

        public Registree registree() {
            return registrar().registree();
        }

        public TRegistrar registrar() {
            return registrar;
        }

        public String namespace() {
            return registryName().getNamespace();
        }

        public ResourceKey<? extends Registry<TRegistry>> registryType() {
            return registryKey().registryKey();
        }

        public ResourceKey<TRegistry> registryKey() {
            return holder().getKey();
        }

        public Identifier registryName() {
            return registryKey().identifier();
        }

        public String identifier() {
            return registryName().getPath();
        }

        public THolder holder() {
            return holder;
        }

        @Override
        public TValue get() {
            return holder().value();
        }

        @Override
        public boolean equals(Object obj) {
            if(this == obj) {
                return true;
            }

            if(!(obj instanceof Builder.Context<?, ?, ?, ?> other)) {
                return false;
            }

            return registryKey().equals(other.registryKey());
        }

        @Override
        public int hashCode() {
            return registryKey().hashCode();
        }

        @Override
        public String toString() {
            return "Registration-Context{" + registryKey() + '}';
        }
    }
}
