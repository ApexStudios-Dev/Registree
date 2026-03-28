package dev.apexstudios.registree.xplat;

import java.util.ServiceLoader;
import net.minecraft.world.item.CreativeModeTab;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface RegistreePlatform {
    RegistreePlatform INSTANCE = ServiceLoader.load(RegistreePlatform.class).findFirst().orElseThrow();

    Registree newRegistree(String namespace);

    CreativeModeTab.Builder newCreativeModeTabBuilder();
}
