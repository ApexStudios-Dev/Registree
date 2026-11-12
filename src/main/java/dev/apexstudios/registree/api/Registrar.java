package dev.apexstudios.registree.api;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.NonExtendable
public interface Registrar<TRegistry> {
    Registree registree();

    ResourceKey<? extends Registry<TRegistry>> registryType();

    default ResourceKey<TRegistry> registryKey(String identifier) {
        return registree().registryKey(registryType(), identifier);
    }

    default TagKey<TRegistry> tag(String identifier) {
        return registree().tag(registryType(), identifier);
    }

    Holder.@Nullable Reference<TRegistry> getHolderOrNull(String identifier);

    default Holder.Reference<TRegistry> getHolderOrThrow(String identifier) {
        return Objects.requireNonNull(getHolderOrNull(identifier));
    }

    default Optional<Holder.Reference<TRegistry>> getHolder(String identifier) {
        return Optional.ofNullable(getHolderOrNull(identifier));
    }

    @Nullable TRegistry getValueOrNull(String identifier);

    default TRegistry getValueOrThrow(String identifier) {
        return Objects.requireNonNull(getValueOrNull(identifier));
    }

    default Optional<TRegistry> getValue(String identifier) {
        return Optional.ofNullable(getValueOrNull(identifier));
    }

    default Stream<Holder.Reference<TRegistry>> streamHolders() {
        return holders().stream();
    }

    default Stream<TRegistry> streamValues() {
        return values().stream();
    }

    Collection<Holder.Reference<TRegistry>> holders();

    Collection<TRegistry> values();

    Set<String> keys();

    @CanIgnoreReturnValue
    Registrar<TRegistry> whenAvailable(ResourceKey<TRegistry> registryKey, Consumer<? super TRegistry> action);

    @CanIgnoreReturnValue
    default Registrar<TRegistry> whenAvailable(Identifier registryName, Consumer<? super TRegistry> action) {
        return whenAvailable(ResourceKey.create(registryType(), registryName), action);
    }

    @CanIgnoreReturnValue
    default Registrar<TRegistry> whenAvailable(String identifier, Consumer<? super TRegistry> action) {
        return whenAvailable(registryKey(identifier), action);
    }
}
