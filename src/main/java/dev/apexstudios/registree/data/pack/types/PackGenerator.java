package dev.apexstudios.registree.data.pack.types;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.apexstudios.registree.data.ExtendedRegistryBootstrap;
import dev.apexstudios.registree.data.context.ProviderListenerContext;
import dev.apexstudios.registree.data.provider.ProviderType;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface PackGenerator<TSelf extends PackGenerator<TSelf>> {
    @CanIgnoreReturnValue
    TSelf description(Component description);

    @CanIgnoreReturnValue
    default TSelf description(String description) {
        return description(Component.literal(description));
    }

    @CanIgnoreReturnValue
    <TProvider> TSelf providing(ProviderType<TProvider> providerType, BiConsumer<ProviderListenerContext, TProvider> listener);

    @CanIgnoreReturnValue
    default <TProvider> TSelf providing(ProviderType<TProvider> providerType, Consumer<TProvider> listener) {
        return providing(providerType, (context, provider) -> listener.accept(provider));
    }

    @CanIgnoreReturnValue
    <TRegistry> TSelf registering(ResourceKey<? extends Registry<TRegistry>> registryType, Consumer<ExtendedRegistryBootstrap<TRegistry>> bootstrap);

    @CanIgnoreReturnValue
    TSelf enabling(FeatureFlagSet enabledFeatures);

    @CanIgnoreReturnValue
    default TSelf enabling(FeatureFlag featureFlag) {
        return enabling(FeatureFlagSet.of(featureFlag));
    }

    @CanIgnoreReturnValue
    default TSelf enabling(FeatureFlag featureFlag, FeatureFlag... featureFlags) {
        return enabling(FeatureFlagSet.of(featureFlag, featureFlags));
    }
}
