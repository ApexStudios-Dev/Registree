package dev.apexstudios.registree.holder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class DeferredMenu<TMenu extends AbstractContainerMenu> extends DeferredHolder<MenuType<?>, MenuType<TMenu>> {
    DeferredMenu(ResourceKey<MenuType<?>> registryKey) {
        super(registryKey);
    }
}
