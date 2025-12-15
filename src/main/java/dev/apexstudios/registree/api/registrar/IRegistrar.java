package dev.apexstudios.registree.api.registrar;

import dev.apexstudios.registree.api.IRegistree;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jspecify.annotations.Nullable;

public interface IRegistrar<TRegistry> {
    IRegistree registree();

    default String namespace() {
        return registree().namespace();
    }

    default String registryId(String identifier) {
        return registree().registryId(identifier);
    }

    default Identifier registryName(String identifier) {
        return registree().registryName(identifier);
    }

    ResourceKey<? extends Registry<TRegistry>> registryType();

    default ResourceKey<TRegistry> registryKey(Identifier registryName) {
        return ResourceKey.create(registryType(), registryName);
    }

    default ResourceKey<TRegistry> registryKey(String identifier) {
        return registryKey(registryName(identifier));
    }

    default TagKey<TRegistry> tag(Identifier identifier) {
        return TagKey.create(registryType(), identifier);
    }

    default TagKey<TRegistry> tag(String path) {
        return tag(registryName(path));
    }

    default TagKey<TRegistry> vanillaTag(String path) {
        return tag(Identifier.withDefaultNamespace(path));
    }

    default TagKey<TRegistry> neoforgeTag(String path) {
        return tag(Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, path));
    }

    default TagKey<TRegistry> commonTag(String path) {
        return tag(Identifier.fromNamespaceAndPath("c", path));
    }

    @Nullable TRegistry get(String identifier);

    @SuppressWarnings("NullableProblems")
    default Optional<TRegistry> lookup(String identifier) {
        return Optional.ofNullable(get(identifier));
    }

    default TRegistry getOrThrow(String identifier) {
        return Objects.requireNonNull(get(identifier));
    }

    default Stream<TRegistry> stream() {
        return values().stream();
    }

    Collection<TRegistry> values();

    Set<String> keySet();

    boolean containsKey(String identifier);

    boolean containsValue(TRegistry value);

    void onRegister(ResourceKey<TRegistry> registryKey, Consumer<TRegistry> listener);

    default void onRegister(Identifier registryName, Consumer<TRegistry> listener) {
        onRegister(registryKey(registryName), listener);
    }

    default void onRegister(String identifier, Consumer<TRegistry> listener) {
        onRegister(registryKey(identifier), listener);
    }

    ResourceKey<TRegistry> register(String identifier, Function<Identifier, TRegistry> factory);

    default ResourceKey<TRegistry> register(String identifier, Supplier<TRegistry> factory) {
        return register(identifier, registryName -> factory.get());
    }

    default <TElement extends TRegistry, THolder extends DeferredHolder<TRegistry, TElement>> THolder registerForHolder(String identifier, Function<Identifier, TRegistry> factory, Function<ResourceKey<TRegistry>, THolder> holderFactory) {
        return holderFactory.apply(register(identifier, factory));
    }

    default <TElement extends TRegistry, THolder extends DeferredHolder<TRegistry, TElement>> THolder registerForHolder(String identifier, Supplier<TRegistry> factory, Function<ResourceKey<TRegistry>, THolder> holderFactory) {
        return registerForHolder(identifier, registryName -> factory.get(), holderFactory);
    }

    default <TElement extends TRegistry> DeferredHolder<TRegistry, TElement> registerForHolder(String identifier, Function<Identifier, TRegistry> factory) {
        return registerForHolder(identifier, factory, DeferredHolder::create);
    }

    default <TElement extends TRegistry> DeferredHolder<TRegistry, TElement> registerForHolder(String identifier, Supplier<TRegistry> factory) {
        return registerForHolder(identifier, registryName -> factory.get());
    }
}
