package dev.apexstudios.registree.data.pack;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.errorprone.annotations.ForOverride;
import dev.apexstudios.registree.Registree;
import dev.apexstudios.registree.data.ExtendedRegistryBootstrap;
import dev.apexstudios.registree.data.context.ProviderContext;
import dev.apexstudios.registree.data.context.ProviderListenerContext;
import dev.apexstudios.registree.data.context.ProviderOutputContext;
import dev.apexstudios.registree.data.pack.types.PackGenerator;
import dev.apexstudios.registree.data.provider.BaseProvider;
import dev.apexstudios.registree.data.provider.ProviderType;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.DetectedVersion;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.data.registries.RegistriesDatapackGenerator;
import net.minecraft.data.registries.RegistryPatchGenerator;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.GameData;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
@ApiStatus.NonExtendable
public abstract class BasePackGenerator<TSelf extends PackGenerator<TSelf>> implements PackGenerator<TSelf> {
    @SuppressWarnings("unchecked")
    protected final TSelf self = (TSelf) this;
    protected final Registree registree;
    private @Nullable Component description = null;
    private final Multimap<ProviderType<?>, BiConsumer<ProviderListenerContext, ?>> providerListeners = HashMultimap.create();
    private final Multimap<ResourceKey<? extends Registry<?>>, Consumer<? extends ExtendedRegistryBootstrap<?>>> bootstrapListeners = HashMultimap.create();
    protected FeatureFlagSet enabledFeatures = FeatureFlagSet.of();

    @ApiStatus.Internal
    protected BasePackGenerator(Registree registree) {
        this.registree = registree;
    }

    @Override
    public TSelf description(Component description) {
        this.description = description;
        return self;
    }

    @Override
    public <TProvider> TSelf providing(ProviderType<TProvider> providerType, BiConsumer<ProviderListenerContext, TProvider> listener) {
        providerListeners.put(providerType, listener);
        return self;
    }

    @Override
    public <TRegistry> TSelf registering(ResourceKey<? extends Registry<TRegistry>> registryType, Consumer<ExtendedRegistryBootstrap<TRegistry>> bootstrap) {
        bootstrapListeners.put(registryType, bootstrap);
        return self;
    }

    @Override
    public TSelf enabling(FeatureFlagSet enabledFeatures) {
        this.enabledFeatures = this.enabledFeatures.join(enabledFeatures);
        return self;
    }

    public void defaultDescription(Supplier<Component> description) {
        if(this.description == null) {
            this.description = description.get();
        }
    }

    @ForOverride
    public PackOutput packOutput(Path outputDir) {
        return new PackOutput(outputDir);
    }

    @ForOverride
    public CompletableFuture<HolderLookup.Provider> generate(PackOutput output, Function<PackType, ResourceManager> resourceManagerGetter, CompletableFuture<HolderLookup.Provider> vanillaRegistries, Consumer<DataProvider> providerConsumer) {
        var context = ProviderContext.of(registree, resourceManagerGetter, enabledFeatures);
        var moddedRegistries = registerDatapackEntries(output, vanillaRegistries, providerConsumer);
        generateMetadata(context, output, providerConsumer);
        registerProviders(context, output, moddedRegistries, providerConsumer);
        return moddedRegistries;
    }

    private void generateMetadata(ProviderContext context, PackOutput output, Consumer<DataProvider> providerConsumer) {
        var description = this.description == null ? CommonComponents.EMPTY : this.description;
        var generatedFeatures = context.enabledFeatures().subtract(FeatureFlags.DEFAULT_FLAGS);
        // TODO: Client if the following matches
        // - no providers or only client providers are used
        // - no datapack entries are registered
        // - no feature flags are enabled
        var packType = PackType.SERVER_DATA;

        var provider = new PackMetadataGenerator(output).add(
                PackMetadataSection.forPackType(packType),
                new PackMetadataSection(description, DetectedVersion.BUILT_IN.packVersion(packType).minorRange())
        );

        if(!generatedFeatures.isEmpty()) {
            if(packType != PackType.SERVER_DATA) {
                throw new IllegalStateException("FeatureFlags are only supported by 'SERVER_DATA' packs");
            }

            provider = provider.add(
                    FeatureFlagsMetadataSection.TYPE,
                    new FeatureFlagsMetadataSection(generatedFeatures)
            );
        }

        providerConsumer.accept(provider);
    }

