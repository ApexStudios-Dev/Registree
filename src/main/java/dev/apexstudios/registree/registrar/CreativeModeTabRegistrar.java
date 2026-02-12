package dev.apexstudios.registree.registrar;

import dev.apexstudios.registree.Registree;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

public class CreativeModeTabRegistrar extends Registrar<CreativeModeTab> {
    public CreativeModeTabRegistrar(Registree registree) {
        super(registree, Registries.CREATIVE_MODE_TAB);
    }

    public ResourceKey<CreativeModeTab> register(String identifier, BiConsumer<Identifier, CreativeModeTab.Builder> modifier) {
        return registerForKey(identifier, registryName -> {
            var builder = CreativeModeTab.builder();
            modifier.accept(registryName, builder);
            return builder.build();
        });
    }

    public ResourceKey<CreativeModeTab> register(String identifier, Consumer<CreativeModeTab.Builder> modifier) {
        return register(identifier, (registryName, builder) -> modifier.accept(builder));
    }

    public ResourceKey<CreativeModeTab> register(String identifier, Supplier<ItemInstance> iconFactory, CreativeModeTab.DisplayItemsGenerator displayItemsGenerator) {
        return register(identifier, (registryName, builder) -> builder
                .icon(() -> fromInstance(iconFactory.get()))
                .displayItems(displayItemsGenerator)
                .title(Component.translatable(descriptionId(registryName)))
        );
    }

    public static String descriptionId(ResourceKey<CreativeModeTab> registryKey) {
        return descriptionId(registryKey.identifier());
    }

    public static String descriptionId(Identifier registryName) {
        return Util.makeDescriptionId("itemGroup", registryName);
    }

    private static ItemStack fromInstance(ItemInstance instance) {
        return switch (instance) {
            case ItemStack stack -> stack;

            case ItemStackTemplate template -> {
                var stack = template.create();
                stack.setCount(1);
                yield stack;
            }

            default -> new ItemStack(instance.typeHolder(), 1);
        };
    }
}
