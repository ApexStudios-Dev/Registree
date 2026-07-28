package dev.apexstudios.registree.builder;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import dev.apexstudios.registree.BaseRegistree;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.capabilities.BaseCapability;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.IBlockCapabilityProvider;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.world.poi.ExtendPoiTypesEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCauldronInteractionEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.level.GameRuleChangedEvent;
import net.neoforged.neoforge.mixins.BlockEntityTypeAccessor;
import org.apache.commons.lang3.function.TriConsumer;
import org.jspecify.annotations.Nullable;

public interface RegistryEventHelper {
    static <TCapability extends BaseCapability<?, ?>, TProvider> void registerCapabilities(BaseRegistree<?> registree, Multimap<TCapability, TProvider> capabilities, TriConsumer<RegisterCapabilitiesEvent, TCapability, TProvider> registrar) {
        if(capabilities.isEmpty()) {
            return;
        }

        var immutable = ImmutableMultimap.copyOf(capabilities);
        registree.event(RegisterCapabilitiesEvent.class, event -> immutable.keySet().forEach(cap -> registerCapability(event, immutable, cap, registrar)));
    }

    static <TCapability extends BaseCapability<?, ?>, TProvider> void registerCapability(RegisterCapabilitiesEvent event, Multimap<TCapability, TProvider> capabilities, TCapability capability, TriConsumer<RegisterCapabilitiesEvent, TCapability, TProvider> registrar) {
        var providers = capabilities.get(capability);

        if(providers.isEmpty()) {
            return;
        }

        providers.forEach(provider -> registrar.accept(event, capability, provider));
    }