    @SuppressWarnings({"UnstableApiUsage", "deprecation"})
    private CompletableFuture<HolderLookup.Provider> registerDatapackEntries(PackOutput output, CompletableFuture<HolderLookup.Provider> vanillaRegistries, Consumer<DataProvider> providerConsumer) {
        if(bootstrapListeners.isEmpty()) {
            return vanillaRegistries;
        }

        var registrySet = new RegistrySetBuilder();
        var conditionsMap = Maps.<ResourceKey<?>, List<ICondition>>newHashMap();

        GameData.getRegistrationOrder().stream().map(ResourceKey::createRegistryKey).forEach(registryType -> {
            var bootstrap = registerDatapackEntries(registryType, (registryKey, conditions) -> conditionsMap.put(registryKey, List.of(conditions)));

            if (bootstrap != null) {
                registrySet.add(registryType, bootstrap);
            }
        });

        var patchedRegistries = RegistryPatchGenerator.createLookup(vanillaRegistries, registrySet);
        var moddedRegistries = patchedRegistries.thenApply(RegistrySetBuilder.PatchedRegistries::patches);
        providerConsumer.accept(new RegistriesDatapackGenerator(output, moddedRegistries, Set.of(registree.namespace()), conditionsMap));
        return moddedRegistries;
    }

    @SuppressWarnings("unchecked")
    private <TRegistry> RegistrySetBuilder.@Nullable RegistryBootstrap<TRegistry> registerDatapackEntries(ResourceKey<? extends Registry<TRegistry>> registryType, BiConsumer<ResourceKey<TRegistry>, ICondition[]> conditionsConsumer) {
        var listeners = bootstrapListeners.removeAll(registryType);

        if(listeners.isEmpty()) {
            return null;
        }

        return vanilla -> {
            var extended = new ExtendedRegistryBootstrap<>(vanilla, registryType, registree.namespace(), conditionsConsumer);
            listeners.forEach(listener -> ((Consumer<ExtendedRegistryBootstrap<TRegistry>>) listener).accept(extended));
        };
    }

    private void registerProviders(ProviderContext context, PackOutput output, CompletableFuture<HolderLookup.Provider> registries, Consumer<DataProvider> providerConsumer) {
        providerListeners.keySet().forEach(providerType -> registerProvider(providerType, context, output, registries, providerConsumer));
    }

    private <TProvider> void registerProvider(ProviderType<TProvider> providerType, ProviderContext context, PackOutput output, CompletableFuture<HolderLookup.Provider> registries, Consumer<DataProvider> providerConsumer) {
        providerConsumer.accept(new DataProvider() {
            @SuppressWarnings("DataFlowIssue")
            @Override
            public CompletableFuture<?> run(CachedOutput cachedOutput) {
                return registries.thenCompose(registries -> {
                    var listenerContext = ProviderListenerContext.of(context, registries);
                    var provider = providerType.create(listenerContext);

                    if(provider == null) {
                        return CompletableFuture.completedFuture(null);
                    }

                    providerListeners.removeAll(providerType).forEach(listener -> ((BiConsumer<ProviderListenerContext, TProvider>) listener).accept(listenerContext, provider));
                    return ((BaseProvider) provider).generate(cachedOutput, ProviderOutputContext.of(listenerContext, output));
                });
            }

            @Override
            public String getName() {
                return providerType.registryName().toDebugFileName();
            }
        });
    }
}
