package dev.apexstudios.registree.api.registrar;

import dev.apexstudios.registree.api.builder.IMenuTypeBuilder;
import dev.apexstudios.registree.api.holder.DeferredMenuType;
import dev.apexstudios.registree.core.builder.MenuTypeBuilder;
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.IContainerFactory;

public interface IMenuTypeRegistrar extends IRegistrar.WithHolder<MenuType<?>, DeferredMenuType<?>> {
    default <TMenu extends AbstractContainerMenu> DeferredMenuType<TMenu> registerMenu(String identifier, MenuType.MenuSupplier<TMenu> factory, FeatureFlagSet requiredFeatures) {
        return registerForHolder(identifier, () -> new MenuType<>(factory, requiredFeatures));
    }

    default <TMenu extends AbstractContainerMenu> DeferredMenuType<TMenu> registerMenu(String identifier, MenuType.MenuSupplier<TMenu> factory) {
        return registerMenu(identifier, factory, FeatureFlags.DEFAULT_FLAGS);
    }

    default <TMenu extends AbstractContainerMenu> DeferredMenuType<TMenu> registerMenuExt(String identifier, IContainerFactory<TMenu> factory, FeatureFlagSet requiredFeatures) {
        return registerMenu(identifier, factory, requiredFeatures);
    }

    default <TMenu extends AbstractContainerMenu> DeferredMenuType<TMenu> registerMenuExt(String identifier, IContainerFactory<TMenu> factory) {
        return registerMenuExt(identifier, factory, FeatureFlags.DEFAULT_FLAGS);
    }

    <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenuType<TMenu> registerMenu(String identifier, MenuType.MenuSupplier<TMenu> factory, FeatureFlagSet requiredFeatures, Supplier<Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>>> screenFactory);

    default <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenuType<TMenu> registerMenu(String identifier, MenuType.MenuSupplier<TMenu> factory, Supplier<Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>>> screenFactory) {
        return registerMenu(identifier, factory, FeatureFlags.DEFAULT_FLAGS, screenFactory);
    }

    default <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenuType<TMenu> registerMenuExt(String identifier, IContainerFactory<TMenu> factory, FeatureFlagSet requiredFeatures, Supplier<Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>>> screenFactory) {
        return registerMenu(identifier, factory, requiredFeatures, screenFactory);
    }

    default <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenuType<TMenu> registerMenuExt(String identifier, IContainerFactory<TMenu> factory, Supplier<Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>>> screenFactory) {
        return registerMenuExt(identifier, factory, FeatureFlags.DEFAULT_FLAGS, screenFactory);
    }

    default <TMenu extends AbstractContainerMenu> IMenuTypeBuilder<TMenu> builder(String identifier, MenuType.MenuSupplier<TMenu> factory) {
        return new MenuTypeBuilder<>(this, identifier, factory);
    }

    default <TMenu extends AbstractContainerMenu> IMenuTypeBuilder<TMenu> builderExt(String identifier, IContainerFactory<TMenu> factory) {
        return builder(identifier, factory);
    }
}
