package dev.apexstudios.registree.core.registrar;

import dev.apexstudios.registree.api.IRegistree;
import dev.apexstudios.registree.api.holder.DeferredMenuType;
import dev.apexstudios.registree.api.registrar.IMenuTypeRegistrar;
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class MenuTypeRegistrar extends Registrar<MenuType<?>> implements IMenuTypeRegistrar {
    public MenuTypeRegistrar(IRegistree registree) {
        super(registree, Registries.MENU);
    }

    @Override
    public <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenuType<TMenu> registerMenu(String identifier, MenuType.MenuSupplier<TMenu> factory, FeatureFlagSet requiredFeatures, Supplier<Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>>> screenFactory) {
        var holder = registerMenu(identifier, factory, requiredFeatures);
        registree().event(RegisterMenuScreensEvent.class, event -> event.register(holder.value(), screenFactory.get().get()));
        return holder;
    }
}
