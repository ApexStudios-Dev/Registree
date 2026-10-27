package dev.apexstudios.registree.api.util;

import com.mojang.serialization.Codec;
import dev.apexstudios.registree.api.Registree;
import dev.apexstudios.registree.api.holders.DeferredDataComponentType;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface DataComponentRegistrations {
    static <TValue> DeferredDataComponentType<TValue> register(
            Registree registree,
            String identifier,
            UnaryOperator<DataComponentType.Builder<TValue>> builder
    ) {
        return Registrations.registerForHolder(
                registree,
                Registries.DATA_COMPONENT_TYPE,
                identifier,
                _ -> builder.andThen(DataComponentType.Builder::build)
                        .apply(DataComponentType.builder()),
                DeferredDataComponentType::new
        );
    }

    static <TValue> DeferredDataComponentType<TValue> register(
            Registree registree,
            String identifier,
            Codec<TValue> codec,
            StreamCodec<? super RegistryFriendlyByteBuf, TValue> streamCodec
    ) {
        return register(registree, identifier, builder -> builder
                .persistent(codec)
                .networkSynchronized(streamCodec)
        );
    }
}
