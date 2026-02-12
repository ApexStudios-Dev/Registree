package dev.apexstudios.registree.registrar;

import com.mojang.serialization.MapCodec;
import dev.apexstudios.registree.Registree;
import dev.apexstudios.registree.holder.DeferredRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class RecipeSerializerRegistrar extends Registrar<RecipeSerializer<?>> {
    public RecipeSerializerRegistrar(Registree registree) {
        super(registree, Registries.RECIPE_SERIALIZER);
    }

    public <TRecipe extends Recipe<?>> DeferredRecipeSerializer<TRecipe> register(String identifier, MapCodec<TRecipe> codec, StreamCodec<RegistryFriendlyByteBuf, TRecipe> streamCodec) {
        return registerForHolder(identifier, () -> new RecipeSerializer<>(codec, streamCodec), DeferredRecipeSerializer::createRecipeSerializer);
    }
}
