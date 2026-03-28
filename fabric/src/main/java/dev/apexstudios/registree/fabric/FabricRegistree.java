package dev.apexstudios.registree.fabric;

import com.google.common.base.Suppliers;
import dev.apexstudios.registree.fabric.mixin.BuiltInRegistriesAccessor;
import dev.apexstudios.registree.xplat.Registree;
import dev.apexstudios.registree.xplat.SimpleRegistree;
import dev.apexstudios.registree.xplat.holder.DeferredMenu;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jspecify.annotations.Nullable;

public final class FabricRegistree extends SimpleRegistree {
    private static final Comparator<Identifier> IDENTIFIER_BY_NAMESPACE = Comparator.comparing(Identifier::getNamespace).thenComparing(Identifier::getPath);

    private static final Supplier<Set<Identifier>> REGISTRY_ORDER = Suppliers.memoize(() -> {
        // Match NeoForge
        var ordered = new LinkedHashSet<Identifier>();
        ordered.add(Registries.ATTRIBUTE.identifier()); // Vanilla order is incorrect, both Item and MobEffect depend on Attribute at construction time.
        ordered.add(Registries.DATA_COMPONENT_TYPE.identifier()); // Vanilla order is incorrect, Item depends on data components at construction time.
        ordered.add(Registries.PARTICLE_TYPE.identifier()); // Vanilla order is incorrect, both Block and MobEffect depend on ParticleType at construction time.
        ordered.addAll(BuiltInRegistriesAccessor.getLoaders().keySet());
        ordered.addAll(BuiltInRegistries.REGISTRY.keySet().stream().sorted(IDENTIFIER_BY_NAMESPACE).toList());
        return ordered;
    });

    private @Nullable Runnable deferredRegistration = null;

    FabricRegistree(String namespace) {
        super(namespace);
    }

    private void register() {
        if(frozen) {
            return;
        }

        frozen = true;

        forEachRegistry(this::register);
        forEachRegistry(this::invokeListeners);

        if(deferredRegistration != null) {
            deferredRegistration.run();
            deferredRegistration = null;
        }
    }

    private void defer(Runnable action) {
        if(frozen) {
            action.run();
        } else {
            if(deferredRegistration == null) {
                deferredRegistration = action;
            } else {
                deferredRegistration = andThen(deferredRegistration, action);
            }
        }
    }

    @Override
    public <TMenu extends AbstractContainerMenu, TScreen extends Screen & MenuAccess<TMenu>> DeferredMenu<TMenu> registerMenu(String registryName, MenuType.MenuSupplier<TMenu> factory, Supplier<Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>>> screenFactory, FeatureFlagSet requiredFeatures) {
        var holder = registerMenu(registryName, factory, requiredFeatures);
        defer(() -> MenuScreens.register(holder.value(), screenFactory.get().get()));
        return holder;
    }

    public static void register(Registree registree) {
        ((FabricRegistree) registree).register();
    }

    public static void forEachRegistry(Consumer<Registry<?>> action) {
        for(var registryName : REGISTRY_ORDER.get()) {
            action.accept(Objects.requireNonNull(BuiltInRegistries.REGISTRY.getValue(registryName)));
        }
    }

    private static Runnable andThen(Runnable before, Runnable after) {
        Objects.requireNonNull(before);
        Objects.requireNonNull(after);

        return () -> {
            before.run();
            after.run();
        };
    }
}
