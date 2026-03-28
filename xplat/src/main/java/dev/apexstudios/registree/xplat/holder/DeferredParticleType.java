package dev.apexstudios.registree.xplat.holder;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceKey;

public final class DeferredParticleType<TParticle extends ParticleOptions, TParticleType extends ParticleType<TParticle>> extends DeferredHolder<ParticleType<?>, TParticleType> {
    public DeferredParticleType(ResourceKey<ParticleType<?>> registryKey) {
        super(registryKey);
    }
}
