package dev.apexstudios.registree.data;

import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.ApiStatus;

public final class ExtendedRegistryBootstrap<TRegistry> implements HolderGetter<TRegistry> {
    private static final ICondition[] NO_CONDITIONS = new ICondition[0];

    private final BootstrapContext<TRegistry> vanilla;
    private final ResourceKey<? extends Registry<TRegistry>> registryType;
    private final String namespace;
    private final HolderGetter<TRegistry> delegate;
    private final BiConsumer<ResourceKey<TRegistry>, ICondition[]> conditionsConsumer;

    @ApiStatus.Internal
    public ExtendedRegistryBootstrap(BootstrapContext<TRegistry> vanilla, ResourceKey<? extends Registry<TRegistry>> registryType, String namespace, BiConsumer<ResourceKey<TRegistry>, ICondition[]> conditionsConsumer) {
        this.vanilla = vanilla;
        this.registryType = registryType;
        this.namespace = namespace;
        this.conditionsConsumer = conditionsConsumer;

        delegate = vanilla.lookup(registryType);
    }

    public Holder.Reference<TRegistry> register(ResourceKey<TRegistry> registryKey, Lifecycle lifecycle, TRegistry value, ICondition... conditions) {
        var holder = vanilla.register(registryKey, value, lifecycle);

        if(conditions.length != 0) {
            conditionsConsumer.accept(registryKey, conditions);
        }

        return holder;
    }

    public Holder.Reference<TRegistry> register(ResourceKey<TRegistry> registryKey, Lifecycle lifecycle, TRegistry value) {
        return register(registryKey, lifecycle, value, NO_CONDITIONS);
    }

    public Holder.Reference<TRegistry> register(ResourceKey<TRegistry> registryKey, TRegistry value, ICondition... conditions) {
        return register(registryKey, Lifecycle.stable(), value, conditions);
    }

    public Holder.Reference<TRegistry> register(ResourceKey<TRegistry> registryKey, TRegistry value) {
        return register(registryKey, Lifecycle.stable(), value, NO_CONDITIONS);
    }

    public Holder.Reference<TRegistry> register(Identifier registryName, Lifecycle lifecycle, TRegistry value, ICondition... conditions) {
        return register(ResourceKey.create(registryType, registryName), lifecycle, value, conditions);
    }

    public Holder.Reference<TRegistry> register(Identifier registryName, Lifecycle lifecycle, TRegistry value) {
        return register(registryName, lifecycle, value, NO_CONDITIONS);
    }

    public Holder.Reference<TRegistry> register(Identifier registryName, TRegistry value, ICondition... conditions) {
        return register(registryName, Lifecycle.stable(), value, conditions);
    }

    public Holder.Reference<TRegistry> register(Identifier registryName, TRegistry value) {
        return register(registryName, Lifecycle.stable(), value, NO_CONDITIONS);
    }

    public Holder.Reference<TRegistry> register(String identifier, Lifecycle lifecycle, TRegistry value, ICondition... conditions) {
        return register(Identifier.fromNamespaceAndPath(namespace, identifier), lifecycle, value, conditions);
    }

    public Holder.Reference<TRegistry> register(String identifier, Lifecycle lifecycle, TRegistry value) {
        return register(identifier, lifecycle, value, NO_CONDITIONS);
    }

    public Holder.Reference<TRegistry> register(String identifier, TRegistry value, ICondition... conditions) {
        return register(identifier, Lifecycle.stable(), value, conditions);
    }

    public Holder.Reference<TRegistry> register(String identifier, TRegistry value) {
        return register(identifier, Lifecycle.stable(), value, NO_CONDITIONS);
    }

    public <TOther> HolderGetter<TOther> lookup(ResourceKey<? extends Registry<? extends TOther>> registryType) {
        return vanilla.lookup(registryType);
    }

    public <TOther> Optional<HolderLookup.RegistryLookup<TOther>> registryLookup(ResourceKey<? extends Registry<? extends TOther>> registryType) {
        return vanilla.registryLookup(registryType);
    }

    @Override
    public Optional<Holder.Reference<TRegistry>> get(ResourceKey<TRegistry> registryKey) {
        return delegate.get(registryKey);
    }

    @Override
    public Optional<HolderSet.Named<TRegistry>> get(TagKey<TRegistry> tag) {
        return delegate.get(tag);
    }
}
