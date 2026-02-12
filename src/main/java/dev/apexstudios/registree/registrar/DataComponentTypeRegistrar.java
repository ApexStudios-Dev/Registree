package dev.apexstudios.registree.registrar;

import com.mojang.serialization.Codec;
import dev.apexstudios.registree.Registree;
import dev.apexstudios.registree.holder.DeferredDataComponentType;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class DataComponentTypeRegistrar extends Registrar<DataComponentType<?>> {
    public DataComponentTypeRegistrar(Registree registree) {
        super(registree, Registries.DATA_COMPONENT_TYPE);
    }

    public <TValue> DeferredDataComponentType<TValue> register(String identifier, Consumer<DataComponentType.Builder<TValue>> modifier) {
        return registerForHolder(identifier, () -> {
            var builder = DataComponentType.<TValue>builder();
            modifier.accept(builder);
            return builder.build();
        }, DeferredDataComponentType::createDataComponentType);
    }

    public <TValue> DeferredDataComponentType<TValue> register(String identifier, Codec<TValue> codec) {
        return register(identifier, codec, codec);
    }

    public <TValue> DeferredDataComponentType<TValue> register(String identifier, Codec<TValue> codec, Codec<TValue> networkCodec) {
        return register(identifier, codec, ByteBufCodecs.fromCodecWithRegistries(networkCodec));
    }

    public <TValue> DeferredDataComponentType<TValue> register(String identifier, Codec<TValue> codec, StreamCodec<RegistryFriendlyByteBuf, TValue> streamCodec) {
        return register(identifier, builder -> builder.persistent(codec).networkSynchronized(streamCodec));
    }

    public <TValue> DeferredDataComponentType<TValue> register(String identifier, StreamCodec<RegistryFriendlyByteBuf, TValue> streamCodec) {
        return register(identifier, builder -> builder.networkSynchronized(streamCodec));
    }
}
