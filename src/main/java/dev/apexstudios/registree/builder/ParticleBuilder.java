package dev.apexstudios.registree.builder;

import com.mojang.datafixers.util.Either;
import dev.apexstudios.registree.BaseRegistree;
import dev.apexstudios.registree.holder.DeferredParticle;
import dev.apexstudios.registree.holder.Holders;
import java.util.function.Supplier;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public final class ParticleBuilder<TParticleType extends ParticleType<TOptions>, TOptions extends ParticleOptions> extends AbstractBuilder<ParticleType<?>, TParticleType, DeferredParticle<TParticleType, TOptions>, ParticleBuilder<TParticleType, TOptions>> {
    private final Supplier<TParticleType> factory;
    private @Nullable Either<Supplier<Supplier<ParticleProvider<TOptions>>>, Supplier<Supplier<ParticleResources.SpriteParticleRegistration<TOptions>>>> provider = null;

    @ApiStatus.Internal
    public ParticleBuilder(BaseRegistree<?> registree, String identifier, Supplier<TParticleType> factory) {
        super(registree, Registries.PARTICLE_TYPE, identifier, Holders::createParticle);

        this.factory = factory;
    }

    public ParticleBuilder<TParticleType, TOptions> provider(Supplier<Supplier<ParticleProvider<TOptions>>> provider) {
        this.provider = Either.left(provider);
        return this;
    }

    public ParticleBuilder<TParticleType, TOptions> spriteProvider(Supplier<Supplier<ParticleResources.SpriteParticleRegistration<TOptions>>> provider) {
        this.provider = Either.right(provider);
        return this;
    }

    @Override
    protected TParticleType createValue(ResourceKey<ParticleType<?>> registryKey) {
        return factory.get();
    }

    @Override
    protected void registerEvents() {
        super.registerEvents();

        RegistryClientEventHelper.registerParticleProvider(registree, this::value, provider);
    }
}
