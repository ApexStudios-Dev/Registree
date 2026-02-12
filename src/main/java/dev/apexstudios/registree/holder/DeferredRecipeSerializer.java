package dev.apexstudios.registree.holder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;

public class DeferredRecipeSerializer<TRecipe extends Recipe<?>> extends DeferredHolder<RecipeSerializer<?>, RecipeSerializer<TRecipe>> {
    protected DeferredRecipeSerializer(ResourceKey<RecipeSerializer<?>> registryKey) {
        super(registryKey);
    }

    public static <TRecipe extends Recipe<?>> DeferredRecipeSerializer<TRecipe> createRecipeSerializer(ResourceKey<RecipeSerializer<?>> registryKey) {
        return new DeferredRecipeSerializer<>(registryKey);
    }

    public static <TRecipe extends Recipe<?>> DeferredRecipeSerializer<TRecipe> createRecipeSerializer(Identifier registryName) {
        return createRecipeSerializer(ResourceKey.create(Registries.RECIPE_SERIALIZER, registryName));
    }
}
