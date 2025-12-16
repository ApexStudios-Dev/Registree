package dev.apexstudios.registree.api.builder;

import dev.apexstudios.registree.api.holder.DeferredMenuType;
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public interface IMenuTypeBuilder<TMenu extends AbstractContainerMenu> extends IBuilder<MenuType<?>, MenuType<TMenu>, DeferredMenuType<TMenu>, IMenuTypeBuilder<TMenu>> {
    IMenuTypeBuilder<TMenu> requiredFeatures(FeatureFlagSet requiredFeatures);

    default IMenuTypeBuilder<TMenu> requiredFeature(FeatureFlag requiredFeature) {
        return requiredFeatures(FeatureFlagSet.of(requiredFeature));
    }

    default IMenuTypeBuilder<TMenu> requiredFeatures(FeatureFlag requiredFeature, FeatureFlag requiredFeatures) {
        return requiredFeatures(FeatureFlagSet.of(requiredFeature, requiredFeatures));
    }

    <TScreen extends Screen & MenuAccess<TMenu>> IMenuTypeBuilder<TMenu> screen(Supplier<Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>>> screenFactory);
}
