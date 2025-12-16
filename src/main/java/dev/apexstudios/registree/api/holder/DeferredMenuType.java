package dev.apexstudios.registree.api.holder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class DeferredMenuType<TMenu extends AbstractContainerMenu> extends DeferredHolder<MenuType<?>, MenuType<TMenu>> {
    protected DeferredMenuType(ResourceKey<MenuType<?>> registryKey) {
        super(registryKey);
    }
}
