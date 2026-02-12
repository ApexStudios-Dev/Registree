package dev.apexstudios.registree.holder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class DeferredRecipeType<TRecipe extends Recipe<?>> extends DeferredHolder<RecipeType<?>, RecipeType<TRecipe>> {
    protected DeferredRecipeType(ResourceKey<RecipeType<?>> registryKey) {
        super(registryKey);
    }

    public static <TRecipe extends Recipe<?>> DeferredRecipeType<TRecipe> createRecipeType(ResourceKey<RecipeType<?>> registryKey) {
        return new DeferredRecipeType<>(registryKey);
    }

    public static <TRecipe extends Recipe<?>> DeferredRecipeType<TRecipe> createRecipeType(Identifier registryName) {
        return createRecipeType(ResourceKey.create(Registries.RECIPE_TYPE, registryName));
    }
}
