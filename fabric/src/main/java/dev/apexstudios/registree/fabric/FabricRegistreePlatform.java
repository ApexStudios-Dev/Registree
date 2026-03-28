package dev.apexstudios.registree.fabric;

import dev.apexstudios.registree.xplat.Registree;
import dev.apexstudios.registree.xplat.RegistreePlatform;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.world.item.CreativeModeTab;

public final class FabricRegistreePlatform implements RegistreePlatform {
    @Override
    public Registree newRegistree(String namespace) {
        return new FabricRegistree(namespace);
    }

    @Override
    public CreativeModeTab.Builder newCreativeModeTabBuilder() {
        return FabricCreativeModeTab.builder();
    }
}
