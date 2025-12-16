package dev.apexstudios.registree.api.registrar;

import dev.apexstudios.registree.api.builder.IBlockBuilder;
import dev.apexstudios.registree.api.holder.DeferredBlock;
import dev.apexstudios.registree.core.builder.BlockBuilder;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public interface IBlockRegistrar extends IRegistrar<Block, DeferredBlock<?>> {
    default <TBlock extends Block> DeferredBlock<TBlock> registerBlock(String identifier, Function<BlockBehaviour.Properties, TBlock> factory, Supplier<BlockBehaviour.Properties> propertiesFactory) {
        return registerForHolder(identifier, registryName -> factory.apply(propertiesFactory.get().setId(registryKey(registryName))));
    }

    default DeferredBlock<Block> registerBlock(String identifier, Supplier<BlockBehaviour.Properties> propertiesFactory) {
        return registerBlock(identifier, Block::new, propertiesFactory);
    }

    default <TBlock extends Block> IBlockBuilder<TBlock> builder(String identifier, Function<BlockBehaviour.Properties, TBlock> factory) {
        return new BlockBuilder<>(this, identifier, factory);
    }

    default IBlockBuilder<Block> builder(String identifier) {
        return builder(identifier, Block::new);
    }
}
