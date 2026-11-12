package dev.apexstudios.registree.api.util;

import com.mojang.serialization.MapCodec;
import dev.apexstudios.registree.api.Registree;
import dev.apexstudios.registree.api.holders.DeferredRecipeDisplay;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

public interface RecipeDisplayRegistrations {
    static <TDisplay extends RecipeDisplay> DeferredRecipeDisplay<TDisplay> register(
            Registree registree,
            String identifier,
            MapCodec<TDisplay> mapCodec,
            StreamCodec<RegistryFriendlyByteBuf, TDisplay> streamCodec
    ) {
        return Registrations.registerForHolder(
                registree,
                Registries.RECIPE_DISPLAY,
                identifier,
                _ -> new RecipeDisplay.Type<>(mapCodec, streamCodec),
                DeferredRecipeDisplay::new
        );
    }
}
