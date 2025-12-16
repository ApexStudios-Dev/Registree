package dev.apexstudios.registree.core.builder;

import dev.apexstudios.registree.api.builder.IBuilderContext;
import dev.apexstudios.registree.api.builder.ICreativeModeTabBuilder;
import dev.apexstudios.registree.api.registrar.ICreativeModeTabRegistrar;
import java.util.function.BiConsumer;
import net.minecraft.world.item.CreativeModeTab;

public class CreativeModeTabBuilder extends Builder.Basic<ICreativeModeTabRegistrar, CreativeModeTab, ICreativeModeTabBuilder> implements ICreativeModeTabBuilder {
    private BiConsumer<IBuilderContext<CreativeModeTab>, CreativeModeTab.Builder> propertiesModifier = (context, properties) -> { };

    public CreativeModeTabBuilder(ICreativeModeTabRegistrar registrar, String identifier) {
        super(registrar, identifier);
    }

    @Override
    protected CreativeModeTab createElement(IBuilderContext<CreativeModeTab> context) {
        var builder = CreativeModeTab.builder();
        propertiesModifier.accept(context, builder);
        return builder.build();
    }

    @Override
    public ICreativeModeTabBuilder properties(BiConsumer<IBuilderContext<CreativeModeTab>, CreativeModeTab.Builder> propertiesModifier) {
        this.propertiesModifier = this.propertiesModifier.andThen(propertiesModifier);
        return this;
    }
}