    static void registerBlockCapabilities(BaseRegistree<?> registree, Supplier<Block> block, Multimap<BlockCapability<?, ?>, IBlockCapabilityProvider<?, ?>> capabilities) {
        registerCapabilities(registree, capabilities, (event, capability, provider) -> event.registerBlock((BlockCapability) capability, provider, block.get()));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static <TBlockEntity extends BlockEntity> void registerBlockEntityCapabilities(BaseRegistree<?> registree, Supplier<BlockEntityType<TBlockEntity>> blockEntityType, Multimap<BlockCapability<?, ?>, ICapabilityProvider<TBlockEntity, ?, ?>> capabilities) {
        registerCapabilities(registree, capabilities, (event, capability, provider) -> event.registerBlockEntity((BlockCapability) capability, blockEntityType.get(), provider));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static <TEntity extends Entity> void registerEntityCapabilities(BaseRegistree<?> registree, Supplier<EntityType<TEntity>> entityType, Multimap<EntityCapability<?, ?>, ICapabilityProvider<TEntity, ?, ?>> capabilities) {
        registerCapabilities(registree, capabilities, (event, capability, provider) -> event.registerEntity((EntityCapability) capability, entityType.get(), provider));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static void registerItemCapabilities(BaseRegistree<?> registree, ItemLike item, Multimap<ItemCapability<?, ?>, ICapabilityProvider<ItemStack, ?, ?>> capabilities) {
        registerCapabilities(registree, capabilities, (event, capability, provider) -> event.registerItem((ItemCapability) capability, provider, item.asItem()));
    }

    static void appendBlockToBlockEntityTypes(BaseRegistree<?> registree, Supplier<Block> block, List<Supplier<? extends BlockEntityType<?>>> blockEntityTypes) {
        if(blockEntityTypes.isEmpty()) {
            return;
        }

        var immutable = List.copyOf(blockEntityTypes);
        registree.event(BlockEntityTypeAddBlocksEvent.class, event -> immutable.forEach(blockEntityType -> appendBlockToBlockEntityType(block.get(), blockEntityType.get())));
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void appendBlockToBlockEntityType(Block block, BlockEntityType<?> blockEntityType) {
        var validBlocks = new HashSet<>(blockEntityType.getValidBlocks());
        validBlocks.add(block);
        ((BlockEntityTypeAccessor) blockEntityType).neoforge$setValidBlocks(validBlocks);
    }

    static void appendBlockToPoiType(BaseRegistree<?> registree, Supplier<Block> block, Map<ResourceKey<PoiType>, Predicate<BlockState>> poiTypes) {
         if(poiTypes.isEmpty()) {
            return;
        }

        var immutable = Map.copyOf(poiTypes);

        registree.event(ExtendPoiTypesEvent.class, event -> {
            var definition = block.get().getStateDefinition();

            immutable.forEach((poiType, filter) -> event.addStatesToPoi(poiType, definition
                    .getPossibleStates()
                    .stream()
                    .filter(filter)
                    .collect(Collectors.toSet())
            ));
        });
    }

    static <TEntity extends Entity> void registerEntitySpawnPlacement(BaseRegistree<?> registree, Supplier<EntityType<TEntity>> entityType, @Nullable SpawnPlacement<TEntity> spawnPlacement) {
        if(spawnPlacement == null) {
            return;
        }

        registree.event(RegisterSpawnPlacementsEvent.class, event -> event.register(entityType.get(), spawnPlacement.type, spawnPlacement.heightmap, spawnPlacement.predicate, RegisterSpawnPlacementsEvent.Operation.REPLACE));
    }

    static <TEntity extends LivingEntity> void registerEntityAttributes(BaseRegistree<?> registree, Supplier<EntityType<TEntity>> entityType, @Nullable Supplier<@Nullable AttributeSupplier> attributesFactory) {
        if(attributesFactory == null) {
            return;
        }

        registree.event(EntityAttributeCreationEvent.class, event -> {
            var attributes = attributesFactory.get();

            if(attributes != null) {
                event.put(entityType.get(), attributes);
            }
        });
    }

    static <TRuleType> void registerGameRuleChanged(Supplier<GameRule<TRuleType>> gameRule, @Nullable GameRuleListener<TRuleType> listener) {
        if(listener == null) {
            return;
        }

        NeoForge.EVENT_BUS.addListener(GameRuleChangedEvent.class, event -> event.runIfMatching(gameRule.get(), newValue -> listener.accept(event.getServer(), gameRule.get(), newValue)));
    }

    static <TItem extends Item> void appendItemToCreativeModeTabs(BaseRegistree<?> registree, Supplier<TItem> item, Map<ResourceKey<CreativeModeTab>, CreativeModeTabAppender<TItem>> creativeModeTabs) {
        if(creativeModeTabs.isEmpty()) {
            return;
        }

        var immutable = Map.copyOf(creativeModeTabs);

        registree.event(BuildCreativeModeTabContentsEvent.class, event -> {
            var generator = immutable.get(event.getTabKey());

            if(generator != null) {
                generator.accept(item.get(), event.getParameters(), event);
            }
        });
    }

    static void registerItemCauldronInteractions(BaseRegistree<?> registree, ItemLike item, Map<Identifier, CauldronInteraction> interactions) {
        if(interactions.isEmpty()) {
            return;
        }

        var immutable = Map.copyOf(interactions);
        registree.event(RegisterCauldronInteractionEvent.Interaction.class, event -> immutable.forEach((dispatcherId, interaction) -> event.register(dispatcherId, item.asItem(), interaction)));
    }

    static void registerItemGlobalCauldronInteractions(BaseRegistree<?> registree, ItemLike item, List<CauldronInteraction> globalInteractions) {
        if(globalInteractions.isEmpty()) {
            return;
        }

        var immutable = List.copyOf(globalInteractions);
        registree.event(RegisterCauldronInteractionEvent.Interaction.class, event -> immutable.forEach(interaction -> event.registerToAll(item.asItem(), interaction)));
    }

    static void registerRecipeTypeSyncHandler(Supplier<RecipeType<?>> recipeType, @Nullable RecipeSyncHandler syncHandler) {
        if(syncHandler == null) {
            return;
        }

        NeoForge.EVENT_BUS.addListener(OnDatapackSyncEvent.class, event -> event.sendRecipes(recipeType.get()));
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, event -> syncHandler.cleanup.run());

        NeoForge.EVENT_BUS.addListener(RecipesReceivedEvent.class, event -> {
            syncHandler.cleanup.run();
            syncHandler.handler.accept(event.getRecipeMap());
        });
    }

    record SpawnPlacement<TEntity extends Entity>(
            Heightmap.Types heightmap,
            SpawnPlacementType type,
            SpawnPlacements.SpawnPredicate<TEntity> predicate
    ) { }

    @FunctionalInterface
    interface GameRuleListener<TRuleType> {
        void accept(MinecraftServer server, GameRule<TRuleType> gameRule, TRuleType newValue);

        default GameRuleListener<TRuleType> andThen(@Nullable GameRuleListener<TRuleType> after) {
            if(after == null) {
                return this;
            }

            return (server, gameRule, newValue) -> {
                accept(server, gameRule, newValue);
                after.accept(server, gameRule, newValue);
            };
        }
    }

    @FunctionalInterface
    interface CreativeModeTabAppender<TItem extends Item> {
        void accept(TItem item, CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output);
    }

    record RecipeSyncHandler(Consumer<RecipeMap> handler, Runnable cleanup) {
        public RecipeSyncHandler merge(Consumer<RecipeMap> handler, Runnable cleanup) {
            return new RecipeSyncHandler(this.handler.andThen(handler), () -> {
                this.cleanup.run();
                cleanup.run();
            });
        }
    }
}
