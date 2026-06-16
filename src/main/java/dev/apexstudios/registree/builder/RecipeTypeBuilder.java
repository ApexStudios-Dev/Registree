package dev.apexstudios.registree.builder;

import dev.apexstudios.registree.BaseRegistree;
import dev.apexstudios.registree.holder.DeferredRecipeType;
import dev.apexstudios.registree.holder.Holders;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public final class RecipeTypeBuilder<TRecipe extends Recipe<TInput>, TInput extends RecipeInput> extends AbstractBuilder<RecipeType<?>, RecipeType<TRecipe>, DeferredRecipeType<TRecipe>, RecipeTypeBuilder<TRecipe, TInput>> {
    private @Nullable RegistryEventHelper.RecipeSyncHandler syncHandler = null;

    @ApiStatus.Internal
    public RecipeTypeBuilder(BaseRegistree<?> registree, String identifier) {
        super(registree, Registries.RECIPE_TYPE, identifier, Holders::createRecipeType);
    }

    public RecipeTypeBuilder<TRecipe, TInput> syncRecipeMap(Consumer<RecipeMap> handler, Runnable cleanup) {
        syncHandler = syncHandler == null ? new RegistryEventHelper.RecipeSyncHandler(handler, cleanup) : syncHandler.merge(handler, cleanup);
        return this;
    }

    public RecipeTypeBuilder<TRecipe, TInput> syncRecipes(Consumer<Collection<RecipeHolder<TRecipe>>> handler, Runnable cleanup) {
        return syncRecipeMap(recipeMap -> handler.accept(recipeMap.byType(value())), cleanup);
    }

    public RecipeTypeBuilder<TRecipe, TInput> syncRecipes(List<RecipeHolder<TRecipe>> recipes) {
        return syncRecipes(recipes::addAll, recipes::clear);
    }

    @Override
    protected RecipeType<TRecipe> createValue(ResourceKey<RecipeType<?>> registryKey) {
        return RecipeType.simple(registryKey.identifier());
    }

    @Override
    protected void registerEvents() {
        super.registerEvents();

        RegistryEventHelper.registerRecipeTypeSyncHandler(this::value, syncHandler);
    }
}
