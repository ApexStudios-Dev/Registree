package dev.apexstudios.registree.core.registrar;

import dev.apexstudios.registree.api.IRegistree;
import dev.apexstudios.registree.api.registrar.IBlockEntityTypeRegistrar;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockEntityTypeRegistrar extends Registrar<BlockEntityType<?>> implements IBlockEntityTypeRegistrar {
    public BlockEntityTypeRegistrar(IRegistree registree) {
        super(registree, Registries.BLOCK_ENTITY_TYPE);
    }
}
