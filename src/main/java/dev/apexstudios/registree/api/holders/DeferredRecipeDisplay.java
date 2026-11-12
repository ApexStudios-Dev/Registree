package dev.apexstudios.registree.api.holders;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

public class DeferredRecipeDisplay<TDisplay extends RecipeDisplay> extends DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<TDisplay>> {
    public DeferredRecipeDisplay(ResourceKey<RecipeDisplay.Type<?>> registryKey) {
        super(registryKey);
    }
}
