package dev.apexstudios.registree.builder;

import dev.apexstudios.registree.BaseRegistree;
import dev.apexstudios.registree.holder.DeferredMenu;
import dev.apexstudios.registree.holder.Holders;
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public final class MenuBuilder<TMenu extends AbstractContainerMenu> extends AbstractBuilder<MenuType<?>, MenuType<TMenu>, DeferredMenu<TMenu>, MenuBuilder<TMenu>> {
    private final MenuType.MenuSupplier<TMenu> factory;
    private FeatureFlagSet requiredFeatures = FeatureFlagSet.of();
    private @Nullable Supplier<Supplier<MenuScreens.ScreenConstructor<TMenu, ?>>> screenFactory = null;

    @ApiStatus.Internal
    public MenuBuilder(BaseRegistree<?> registree, String identifier, MenuType.MenuSupplier<TMenu> factory) {
        super(registree, Registries.MENU, identifier, Holders::createMenu);

        this.factory = factory;
    }

    public MenuBuilder<TMenu> requiredFeatures(FeatureFlagSet requiredFeatures) {
        this.requiredFeatures = this.requiredFeatures.join(requiredFeatures);
        return this;
    }

    public MenuBuilder<TMenu> requiredFeatures(FeatureFlag flag) {
        return requiredFeatures(FeatureFlagSet.of(flag));
    }

    public MenuBuilder<TMenu> requiredFeatures(FeatureFlag flag, FeatureFlag... flags) {
        return requiredFeatures(FeatureFlagSet.of(flag, flags));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <TScreen extends Screen & MenuAccess<TMenu>> MenuBuilder<TMenu> screen(Supplier<Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>>> screenFactory) {
        this.screenFactory = (Supplier) screenFactory;
        return this;
    }

    @Override
    protected MenuType<TMenu> createValue(ResourceKey<MenuType<?>> registryKey) {
        return new MenuType<>(factory, requiredFeatures);
    }

    @Override
    protected void registerEvents() {
        super.registerEvents();

        if(FMLEnvironment.getDist().isClient()) {
            RegistryClientEventHelper.registerMenuScreenFactory(registree, this::value, screenFactory);
        }
    }
}
