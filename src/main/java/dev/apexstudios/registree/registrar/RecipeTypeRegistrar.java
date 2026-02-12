package dev.apexstudios.registree.registrar;

import dev.apexstudios.registree.Registree;
import dev.apexstudios.registree.holder.DeferredRecipeType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

public class RecipeTypeRegistrar extends Registrar<RecipeType<?>> {
    public RecipeTypeRegistrar(Registree registree) {
        super(registree, Registries.RECIPE_TYPE);
    }

    public <TRecipe extends Recipe<?>> DeferredRecipeType<TRecipe> register(String identifier) {
        return registerForHolder(identifier, RecipeType::simple, DeferredRecipeType::createRecipeType);
    }
}
