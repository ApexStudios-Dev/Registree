package dev.apexstudios.registree.holder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.neoforged.neoforge.registries.DeferredHolder;

public class DeferredRecipeBookCategory extends DeferredHolder<RecipeBookCategory, RecipeBookCategory> {
    protected DeferredRecipeBookCategory(ResourceKey<RecipeBookCategory> registryKey) {
        super(registryKey);
    }

    public static DeferredRecipeBookCategory createRecipeBookCategory(ResourceKey<RecipeBookCategory> registryKey) {
        return new DeferredRecipeBookCategory(registryKey);
    }

    public static DeferredRecipeBookCategory createRecipeBookCategory(Identifier registryName) {
        return createRecipeBookCategory(ResourceKey.create(Registries.RECIPE_BOOK_CATEGORY, registryName));
    }
}
