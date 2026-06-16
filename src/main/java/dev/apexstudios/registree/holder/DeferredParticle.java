package dev.apexstudios.registree.holder;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class DeferredParticle<TParticleType extends ParticleType<TOptions>, TOptions extends ParticleOptions> extends DeferredHolder<ParticleType<?>, TParticleType> {
    DeferredParticle(ResourceKey<ParticleType<?>> registryKey) {
        super(registryKey);
    }
}
