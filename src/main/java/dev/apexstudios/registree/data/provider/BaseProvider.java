package dev.apexstudios.registree.data.provider;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import dev.apexstudios.registree.data.context.ProviderOutputContext;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public interface BaseProvider {
    CompletableFuture<?> generate(CachedOutput cache, ProviderOutputContext context);

    static <E> CompletableFuture<?> saveAll(CachedOutput cache, Encoder<E> encoder, PackOutput.PathProvider pathProvider, Map<Identifier, E> entries) {
        return saveAll(cache, encoder, pathProvider::json, entries);
    }

    static <E> CompletableFuture<?> saveAll(CachedOutput cache, HolderLookup.Provider registries, Encoder<E> encoder, PackOutput.PathProvider pathProvider, Map<Identifier, E> entries) {
        return saveAll(cache, registries, encoder, pathProvider::json, entries);
    }

    static <E> CompletableFuture<?> saveAll(CachedOutput cache, DynamicOps<JsonElement> ops, Encoder<E> encoder, PackOutput.PathProvider pathProvider, Map<Identifier, E> entries) {
        return saveAll(cache, ops, encoder, pathProvider::json, entries);
    }

    static <T, E> CompletableFuture<?> saveAll(CachedOutput cache, Encoder<E> encoder, Function<T, Path> pathProvider, Map<T, E> entries) {
        return saveAll(cache, JsonOps.INSTANCE, encoder, pathProvider, entries);
    }

    static <T, E> CompletableFuture<?> saveAll(CachedOutput cache, HolderLookup.Provider registries, Encoder<E> encoder, Function<T, Path> pathProvider, Map<T, E> entries) {
        return saveAll(cache, registries.createSerializationContext(JsonOps.INSTANCE), encoder, pathProvider, entries);
    }

    static <T, E> CompletableFuture<?> saveAll(CachedOutput cache, DynamicOps<JsonElement> ops, Encoder<E> encoder, Function<T, Path> pathProvider, Map<T, E> entries) {
        return CompletableFuture.allOf(entries.entrySet()
                .stream()
                .map(entry -> saveStable(cache, ops, encoder, entry.getValue(), pathProvider.apply(entry.getKey())))
                .toArray(CompletableFuture[]::new)
        );
    }

    static <T> CompletableFuture<?> saveStable(CachedOutput cache, Encoder<T> encoder, T value, Path path) {
        return saveStable(cache, JsonOps.INSTANCE, encoder, value, path);
    }

    static <T> CompletableFuture<?> saveStable(CachedOutput cache, HolderLookup.Provider registries, Encoder<T> encoder, T value, Path path) {
        return saveStable(cache, registries.createSerializationContext(JsonOps.INSTANCE), encoder, value, path);
    }

    static <T> CompletableFuture<?> saveStable(CachedOutput cache, DynamicOps<JsonElement> ops, Encoder<T> encoder, T value, Path path) {
        var json = encoder.encodeStart(ops, value).getOrThrow();
        return isEmpty(json) ? CompletableFuture.completedFuture(null) : DataProvider.saveStable(cache, json, path);
    }

    static boolean isEmpty(@Nullable JsonElement json) {
        if(json == null || json.isJsonNull())
            return true;

        if(json.isJsonObject()) {
            var obj = json.getAsJsonObject();

            if(obj.isEmpty())
                return true;

            for(var key : obj.keySet()) {
                if(!isEmpty(obj.get(key)))
                    return false;
            }

            return true;
        }

        if(json.isJsonArray()) {
            var array = json.getAsJsonArray();

            if(array.isEmpty())
                return true;

            for(var element : array) {
                if(!isEmpty(element))
                    return false;
            }

            return true;
        }

        return false;
    }
}
