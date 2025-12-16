package dev.apexstudios.registree.core.builder;

import dev.apexstudios.registree.api.builder.IBuilderContext;
import dev.apexstudios.registree.api.builder.IMenuTypeBuilder;
import dev.apexstudios.registree.api.holder.DeferredMenuType;
import dev.apexstudios.registree.api.registrar.IMenuTypeRegistrar;
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.jspecify.annotations.Nullable;

public class MenuTypeBuilder<TMenu extends AbstractContainerMenu> extends Builder.WithHolder<IMenuTypeRegistrar, MenuType<?>, MenuType<TMenu>, DeferredMenuType<TMenu>, IMenuTypeBuilder<TMenu>> implements IMenuTypeBuilder<TMenu> {
    private final MenuType.MenuSupplier<TMenu> factory;
    private FeatureFlagSet requiredFeatures = FeatureFlags.DEFAULT_FLAGS;
    @Nullable private Supplier<Supplier<MenuScreens.ScreenConstructor<TMenu, ? extends Screen>>> screenFactory = null;

    public MenuTypeBuilder(IMenuTypeRegistrar registrar, String identifier, MenuType.MenuSupplier<TMenu> factory) {
        super(registrar, identifier);

        this.factory = factory;

        onRegister(menuType -> {
            registrar.registree().event(RegisterMenuScreensEvent.class, event -> {
                if(screenFactory != null) {
                    event.register(menuType, screenFactory.get().get());
                    screenFactory = null;
                }
            });
        });
    }

    @Override
    protected MenuType<TMenu> createElement(IBuilderContext<MenuType<?>> context) {
        return new MenuType<>(factory, requiredFeatures);
    }

    @Override
    public IMenuTypeBuilder<TMenu> requiredFeatures(FeatureFlagSet requiredFeatures) {
        this.requiredFeatures = requiredFeatures;
        return this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <TScreen extends Screen & MenuAccess<TMenu>> IMenuTypeBuilder<TMenu> screen(Supplier<Supplier<MenuScreens.ScreenConstructor<TMenu, TScreen>>> screenFactory) {
        this.screenFactory = (Supplier) screenFactory;
        return this;
    }
}
