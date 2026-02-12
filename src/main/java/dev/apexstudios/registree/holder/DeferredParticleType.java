package dev.apexstudios.registree.holder;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;

public class DeferredParticleType<TParticleType extends ParticleType<?>> extends DeferredHolder<ParticleType<?>, TParticleType> {
    protected DeferredParticleType(ResourceKey<ParticleType<?>> registryKey) {
        super(registryKey);
    }

    public static <TParticleType extends ParticleType<?>> DeferredParticleType<TParticleType> createParticleType(ResourceKey<ParticleType<?>> registryKey) {
        return new DeferredParticleType<>(registryKey);
    }

    public static <TParticleType extends ParticleType<?>> DeferredParticleType<TParticleType> createParticleType(Identifier registryName) {
        return createParticleType(ResourceKey.create(Registries.PARTICLE_TYPE, registryName));
    }
}
