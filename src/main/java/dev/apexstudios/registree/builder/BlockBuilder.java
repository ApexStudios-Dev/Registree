package dev.apexstudios.registree.builder;

import com.google.common.base.Predicates;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;
import dev.apexstudios.registree.registrar.BlockRegistrar;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.IBlockCapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialBlockModelRendererEvent;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.world.poi.ExtendPoiTypesEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.apache.commons.lang3.function.Consumers;
import org.jspecify.annotations.Nullable;

public class BlockBuilder<TBlock extends Block> extends Builder<BlockRegistrar, Block, TBlock, DeferredBlock<TBlock>, BlockBuilder.Context<TBlock>> {
    private final Function<BlockBehaviour.Properties, TBlock> factory;
    @SuppressWarnings("deprecation")
    private Function<Context<TBlock>, BlockBehaviour.Properties> initialProperties = context -> BlockBehaviour.Properties.ofLegacyCopy(Blocks.STONE);
    private BiConsumer<Context<TBlock>, BlockBehaviour.Properties> propertiesModifier = (context, properties) -> { };
    private final Multimap<BlockCapability<?, ?>, IBlockCapabilityProvider<?, ?>> capabilities = HashMultimap.create();
    private @Nullable IClientBlockExtensions clientExtensions = null;
    private final Set<Either<ResourceKey<BlockEntityType<?>>, Supplier<BlockEntityType<?>>>> blockEntityTypes = Sets.newHashSet();
    private final Map<ResourceKey<PoiType>, Predicate<BlockState>> poiTypes = Maps.newHashMap();
    private @Nullable Supplier<SpecialModelRenderer.Unbaked> specialModelRenderer = null;
    private @Nullable Supplier<BlockColor> colorHandler = null;
    private @Nullable ChunkSectionLayer renderType = null;

    public BlockBuilder(BlockRegistrar registrar, String identifier, Function<BlockBehaviour.Properties, TBlock> factory) {
        super(registrar, identifier, DeferredBlock::createBlock, Context::new);

        this.factory = factory;
    }

    public BlockBuilder<TBlock> initialProperties(Function<Context<TBlock>, BlockBehaviour.Properties> initialProperties) {
        this.initialProperties = initialProperties;
        return this;
    }

    public BlockBuilder<TBlock> initialProperties(Supplier<BlockBehaviour.Properties> initialProperties) {
        return initialProperties(context -> initialProperties.get());
    }

    @SuppressWarnings("deprecation")
    public BlockBuilder<TBlock> copyProperties(Supplier<? extends BlockBehaviour> copyFrom, boolean copyLegacy) {
        return initialProperties(() -> copyLegacy ? BlockBehaviour.Properties.ofLegacyCopy(copyFrom.get()) : BlockBehaviour.Properties.ofFullCopy(copyFrom.get()));
    }

    public BlockBuilder<TBlock> properties(BiConsumer<Context<TBlock>, BlockBehaviour.Properties> propertiesModifier) {
        this.propertiesModifier = this.propertiesModifier.andThen(propertiesModifier);
        return this;
    }

    public BlockBuilder<TBlock> properties(Consumer<BlockBehaviour.Properties> propertiesModifier) {
        return properties((context, properties) -> propertiesModifier.accept(properties));
    }

    public <TCapability, TContext extends @Nullable Object> BlockBuilder<TBlock> capability(BlockCapability<TCapability, TContext> capability, IBlockCapabilityProvider<TCapability, TContext> provider) {
        capabilities.put(capability, provider);
        return this;
    }

    @SuppressWarnings("unchecked")
    private <TCapability, TContext extends @Nullable Object> void registerCapability(RegisterCapabilitiesEvent event, Context<TBlock> context, BlockCapability<TCapability, TContext> capability) {
        for(var provider : capabilities.get(capability)) {
            event.registerBlock(capability, (IBlockCapabilityProvider<TCapability, TContext>) provider, context.get());
        }
    }

    public BlockBuilder<TBlock> clientExtensions(IClientBlockExtensions clientExtensions) {
        this.clientExtensions = clientExtensions;
        return this;
    }

    public BlockBuilder<TBlock> blockEntityType(Supplier<BlockEntityType<?>> blockEntityType) {
        blockEntityTypes.add(Either.right(blockEntityType));
        return this;
    }

    public BlockBuilder<TBlock> blockEntityType(ResourceKey<BlockEntityType<?>> blockEntityType) {
        blockEntityTypes.add(Either.left(blockEntityType));
        return this;
    }

    public BlockBuilder<TBlock> poiType(ResourceKey<PoiType> poiType, Predicate<BlockState> supportedBlockState) {
        poiTypes.put(poiType, supportedBlockState);
        return this;
    }

    public BlockBuilder<TBlock> poiType(ResourceKey<PoiType> poiType) {
        return poiType(poiType, Predicates.alwaysTrue());
    }

    public <TValue extends Comparable<TValue>> BlockBuilder<TBlock> poiType(ResourceKey<PoiType> poiType, Property<TValue> property, TValue requiredValue) {
        return poiType(poiType, blockState -> blockState.hasProperty(property) && blockState.getValue(property) == requiredValue);
    }

    public BlockBuilder<TBlock> poiType(ResourceKey<PoiType> poiType, Property<?>... requiredProperties) {
        return poiType(poiType, blockState -> Stream.of(requiredProperties).allMatch(blockState::hasProperty));
    }

    public BlockBuilder<TBlock> specialModelRenderer(Supplier<SpecialModelRenderer.Unbaked> specialModelRenderer) {
        this.specialModelRenderer = specialModelRenderer;
        return this;
    }

