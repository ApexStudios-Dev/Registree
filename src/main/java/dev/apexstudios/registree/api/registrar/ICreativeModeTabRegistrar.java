package dev.apexstudios.registree.api.registrar;

import dev.apexstudios.registree.api.builder.ICreativeModeTabBuilder;
import dev.apexstudios.registree.api.holder.DeferredItemLike;
import dev.apexstudios.registree.core.builder.CreativeModeTabBuilder;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public interface ICreativeModeTabRegistrar extends IRegistrar<CreativeModeTab> {
    default ResourceKey<CreativeModeTab> registerCreativeModeTab(String identifier, Consumer<CreativeModeTab.Builder> properties) {
        return register(identifier, () -> {
            var builder = CreativeModeTab.builder();
            properties.accept(builder);
            return builder.build();
        });
    }

    default ResourceKey<CreativeModeTab> registerCreativeModeTab(String identifier, Supplier<ItemStack> icon, CreativeModeTab.DisplayItemsGenerator displayItems) {
        return registerCreativeModeTab(identifier, properties -> properties.displayItems(displayItems).icon(icon));
    }

    default ResourceKey<CreativeModeTab> registerCreativeModeTab(String identifier, DeferredItemLike<?, ?> icon) {
        return registerCreativeModeTab(identifier, icon::toStack, (parameters, output) -> {
            var enabledFeatures = parameters.enabledFeatures();

            for(var item : registree().items().values()) {
                if(item.isEnabled(enabledFeatures)) {
                    output.accept(item);
                }
            }
        });
    }

    default ICreativeModeTabBuilder builder(String identifier) {
        return new CreativeModeTabBuilder(this, identifier);
    }
}
