package dev.apexstudios.registree.api.util;

import com.mojang.serialization.MapCodec;
import dev.apexstudios.registree.api.Registree;
import dev.apexstudios.registree.api.holders.DeferredRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public interface RecipeSerializerRegistrations {
    static <TRecipe extends Recipe<?>> DeferredRecipeSerializer<TRecipe> register(Registree registree, String identifier, MapCodec<TRecipe> mapCodec, StreamCodec<RegistryFriendlyByteBuf, TRecipe> streamCodec) {
        return Registrations.registerForHolder(
                registree,
                Registries.RECIPE_SERIALIZER,
                identifier,
                _ -> new RecipeSerializer<>(mapCodec, streamCodec),
                DeferredRecipeSerializer::new
        );
    }
}