    public BlockBuilder<TBlock> colorHandler(Supplier<BlockColor> colorHandler) {
        this.colorHandler = colorHandler;
        return this;
    }

    public BlockBuilder<TBlock> renderType(ChunkSectionLayer renderType) {
        this.renderType = renderType;
        return this;
    }

    public <TItem extends Item> BlockBuilder<TBlock> item(String identifier, BiFunction<TBlock, Item.Properties, TItem> factory, Consumer<ItemBuilder<TItem>> modifier) {
        child(context -> {
            var builder = context.registree().items().builder(identifier, properties -> factory.apply(context.get(), properties));
            modifier.accept(builder);
            return builder;
        });

        return this;
    }

    public <TItem extends Item> BlockBuilder<TBlock> item(String identifier, BiFunction<TBlock, Item.Properties, TItem> factory) {
        return item(identifier, factory, Consumers.nop());
    }

    public BlockBuilder<TBlock> item(String identifier, Consumer<ItemBuilder<BlockItem>> modifier) {
        return item(identifier, BlockItem::new, modifier);
    }

    public BlockBuilder<TBlock> item(String identifier) {
        return item(identifier, Consumers.nop());
    }

    public <TItem extends Item> BlockBuilder<TBlock> item(BiFunction<TBlock, Item.Properties, TItem> factory, Consumer<ItemBuilder<TItem>> modifier) {
        child(context -> {
            var builder = context.registree().items().builder(context.identifier(), properties -> factory.apply(context.get(), properties));
            modifier.accept(builder);
            return builder;
        });

        return this;
    }

    public <TItem extends Item> BlockBuilder<TBlock> item(BiFunction<TBlock, Item.Properties, TItem> factory) {
        return item(factory, Consumers.nop());
    }

    public BlockBuilder<TBlock> item(Consumer<ItemBuilder<BlockItem>> modifier) {
        return item(BlockItem::new, modifier);
    }

    public BlockBuilder<TBlock> item() {
        return item(Consumers.nop());
    }

    public <TBlockEntity extends BlockEntity> BlockBuilder<TBlock> blockEntityType(String identifier, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory, Consumer<BlockEntityTypeBuilder<TBlockEntity>> modifier) {
        child(context -> {
            var builder = context.registree().blockEntityTypes().builder(identifier, factory).validBlock(context);
            modifier.accept(builder);
            return builder;
        });

        return this;
    }

    public <TBlockEntity extends BlockEntity> BlockBuilder<TBlock> blockEntityType(String identifier, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory) {
        return blockEntityType(identifier, factory, Consumers.nop());
    }

    public <TBlockEntity extends BlockEntity> BlockBuilder<TBlock> blockEntityType(BlockEntityType.BlockEntitySupplier<TBlockEntity> factory, Consumer<BlockEntityTypeBuilder<TBlockEntity>> modifier) {
        child(context -> {
            var builder = context.registree().blockEntityTypes().builder(context.identifier(), factory).validBlock(context);
            modifier.accept(builder);
            return builder;
        });

        return this;
    }

    public <TBlockEntity extends BlockEntity> BlockBuilder<TBlock> blockEntityType(BlockEntityType.BlockEntitySupplier<TBlockEntity> factory) {
        return blockEntityType(factory, Consumers.nop());
    }

    @Override
    protected TBlock compile(Context<TBlock> context) {
        var properties = initialProperties.apply(context);
        propertiesModifier.accept(context, properties);
        return factory.apply(properties);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void finalize(Context<TBlock> context) {
        if(!capabilities.isEmpty()) {
            context.registree().event(RegisterCapabilitiesEvent.class, event -> {
                for(var capability : capabilities.keySet()) {
                    registerCapability(event, context, capability);
                }

                capabilities.clear();
            });
        }

        if(clientExtensions != null) {
            context.registree().event(RegisterClientExtensionsEvent.class, event -> {
                event.registerBlock(clientExtensions, context.get());
                clientExtensions = null;
            });
        }

        if(!blockEntityTypes.isEmpty()) {
            context.registree().event(BlockEntityTypeAddBlocksEvent.class, event -> {
                blockEntityTypes.forEach(either -> either
                                .ifLeft(registryKey -> event.modify(registryKey, context.get()))
                                .ifRight(blockEntityType -> event.modify(blockEntityType.get(), context.get()))
                );

                blockEntityTypes.clear();
            });
        }

        if(!poiTypes.isEmpty()) {
            context.registree().event(ExtendPoiTypesEvent.class, event -> {
                poiTypes.forEach((poiType, predicate) -> event.addStatesToPoi(poiType, context.get().getStateDefinition().getPossibleStates().stream().filter(predicate).collect(Collectors.toSet())));
                poiTypes.clear();
            });
        }

        if(specialModelRenderer != null) {
            context.registree().event(RegisterSpecialBlockModelRendererEvent.class, event -> {
                event.register(context.get(), specialModelRenderer.get());
                specialModelRenderer = null;
            });
        }

        if(colorHandler != null) {
            context.registree().event(RegisterColorHandlersEvent.Block.class, event -> {
                event.register(colorHandler.get(), context.get());
                colorHandler = null;
            });
        }

        if(renderType != null) {
            context.registree().event(FMLClientSetupEvent.class, event -> event.enqueueWork(() -> {
                ItemBlockRenderTypes.setRenderLayer(context.get(), renderType);
                renderType = null;
            }));
        }
    }

    public static final class Context<TBlock extends Block> extends Builder.Context<BlockRegistrar, Block, TBlock, DeferredBlock<TBlock>> {
        private Context(BlockRegistrar registrar, DeferredBlock<TBlock> holder) {
            super(registrar, holder);
        }
    }
}
