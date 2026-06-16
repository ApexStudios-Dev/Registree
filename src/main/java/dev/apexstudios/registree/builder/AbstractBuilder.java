package dev.apexstudios.registree.builder;

import dev.apexstudios.registree.BaseRegistree;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class AbstractBuilder<TRegistry, TValue extends TRegistry, THolder extends DeferredHolder<TRegistry, TValue>, TSelf extends AbstractBuilder<TRegistry, TValue, THolder, TSelf>> {
    protected final BaseRegistree<?> registree;
    private final ResourceKey<? extends Registry<TRegistry>> registryType;
    protected final String identifier;
    private final Function<ResourceKey<TRegistry>, THolder> holderFactory;

    protected AbstractBuilder(BaseRegistree<?> registree, ResourceKey<? extends Registry<TRegistry>> registryType, String identifier, Function<ResourceKey<TRegistry>, THolder> holderFactory) {
        this.registree = registree;
        this.registryType = registryType;
        this.identifier = identifier;
        this.holderFactory = holderFactory;
    }

    @SuppressWarnings("unchecked")
    public TSelf whenAvailable(Consumer<TValue> action) {
        registree.whenAvailable(registryType, identifier, value -> action.accept((TValue) value));
        return self();
    }

    protected abstract TValue createValue(ResourceKey<TRegistry> registryKey);

    public THolder register() {
        var holder = registree.registerForHolder(registryType, identifier, registryName -> createValue(ResourceKey.create(registryType, registryName)), holderFactory);
        registerEvents();
        return holder;
    }

    @SuppressWarnings("unchecked")
    protected TValue value() {
        return (TValue) registree.getValueOrThrow(registryType, identifier);
    }

    @SuppressWarnings("unchecked")
    private TSelf self() {
        return (TSelf) this;
    }

    @MustBeInvokedByOverriders
    protected void registerEvents() {

    }
}
