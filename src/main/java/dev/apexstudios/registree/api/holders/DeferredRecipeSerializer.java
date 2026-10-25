package dev.apexstudios.registree.api.holders;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;

public class DeferredRecipeSerializer<TRecipe extends Recipe<?>> extends DeferredHolder<RecipeSerializer<?>, RecipeSerializer<TRecipe>> {
    public DeferredRecipeSerializer(ResourceKey<RecipeSerializer<?>> registryKey) {
        super(registryKey);
    }
}
