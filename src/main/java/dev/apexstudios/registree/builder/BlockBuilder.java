package dev.apexstudios.registree.builder;

import com.google.common.base.Predicates;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import dev.apexstudios.registree.BaseRegistree;
import dev.apexstudios.registree.holder.Holders;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BuiltInBlockModels;
import net.minecraft.core.registries.Registries;
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
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.IBlockCapabilityProvider;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public final class BlockBuilder<TBlock extends Block> extends AbstractBuilder<Block, TBlock, DeferredBlock<TBlock>, BlockBuilder<TBlock>> {
    private final Function<BlockBehaviour.Properties, TBlock> factory;
    private Supplier<BlockBehaviour.Properties> initialProperties = () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE);
    private Function<BlockBehaviour.Properties, BlockBehaviour.Properties> propertiesModifier = Function.identity();
    private final Multimap<BlockCapability<?, ?>, IBlockCapabilityProvider<?, ?>> capabilities = MultimapBuilder.linkedHashKeys().linkedListValues().build();
    private @Nullable Supplier<Supplier<IClientBlockExtensions>> clientExtension = null;
    private final List<Supplier<? extends BlockEntityType<?>>> blockEntityTypes = new LinkedList<>();
    private @Nullable Supplier<Supplier<List<BlockTintSource>>> tintSources = null;
    private final Map<ResourceKey<PoiType>, Predicate<BlockState>> poiTypes = new LinkedHashMap<>();
    private @Nullable Supplier<Supplier<BuiltInBlockModels.ModelFactory>> modelFactory = null;

    @ApiStatus.Internal
    public BlockBuilder(BaseRegistree<?> registree, String identifier, Function<BlockBehaviour.Properties, TBlock> factory) {
        super(registree, Registries.BLOCK, identifier, Holders::createBlock);

        this.factory = factory;
    }

    public BlockBuilder<TBlock> initialProperties(Supplier<BlockBehaviour.Properties> initialProperties) {
        this.initialProperties = initialProperties;
        return this;
    }

    public BlockBuilder<TBlock> properties(UnaryOperator<BlockBehaviour.Properties> propertiesModifier) {
        this.propertiesModifier = this.propertiesModifier.andThen(propertiesModifier);
        return this;
    }

    public <TCapability, TContext extends @Nullable Object> BlockBuilder<TBlock> capability(BlockCapability<TCapability, TContext> capability, IBlockCapabilityProvider<TCapability, TContext> provider) {
        capabilities.put(capability, provider);
        return this;
    }

    public <TBlockEntity extends BlockEntity> BlockBuilder<TBlock> blockEntity(BlockEntityType.BlockEntitySupplier<TBlockEntity> factory, UnaryOperator<BlockEntityBuilder<TBlockEntity>> modifier) {
        modifier.apply(registree.blockEntity(identifier, factory).validBlock(this::value)).register();
        return this;
    }

    public <TBlockEntity extends BlockEntity> BlockBuilder<TBlock> blockEntity(BlockEntityType.BlockEntitySupplier<TBlockEntity> factory) {
        return blockEntity(factory, UnaryOperator.identity());
    }

    public BlockBuilder<TBlock> blockEntity(Supplier<? extends BlockEntityType<?>> blockEntityType) {
        blockEntityTypes.add(blockEntityType);
        return this;
    }

    @SafeVarargs
    public final BlockBuilder<TBlock> blockEntity(Supplier<? extends BlockEntityType<?>> blockEntityType, Supplier<? extends BlockEntityType<?>>... blockEntityTypes) {
        Collections.addAll(this.blockEntityTypes, blockEntityTypes);
        return blockEntity(blockEntityType);
    }

    public <TItem extends Item> BlockBuilder<TBlock> item(BiFunction<TBlock, Item.Properties, TItem> factory, UnaryOperator<ItemBuilder<TItem>> modifier) {
        modifier.apply(registree.item(identifier, properties -> factory.apply(value(), properties))
                .properties(properties -> properties
                        .useBlockDescriptionPrefix()
                        .requiredFeatures(value().requiredFeatures())
                )
        ).register();

        return this;
    }

    public <TItem extends Item> BlockBuilder<TBlock> item(BiFunction<TBlock, Item.Properties, TItem> factory) {
        return item(factory, UnaryOperator.identity());
    }

    public BlockBuilder<TBlock> item(UnaryOperator<ItemBuilder<BlockItem>> action) {
        return item(BlockItem::new, action);
    }

    public BlockBuilder<TBlock> item() {
        return item(BlockItem::new, UnaryOperator.identity());
    }

    public BlockBuilder<TBlock> clientExtension(Supplier<Supplier<IClientBlockExtensions>> clientExtension) {
        this.clientExtension = clientExtension;
        return this;
    }

    public BlockBuilder<TBlock> tintSources(Supplier<Supplier<List<BlockTintSource>>> tintSources) {
        this.tintSources = tintSources;
        return this;
    }

    public BlockBuilder<TBlock> poiType(ResourceKey<PoiType> poiType, Predicate<BlockState> filter) {
        poiTypes.put(poiType, filter);
        return this;
    }

    public <TProperty extends Comparable<TProperty>> BlockBuilder<TBlock> poiType(ResourceKey<PoiType> poiType, Property<TProperty> property, TProperty value) {
        return poiType(poiType, blockState -> blockState.getValue(property) == value);
    }

    public BlockBuilder<TBlock> poiType(ResourceKey<PoiType> poiType, Property<?> property) {
        return poiType(poiType, blockState -> blockState.hasProperty(property));
    }

    public BlockBuilder<TBlock> poiType(ResourceKey<PoiType> poiType) {
        return poiType(poiType, Predicates.alwaysTrue());
    }

    public BlockBuilder<TBlock> model(Supplier<Supplier<BuiltInBlockModels.ModelFactory>> modelFactory) {
        this.modelFactory = modelFactory;
        return this;
    }

    @Override
    protected TBlock createValue(ResourceKey<Block> registryKey) {
        return propertiesModifier.
                <BlockBehaviour.Properties>compose(properties -> properties.setId(registryKey))
                .andThen(factory)
                .apply(initialProperties.get());
    }

    @Override
    protected void registerEvents() {
        super.registerEvents();

        RegistryEventHelper.registerBlockCapabilities(registree, this::value, capabilities);
        RegistryEventHelper.appendBlockToBlockEntityTypes(registree, this::value, blockEntityTypes);
        RegistryEventHelper.appendBlockToPoiType(registree, this::value, poiTypes);

        if(FMLEnvironment.getDist().isClient()) {
            RegistryClientEventHelper.registerBlockClientExtension(registree, this::value, clientExtension);
            RegistryClientEventHelper.registerBlockTintSources(registree, this::value, tintSources);
            RegistryClientEventHelper.registerBlockModel(registree, this::value, modelFactory);
        }
    }
}
