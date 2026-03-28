package dev.apexstudios.registree.fabric.mixin;

import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BuiltInRegistries.class)
public interface BuiltInRegistriesAccessor {
    @Accessor("LOADERS")
    static Map<Identifier, Supplier<?>> getLoaders() {
        throw new IllegalStateException("Mixin not applied!");
    }
}
