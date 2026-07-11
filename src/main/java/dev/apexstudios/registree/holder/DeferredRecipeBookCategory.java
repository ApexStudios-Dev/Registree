package dev.apexstudios.registree.holder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class DeferredRecipeBookCategory extends DeferredHolder<RecipeBookCategory, RecipeBookCategory> {
    DeferredRecipeBookCategory(ResourceKey<RecipeBookCategory> registryKey) {
        super(registryKey);
    }
}
