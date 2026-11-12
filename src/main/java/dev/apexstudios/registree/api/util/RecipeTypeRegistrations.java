package dev.apexstudios.registree.api.util;

import dev.apexstudios.registree.api.Registree;
import dev.apexstudios.registree.api.holders.DeferredRecipeType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

public interface RecipeTypeRegistrations {
    static <TRecipe extends Recipe<?>> DeferredRecipeType<TRecipe> register(Registree registree, String identifier) {
        return Registrations.registerForHolder(
                registree,
                Registries.RECIPE_TYPE,
                identifier,
                registration -> RecipeType.simple(registration.registryKey().identifier()),
                DeferredRecipeType::new
        );
    }
}
