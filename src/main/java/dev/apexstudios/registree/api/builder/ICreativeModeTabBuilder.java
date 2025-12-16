package dev.apexstudios.registree.api.builder;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.world.item.CreativeModeTab;

public interface ICreativeModeTabBuilder extends IBuilder.Basic<CreativeModeTab, ICreativeModeTabBuilder> {
    ICreativeModeTabBuilder properties(BiConsumer<IBuilderContext<CreativeModeTab>, CreativeModeTab.Builder> propertiesModifier);

    default ICreativeModeTabBuilder properties(Consumer<CreativeModeTab.Builder> propertiesModifier) {
        return properties((context, properties) -> propertiesModifier.accept(properties));
    }
}
