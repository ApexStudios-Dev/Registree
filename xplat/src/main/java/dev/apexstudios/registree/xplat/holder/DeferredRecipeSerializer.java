package dev.apexstudios.registree.xplat.holder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class DeferredRecipeSerializer<TRecipe extends Recipe<?>> extends DeferredHolder<RecipeSerializer<?>, RecipeSerializer<TRecipe>> {
    public DeferredRecipeSerializer(ResourceKey<RecipeSerializer<?>> registryKey) {
        super(registryKey);
    }
}
