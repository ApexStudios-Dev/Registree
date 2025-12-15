package dev.apexstudios.registree.core.builder;

import com.google.common.collect.Maps;
import dev.apexstudios.registree.api.builder.IBlockBuilder;
import dev.apexstudios.registree.api.builder.IBlockEntityTypeBuilder;
import dev.apexstudios.registree.api.builder.IBuilderContext;
import dev.apexstudios.registree.api.builder.IItemBuilder;
import dev.apexstudios.registree.api.registrar.IBlockRegistrar;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.IBlockCapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.jspecify.annotations.Nullable;

public class BlockBuilder<TBlock extends Block> extends Builder<Block, TBlock, DeferredBlock<TBlock>, IBlockBuilder<TBlock>> implements IBlockBuilder<TBlock> {
    private Function<IBuilderContext<Block>, BlockBehaviour.Properties> initialProperties = context -> BlockBehaviour.Properties.ofLegacyCopy(Blocks.STONE);
    private BiConsumer<IBuilderContext<Block>, BlockBehaviour.Properties> propertiesModifier = (context, properties) -> properties.setId(context.registryKey());
    private final Function<BlockBehaviour.Properties, TBlock> factory;
    private final Map<BlockCapability<?, ?>, IBlockCapabilityProvider<?, ?>> capabilities = Maps.newHashMap();
    @Nullable private Supplier<Supplier<BlockColor>> blockColor = null;
    @Nullable private Supplier<Supplier<IClientBlockExtensions>> clientExtensions = null;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public BlockBuilder(IBlockRegistrar registrar, String identifier, Function<BlockBehaviour.Properties, TBlock> factory) {
        super(registrar, identifier, DeferredBlock::createBlock);

        this.factory = factory;

        onRegister(block -> {
            registrar.registree().event(RegisterColorHandlersEvent.Block.class, event -> {
                if(blockColor != null) {
                    event.register(blockColor.get().get(), block);
                    blockColor = null;
                }
            });

            registrar.registree().event(RegisterCapabilitiesEvent.class, event -> {
                capabilities.forEach((capability, capabilityProvider) -> event.registerBlock((BlockCapability) capability, capabilityProvider, block));
                capabilities.clear();
            });

            registrar.registree().event(RegisterClientExtensionsEvent.class, event -> {
                if(clientExtensions != null) {
                    event.registerBlock(clientExtensions.get().get(), block);
                    clientExtensions = null;
                }
            });
        });
    }

    @Override
    protected TBlock createElement(IBuilderContext<Block> context) {
        var properties = initialProperties.apply(context);
        propertiesModifier.accept(context, properties);
        return factory.apply(properties);
    }

    @Override
    public IBlockBuilder<TBlock> initialProperties(Function<IBuilderContext<Block>, BlockBehaviour.Properties> initialProperties) {
        this.initialProperties = initialProperties;
        return this;
    }

    @Override
    public IBlockBuilder<TBlock> properties(BiConsumer<IBuilderContext<Block>, BlockBehaviour.Properties> propertiesModifier) {
        this.propertiesModifier = this.propertiesModifier.andThen(propertiesModifier);
        return this;
    }

    @Override
    public <TCapability, TContext> IBlockBuilder<TBlock> capability(BlockCapability<TCapability, TContext> capability, IBlockCapabilityProvider<TCapability, TContext> capabilityProvider) {
        capabilities.put(capability, capabilityProvider);
        return this;
    }

    @Override
    public IBlockBuilder<TBlock> extensions(Supplier<Supplier<IClientBlockExtensions>> clientExtensions) {
        this.clientExtensions = clientExtensions;
        return this;
    }

    @Override
    public <TItem extends Item> IBlockBuilder<TBlock> item(BiFunction<TBlock, Item.Properties, TItem> itemFactory, BiConsumer<IBuilderContext<Block>, IItemBuilder<TItem>> itemBuilder) {
        return defer(context -> {
            var builder = registrar.registree().items().builder(context.identifier(), properties -> itemFactory.apply(context.value(), properties));
            itemBuilder.accept(context, builder);
            builder.register();
        });
    }

    @Override
    public <TBlockEntity extends BlockEntity> IBlockBuilder<TBlock> blockEntity(BlockEntityType.BlockEntitySupplier<TBlockEntity> blockEntityFactory, BiConsumer<IBuilderContext<Block>, IBlockEntityTypeBuilder<TBlockEntity>> blockEntityTypeBuilder) {
        return defer(context -> {
            var builder = registrar.registree().blockEntityTypes().builder(context.identifier(), blockEntityFactory).validBlock(context::value);
            blockEntityTypeBuilder.accept(context, builder);
            builder.register();
        });
    }
}
