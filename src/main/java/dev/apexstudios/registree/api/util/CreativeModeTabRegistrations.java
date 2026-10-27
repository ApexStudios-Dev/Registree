package dev.apexstudios.registree.api.util;

import dev.apexstudios.registree.api.Registree;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

public interface CreativeModeTabRegistrations {
    static ResourceKey<CreativeModeTab> register(
            Registree registree,
            String identifier,
            UnaryOperator<CreativeModeTab.Builder> builder
    ) {
        return Registrations.register(
                registree,
                Registries.CREATIVE_MODE_TAB,
                identifier,
                registration -> builder.andThen(CreativeModeTab.Builder::build)
                        .<CreativeModeTab.Builder>compose(properties -> properties.title(Component.translatable(defaultTranslationKey(registration.registryKey()))))
                        .apply(CreativeModeTab.builder())
        );
    }

    static ResourceKey<CreativeModeTab> register(
            Registree registree,
            String identifier,
            Supplier<ItemStack> icon,
            CreativeModeTab.DisplayItemsGenerator items
    ) {
        return register(registree, identifier, builder -> builder
                .icon(icon)
                .displayItems(items)
        );
    }

    static ResourceKey<CreativeModeTab> register(
            Registree registree,
            String identifier,
            Supplier<ItemStack> icon,
            Consumer<CreativeModeTab.Output> items
    ) {
        return register(registree, identifier, icon, (_, output) -> items.accept(output));
    }

    static void append(Registree registree, ResourceKey<CreativeModeTab> creativeModeTab, CreativeModeTab.DisplayItemsGenerator appender) {
        registree.events().addListener(BuildCreativeModeTabContentsEvent.class, event -> {
            if(event.getTabKey() == creativeModeTab) {
                appender.accept(event.getParameters(), event);
            }
        });
    }

    static void append(Registree registree, ResourceKey<CreativeModeTab> creativeModeTab, Consumer<CreativeModeTab.Output> appender) {
        append(registree, creativeModeTab, (_, output) -> appender.accept(output));
    }

    static void append(Registree registree, ResourceKey<CreativeModeTab> creativeModeTab, Supplier<ItemStack> item) {
        append(registree, creativeModeTab, output -> output.accept(item.get()));
    }

    static String defaultTranslationKey(ResourceKey<CreativeModeTab> registryKey) {
        return registryKey.identifier().toLanguageKey("itemGroup");
    }
}
