package dev.apexstudios.testmod.fabric;

import dev.apexstudios.registree.fabric.FabricRegistree;
import dev.apexstudios.testmod.xplat.TestModXplat;
import net.fabricmc.api.ModInitializer;

public final class TestModFabric implements ModInitializer, TestModXplat {
    @Override
    public void onInitialize() {
        init();

        FabricRegistree.register(REGISTREE);
    }
}
