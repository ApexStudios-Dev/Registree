package dev.apexstudios.registree.builder;

import dev.apexstudios.registree.BaseRegistree;
import dev.apexstudios.registree.holder.DeferredFluid;
import dev.apexstudios.registree.holder.Holders;
import java.util.function.Supplier;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public final class FluidBuilder<TFluid extends Fluid> extends AbstractBuilder<Fluid, TFluid, DeferredFluid<TFluid>, FluidBuilder<TFluid>> {
    private final Supplier<TFluid> factory;
    private @Nullable Supplier<Supplier<FluidModel.Unbaked>> model = null;

    @ApiStatus.Internal
    public FluidBuilder(BaseRegistree<?> registree, String identifier, Supplier<TFluid> factory) {
        super(registree, Registries.FLUID, identifier, Holders::createFluid);

        this.factory = factory;
    }

    public FluidBuilder<TFluid> model(Supplier<Supplier<FluidModel.Unbaked>> model) {
        this.model = model;
        return this;
    }

    @Override
    protected TFluid createValue(ResourceKey<Fluid> registryKey) {
        return factory.get();
    }

    @Override
    protected void registerEvents() {
        super.registerEvents();

        RegistryClientEventHelper.registerFluidModel(registree, this::value, model);
    }
}
