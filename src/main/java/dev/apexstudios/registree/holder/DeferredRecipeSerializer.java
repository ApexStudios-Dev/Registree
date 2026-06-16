package dev.apexstudios.registree.holder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class DeferredRecipeSerializer<TRecipe extends Recipe<?>> extends DeferredHolder<RecipeSerializer<?>, RecipeSerializer<TRecipe>> {
    DeferredRecipeSerializer(ResourceKey<RecipeSerializer<?>> registryKey) {
        super(registryKey);
    }
}
