package dev.apexstudios.registree.api.holders;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class DeferredRecipeType<TRecipe extends Recipe<?>> extends DeferredHolder<RecipeType<?>, RecipeType<TRecipe>> {
    public DeferredRecipeType(ResourceKey<RecipeType<?>> registryKey) {
        super(registryKey);
    }
}
