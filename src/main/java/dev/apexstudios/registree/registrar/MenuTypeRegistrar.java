package dev.apexstudios.registree.registrar;

import com.google.common.collect.Maps;
import dev.apexstudios.registree.Registree;
import dev.apexstudios.registree.holder.DeferredMenuType;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class MenuTypeRegistrar extends Registrar<MenuType<?>> {
    private final Map<DeferredMenuType<?>, Supplier<? extends MenuScreens.ScreenConstructor<?, ?>>> screenFactories = Maps.newHashMap();

    public MenuTypeRegistrar(Registree registree) {
        super(registree, Registries.MENU);

        registree.event(RegisterMenuScreensEvent.class, event -> {
            screenFactories.keySet().forEach(holder -> registerScreen(event, holder));
            screenFactories.clear();
        });
    }

    public <TMenu extends AbstractContainerMenu> DeferredMenuType<TMenu> register(String identifier, MenuType.MenuSupplier<TMenu> factory, FeatureFlagSet requiredFeatures) {
        return registerForHolder(identifier, () -> new MenuType<>(factory, requiredFeatures), DeferredMenuType::createMenuType);
    }

    public <TMenu extends AbstractContainerMenu> DeferredMenuType<TMenu> register(String identifier, MenuType.MenuSupplier<TMenu> factory) {
        return register(identifier, factory, FeatureFlags.DEFAULT_FLAGS);
    }

    public <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenuType<TMenu> register(String identifier, MenuType.MenuSupplier<TMenu> factory, FeatureFlagSet requiredFeatures, Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>> screenFactory) {
        var holder = register(identifier, factory, requiredFeatures);
        screenFactories.put(holder, screenFactory);
        return holder;
    }

    @SuppressWarnings("unchecked")
    private <TMenu extends AbstractContainerMenu> void registerScreen(RegisterMenuScreensEvent event, DeferredMenuType<TMenu> holder) {
        var factory = screenFactories.get(holder);

        if(factory != null) {
            event.register(holder.value(), (MenuScreens.ScreenConstructor<? super TMenu, ?>) factory.get());
        }
    }
}
