package dev.apexstudios.testmod.neoforge;

import dev.apexstudios.registree.neoforge.NeoForgeRegistree;
import dev.apexstudios.testmod.xplat.TestModXplat;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(TestModXplat.ID)
public final class TestMod implements TestModXplat {
    public TestMod(IEventBus modBus) {
        init();

        NeoForgeRegistree.register(REGISTREE, modBus);
    }
}
