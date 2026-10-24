package dev.apexstudios.registree.api.util;

import dev.apexstudios.registree.api.Registree;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;

public interface BlockRegistrations {
    static <TBlock extends Block> DeferredBlock<TBlock> register(Registree registree, String identifier, Function<BlockBehaviour.Properties, TBlock> factory, Supplier<BlockBehaviour.Properties> initialProperties, UnaryOperator<BlockBehaviour.Properties> propertiesModifier) {
        return Registrations.registerForHolder(registree, Registries.BLOCK, identifier, registration -> factory.apply(propertiesModifier.apply(initialProperties.get().setId(registration.registryKey()))), DeferredBlock::createBlock);
    }

    static <TBlock extends Block> DeferredBlock<TBlock> register(Registree registree, String identifier, Function<BlockBehaviour.Properties, TBlock> factory, UnaryOperator<BlockBehaviour.Properties> propertiesModifier) {
        return register(registree, identifier, factory, BlockBehaviour.Properties::of, propertiesModifier);
    }

    static <TBlock extends Block> DeferredBlock<TBlock> register(Registree registree, String identifier, Function<BlockBehaviour.Properties, TBlock> factory) {
        return register(registree, identifier, factory, UnaryOperator.identity());
    }

    static DeferredBlock<Block> register(Registree registree, String identifier, Supplier<BlockBehaviour.Properties> initialProperties, UnaryOperator<BlockBehaviour.Properties> propertiesModifier) {
        return register(registree, identifier, Block::new, initialProperties, propertiesModifier);
    }

    static DeferredBlock<Block> register(Registree registree, String identifier, UnaryOperator<BlockBehaviour.Properties> propertiesModifier) {
        return register(registree, identifier, BlockBehaviour.Properties::of, propertiesModifier);
    }

    static DeferredBlock<Block> register(Registree registree, String identifier) {
        return register(registree, identifier, UnaryOperator.identity());
    }
}
