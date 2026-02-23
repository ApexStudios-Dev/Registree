package dev.apexstudios.registree.data.provider;

import dev.apexstudios.registree.data.context.ProviderListenerContext;
import java.util.function.Supplier;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jspecify.annotations.Nullable;

public sealed abstract class ProviderType<TProvider> {
    private final Identifier registryName;

    private ProviderType(Identifier registryName) {
        this.registryName = registryName;
    }

    public Identifier registryName() {
        return registryName;
    }

    public abstract @Nullable TProvider create(ProviderListenerContext context);

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }

        if(!(obj instanceof ProviderType<?> other)) {
            return false;
        }

        return registryName.equals(other.registryName);
    }

    @Override
    public int hashCode() {
        return registryName.hashCode();
    }

    @Override
    public String toString() {
        return "ProviderType{" + registryName + '}';
    }

    public static <TProvider> ProviderType<TProvider> create(Identifier registryName, Factory<TProvider> factory) {
        return new Instance<>(registryName, factory);
    }

    public static <TProvider> ProviderType<TProvider> create(Identifier registryName, Supplier<TProvider> factory) {
        return create(registryName, context -> factory.get());
    }

    public static <TProvider> ProviderType<TProvider> forDist(Identifier registryName, Dist dist, Supplier<Factory<TProvider>> factorySupplier) {
        return new ForDist<>(registryName, dist, factorySupplier);
    }

    public static <TProvider> ProviderType<TProvider> forDistSimple(Identifier registryName, Dist dist, Supplier<Supplier<TProvider>> factorySupplier) {
        return forDist(registryName, dist, () -> context -> factorySupplier.get().get());
    }

    @FunctionalInterface
    public interface Factory<TProvider> {
        TProvider create(ProviderListenerContext context);
    }

    private static final class ForDist<TProvider> extends ProviderType<TProvider> {
        private final Dist dist;
        private final Supplier<Factory<TProvider>> factorySupplier;

        private ForDist(Identifier registryName, Dist dist, Supplier<Factory<TProvider>> factorySupplier) {
            super(registryName);

            this.dist = dist;
            this.factorySupplier = factorySupplier;
        }

        @Override
        public @Nullable TProvider create(ProviderListenerContext context) {
            return FMLEnvironment.getDist() == dist ? factorySupplier.get().create(context) : null;
        }
    }

    private static final class Instance<TProvider> extends ProviderType<TProvider> {
        private final Factory<TProvider> factory;

        private Instance(Identifier registryName, Factory<TProvider> factory) {
            super(registryName);

            this.factory = factory;
        }

        @Override
        public TProvider create(ProviderListenerContext context) {
            return factory.create(context);
        }
    }
}
