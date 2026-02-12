package dev.apexstudios.registree.registrar;

import dev.apexstudios.registree.Registree;
import dev.apexstudios.registree.holder.DeferredRecipeBookCategory;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeBookCategory;

public class RecipeBookCategoryRegistrar extends Registrar<RecipeBookCategory> {
    public RecipeBookCategoryRegistrar(Registree registree) {
        super(registree, Registries.RECIPE_BOOK_CATEGORY);
    }

    public DeferredRecipeBookCategory register(String identifier) {
        return registerForHolder(identifier, RecipeBookCategory::new, DeferredRecipeBookCategory::createRecipeBookCategory);
    }
}
