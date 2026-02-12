package dev.apexstudios.registree.registrar;

import dev.apexstudios.registree.Registree;
import dev.apexstudios.registree.holder.DeferredParticleType;
import java.util.function.Supplier;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;

public class ParticleTypeRegistrar extends Registrar<ParticleType<?>> {
    public ParticleTypeRegistrar(Registree registree) {
        super(registree, Registries.PARTICLE_TYPE);
    }

    public <TParticleType extends ParticleType<?>> DeferredParticleType<TParticleType> register(String identifier, Supplier<TParticleType> factory) {
        return registerForHolder(identifier, factory, DeferredParticleType::createParticleType);
    }

    public DeferredParticleType<SimpleParticleType> register(String identifier, boolean overrideLimiter) {
        return register(identifier, () -> new SimpleParticleType(overrideLimiter));
    }

    public DeferredParticleType<SimpleParticleType> register(String identifier) {
        return register(identifier, false);
    }
}
