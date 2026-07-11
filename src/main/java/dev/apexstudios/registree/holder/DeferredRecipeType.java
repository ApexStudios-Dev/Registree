package dev.apexstudios.registree.holder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class DeferredRecipeType<TRecipe extends Recipe<?>> extends DeferredHolder<RecipeType<?>, RecipeType<TRecipe>> {
    DeferredRecipeType(ResourceKey<RecipeType<?>> registryKey) {
        super(registryKey);
    }
}
