package dev.apexstudios.registree.core.registrar;

import dev.apexstudios.registree.api.IRegistree;
import dev.apexstudios.registree.api.registrar.IBlockRegistrar;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

public class BlockRegistrar extends Registrar<Block> implements IBlockRegistrar {
    public BlockRegistrar(IRegistree registree) {
        super(registree, Registries.BLOCK);
    }
}
