package dev.apexstudios.registree.registrar;

import dev.apexstudios.registree.Registree;
import dev.apexstudios.registree.builder.BlockBuilder;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.apache.commons.lang3.function.Consumers;

public class BlockRegistrar extends Registrar<Block> {
    public BlockRegistrar(Registree registree) {
        super(registree, Registries.BLOCK);
    }

    public <TBlock extends Block> BlockBuilder<TBlock> builder(String identifier, Function<BlockBehaviour.Properties, TBlock> factory) {
        return new BlockBuilder<>(this, identifier, factory);
    }

    public BlockBuilder<Block> builder(String identifier) {
        return builder(identifier, Block::new);
    }

    public <TBlock extends Block> DeferredBlock<TBlock> register(String identifier, Function<BlockBehaviour.Properties, TBlock> factory, Supplier<? extends BlockBehaviour> initialProperties, boolean copyLegacy, Consumer<BlockBehaviour.Properties> propertiesModifier) {
        return registerForHolder(identifier, registryName -> {
            var properties = (copyLegacy ? BlockBehaviour.Properties.ofLegacyCopy(initialProperties.get()) : BlockBehaviour.Properties.ofFullCopy(initialProperties.get())).setId(registryKey(registryName));
            propertiesModifier.accept(properties);
            return factory.apply(properties);
        }, DeferredBlock::createBlock);
    }

    public <TBlock extends Block> DeferredBlock<TBlock> register(String identifier, Function<BlockBehaviour.Properties, TBlock> factory, Supplier<? extends BlockBehaviour> initialProperties, boolean copyLegacy) {
        return register(identifier, factory, initialProperties, copyLegacy, Consumers.nop());
    }

    public <TBlock extends Block> DeferredBlock<TBlock> register(String identifier, Function<BlockBehaviour.Properties, TBlock> factory, Consumer<BlockBehaviour.Properties> propertiesModifier) {
        return registerForHolder(identifier, registryName -> {
            var properties = BlockBehaviour.Properties.of().setId(registryKey(registryName));
            propertiesModifier.accept(properties);
            return factory.apply(properties);
        }, DeferredBlock::createBlock);
    }

    public <TBlock extends Block> DeferredBlock<TBlock> register(String identifier, Function<BlockBehaviour.Properties, TBlock> factory) {
        return register(identifier, factory, Consumers.nop());
    }

    public DeferredBlock<Block> register(String identifier, Supplier<? extends BlockBehaviour> initialProperties, boolean copyLegacy, Consumer<BlockBehaviour.Properties> propertiesModifier) {
        return register(identifier, Block::new, initialProperties, copyLegacy, propertiesModifier);
    }

    public DeferredBlock<Block> register(String identifier, Supplier<? extends BlockBehaviour> initialProperties, boolean copyLegacy) {
        return register(identifier, initialProperties, copyLegacy, Consumers.nop());
    }

    public DeferredBlock<Block> register(String identifier, Consumer<BlockBehaviour.Properties> propertiesModifier) {
        return register(identifier, Block::new, propertiesModifier);
    }

    public DeferredBlock<Block> register(String identifier) {
        return register(identifier, Consumers.nop());
    }
}
