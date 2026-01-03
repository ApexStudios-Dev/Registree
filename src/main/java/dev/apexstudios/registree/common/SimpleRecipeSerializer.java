package dev.apexstudios.registree.common;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public record SimpleRecipeSerializer<TRecipe extends Recipe<?>>(MapCodec<TRecipe> codec, StreamCodec<RegistryFriendlyByteBuf, TRecipe> streamCodec) implements RecipeSerializer<TRecipe> {
    public static <TRecipe extends Recipe<?>> MapCodec<TRecipe> codec(Function<CraftingBookCategory, TRecipe> factory, Function<TRecipe, CraftingBookCategory> getter) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                CraftingBookCategory.CODEC.fieldOf("category").forGetter(getter)
        ).apply(instance, factory));
    }

    public static <TRecipe extends Recipe<?>> StreamCodec<RegistryFriendlyByteBuf, TRecipe> steamCodec(Function<CraftingBookCategory, TRecipe> factory, Function<TRecipe, CraftingBookCategory> getter) {
        return StreamCodec.composite(
                CraftingBookCategory.STREAM_CODEC, getter,
                factory
        );
    }
}
