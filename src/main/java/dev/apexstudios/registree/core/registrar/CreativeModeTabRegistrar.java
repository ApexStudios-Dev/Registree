package dev.apexstudios.registree.core.registrar;

import dev.apexstudios.registree.api.IRegistree;
import dev.apexstudios.registree.api.registrar.ICreativeModeTabRegistrar;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;

public class CreativeModeTabRegistrar extends Registrar<CreativeModeTab> implements ICreativeModeTabRegistrar {
    public CreativeModeTabRegistrar(IRegistree registree) {
        super(registree, Registries.CREATIVE_MODE_TAB);
    }
}
