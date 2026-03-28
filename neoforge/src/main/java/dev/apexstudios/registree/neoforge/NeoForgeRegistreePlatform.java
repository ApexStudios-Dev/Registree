package dev.apexstudios.registree.neoforge;

import dev.apexstudios.registree.xplat.Registree;
import dev.apexstudios.registree.xplat.RegistreePlatform;
import net.minecraft.world.item.CreativeModeTab;

public final class NeoForgeRegistreePlatform implements RegistreePlatform {
    @Override
    public Registree newRegistree(String namespace) {
        return new NeoForgeRegistree(namespace);
    }

    @Override
    public CreativeModeTab.Builder newCreativeModeTabBuilder() {
        return CreativeModeTab.builder();
    }
}
