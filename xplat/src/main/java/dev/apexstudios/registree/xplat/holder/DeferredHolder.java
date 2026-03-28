package dev.apexstudios.registree.xplat.holder;

import com.mojang.datafixers.util.Either;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public class DeferredHolder<TRegistry, TValue extends TRegistry> implements Holder<TRegistry>, Supplier<TValue> {
    private final ResourceKey<TRegistry> registryKey;
    private Holder.@Nullable Reference<TRegistry> delegate;

    public DeferredHolder(ResourceKey<TRegistry> registryKey) {
        this.registryKey = registryKey;
        bind(false);
    }

    public ResourceKey<TRegistry> registryKey() {
        return registryKey;
    }

    public Identifier registryName() {
        return registryKey.identifier();
    }

    @SuppressWarnings("unchecked")
    protected final void bind(boolean throwOnMissingRegistry) {
        if(delegate != null) {
            return;
        }

        var registry = (Registry<TRegistry>) BuiltInRegistries.REGISTRY.getValue(registryKey.registry());

        if(registry != null) {
            delegate = registry.get(registryKey).orElse(null);
        } else if(throwOnMissingRegistry) {
            throw new IllegalStateException("Registry not present for " + registryKey.identifier() + ": " + registryKey.registry());
        }
    }

    @Override
    @ApiStatus.Obsolete
    public final TValue get() {
        return value();
    }

    @SuppressWarnings("unchecked")
    @Override
    public final TValue value() {
        bind(true);
        return (TValue) Objects.requireNonNull(delegate, () -> "Trying to access unbound value: " + registryKey).value();
    }

    @Override
    public final boolean isBound() {
        bind(false);
        return delegate != null && delegate.isBound();
    }

    @Override
    public final boolean areComponentsBound() {
        bind(false);
        return delegate != null && delegate.areComponentsBound();
    }

    @Override
    public final boolean is(Identifier registryName) {
        return registryName().equals(registryName);
    }

    @Override
    public final boolean is(ResourceKey<TRegistry> registryKey) {
        return this.registryKey == registryKey;
    }

    @Override
    public final boolean is(Predicate<ResourceKey<TRegistry>> predicate) {
        return predicate.test(registryKey);
    }

    @Override
    public boolean is(TagKey<TRegistry> tag) {
        bind(false);
        return delegate != null && delegate.is(tag);
    }

    @Override
    public final boolean is(Holder<TRegistry> holder) {
        bind(false);
        return delegate != null && delegate.is(holder);
    }

    @Override
    public final Stream<TagKey<TRegistry>> tags() {
        bind(false);
        return delegate == null ? Stream.empty() : delegate.tags();
    }

    @Override
    public final DataComponentMap components() {
        bind(false);
        return delegate == null ? DataComponentMap.EMPTY : delegate.components();
    }

    @Override
    public final Either<ResourceKey<TRegistry>, TRegistry> unwrap() {
        return Either.left(registryKey);
    }

    @Override
    public final Optional<ResourceKey<TRegistry>> unwrapKey() {
        return Optional.of(registryKey);
    }

    @Override
    public final Kind kind() {
        return Kind.REFERENCE;
    }

    @Override
    public final boolean canSerializeIn(HolderOwner<TRegistry> owner) {
        bind(false);
        return delegate != null && delegate.canSerializeIn(owner);
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        } else if(!(obj instanceof Holder<?> other)) {
            return false;
        } else {
            return other.kind() == Kind.REFERENCE && registryKey == other.unwrapKey().orElseThrow();
        }
    }

    @Override
    public int hashCode() {
        return registryKey.hashCode();
    }

    @Override
    public String toString() {
        return "DeferredHolder{" + registryKey + '}';
    }
}
