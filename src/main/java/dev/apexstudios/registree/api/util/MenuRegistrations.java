package dev.apexstudios.registree.api.util;

import dev.apexstudios.registree.api.Registree;
import dev.apexstudios.registree.api.holders.DeferredMenuType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.IContainerFactory;

public interface MenuRegistrations {
    static <TMenu extends AbstractContainerMenu> DeferredMenuType<TMenu> register(
            Registree registree,
            String identifier,
            MenuType.MenuSupplier<TMenu> factory,
            FeatureFlagSet requiredFeatures
    ) {
        return Registrations.registerForHolder(
                registree,
                Registries.MENU,
                identifier,
                _ -> new MenuType<>(factory, requiredFeatures),
                DeferredMenuType::new
        );
    }

    static <TMenu extends AbstractContainerMenu> DeferredMenuType<TMenu> register(
            Registree registree,
            String identifier,
            MenuType.MenuSupplier<TMenu> factory
    ) {
        return register(registree, identifier, factory, FeatureFlags.DEFAULT_FLAGS);
    }

    static <TMenu extends AbstractContainerMenu> DeferredMenuType<TMenu> registerForExtraData(
            Registree registree,
            String identifier,
            IContainerFactory<TMenu> factory,
            FeatureFlagSet requiredFeatures
    ) {
        return register(registree, identifier, factory, requiredFeatures);
    }

    static <TMenu extends AbstractContainerMenu> DeferredMenuType<TMenu> registerForExtraData(
            Registree registree,
            String identifier,
            IContainerFactory<TMenu> factory
    ) {
        return register(registree, identifier, factory);
    }
}
