package dev.apexstudios.registree.neoforge;

import dev.apexstudios.registree.xplat.Registree;
import dev.apexstudios.registree.xplat.SimpleRegistree;
import dev.apexstudios.registree.xplat.holder.DeferredMenu;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jspecify.annotations.Nullable;

public final class NeoForgeRegistree extends SimpleRegistree {
    private @Nullable IEventBus modBus = null;
    private @Nullable Consumer<IEventBus> deferredEventRegistration = null;

    NeoForgeRegistree(String namespace) {
        super(namespace);
    }

    private void registerEvents(IEventBus modBus) {
        if(this.modBus != null) {
            return;
        }

        modBus.addListener(RegisterEvent.class, event -> register(event.getRegistry()));
        modBus.addListener(EventPriority.LOW, RegisterEvent.class, event -> invokeListeners(event.getRegistry()));
        modBus.addListener(EventPriority.LOWEST, RegisterEvent.class, event -> frozen = true);

        if(deferredEventRegistration != null) {
            deferredEventRegistration.accept(modBus);
            deferredEventRegistration = null;
        }

        this.modBus = modBus;
    }

    private void defer(Consumer<IEventBus> consumer) {
        if(modBus == null) {
            if(deferredEventRegistration == null) {
                deferredEventRegistration = consumer;
            } else {
                deferredEventRegistration = deferredEventRegistration.andThen(consumer);
            }
        } else {
            consumer.accept(modBus);
        }
    }

    @Override
    public <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, Supplier<Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>>> screenFactory, FeatureFlagSet requiredFeatures) {
        var holder = registerMenu(registryName, factory, requiredFeatures);
        defer(modBus -> modBus.addListener(EventPriority.LOW, RegisterMenuScreensEvent.class, event -> event.register(holder.value(), screenFactory.get().get())));
        return holder;
    }

    public static void register(Registree registree, IEventBus modBus) {
        ((NeoForgeRegistree) registree).registerEvents(modBus);
    }
}
