package dev.apexstudios.registree.holder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class DeferredMenuType<TMenu extends AbstractContainerMenu> extends DeferredHolder<MenuType<?>, MenuType<TMenu>> {
    protected DeferredMenuType(ResourceKey<MenuType<?>> key) {
        super(key);
    }

    public static <TMenu extends AbstractContainerMenu> DeferredMenuType<TMenu> createMenuType(ResourceKey<MenuType<?>> registryKey) {
        return new DeferredMenuType<>(registryKey);
    }

    public static <TMenu extends AbstractContainerMenu> DeferredMenuType<TMenu> createMenuType(Identifier registryName) {
        return createMenuType(ResourceKey.create(Registries.MENU, registryName));
    }
}
