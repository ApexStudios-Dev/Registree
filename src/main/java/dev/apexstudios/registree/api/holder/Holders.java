package dev.apexstudios.registree.api.holder;

import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.gamerules.GameRule;

public interface Holders {
    // region Generic
    static <TRegistry, TElement extends TRegistry> DeferredHolder<TRegistry, TElement> create(ResourceKey<TRegistry> registryKey) {
        return new DeferredHolder<>(registryKey);
    }

    static <TRegistry, TElement extends TRegistry> DeferredHolder<TRegistry, TElement> create(ResourceKey<? extends Registry<TRegistry>> registryType, Identifier registryName) {
        return create(ResourceKey.create(registryType, registryName));
    }

    private static <TRegistry, TElement extends TRegistry, THolder extends DeferredHolder<TRegistry, TElement>> THolder remap(Holder<?> holder, Function<Identifier, THolder> factory) {
        return switch (holder) {
            case Holder.Reference<?> reference -> factory.apply(reference.key().identifier());
            case net.neoforged.neoforge.registries.DeferredHolder<?, ?> deferred -> factory.apply(deferred.getId());
            case null, default -> throw new IllegalStateException("Unsuported Holder type: " + holder);
        };
    }
    // endregion

    // region Block
    static <TBlock extends Block> DeferredBlock<TBlock> createBlock(ResourceKey<Block> registryKey) {
        return new DeferredBlock<>(registryKey);
    }

    static <TBlock extends Block> DeferredBlock<TBlock> createBlock(Identifier registryName) {
        return createBlock(ResourceKey.create(Registries.BLOCK, registryName));
    }

    static <TBlock extends Block> DeferredBlock<TBlock> createBlock(Holder<?> holder) {
        return remap(holder, Holders::createBlock);
    }
    // endregion

    // region BlockEntityType
    static <TBlockEntity extends BlockEntity> DeferredBlockEntityType<TBlockEntity> createBlockEntityType(ResourceKey<BlockEntityType<?>> registryKey) {
        return new DeferredBlockEntityType<>(registryKey);
    }

    static <TBlockEntity extends BlockEntity> DeferredBlockEntityType<TBlockEntity> createBlockEntityType(Identifier registryName) {
        return createBlockEntityType(ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, registryName));
    }

    static <TBlockEntity extends BlockEntity> DeferredBlockEntityType<TBlockEntity> createBlockEntityType(Holder<?> holder) {
        return remap(holder, Holders::createBlockEntityType);
    }
    // endregion

    // region EntityType
    static <TEntity extends Entity> DeferredEntityType<TEntity> createEntityType(ResourceKey<EntityType<?>> registryKey) {
        return new DeferredEntityType<>(registryKey);
    }

    static <TEntity extends Entity> DeferredEntityType<TEntity> createEntityType(Identifier registryName) {
        return createEntityType(ResourceKey.create(Registries.ENTITY_TYPE, registryName));
    }

    static <TEntity extends Entity> DeferredEntityType<TEntity> createEntityType(Holder<?> holder) {
        return remap(holder, Holders::createEntityType);
    }
    // endregion

    // region GameRule
    static <TType> DeferredGameRule<TType> createGameRule(ResourceKey<GameRule<?>> registryKey) {
        return new DeferredGameRule<>(registryKey);
    }

    static <TType> DeferredGameRule<TType> createGameRule(Identifier registryName) {
        return createGameRule(ResourceKey.create(Registries.GAME_RULE, registryName));
    }

    static <TType> DeferredGameRule<TType> createGameRule(Holder<?> holder) {
        return remap(holder, Holders::createGameRule);
    }
    // endregion

    // region Item
    static <TItem extends Item> DeferredItem<TItem> createItem(ResourceKey<Item> registryKey) {
        return new DeferredItem<>(registryKey);
    }

    static <TItem extends Item> DeferredItem<TItem> createItem(Identifier registryName) {
        return createItem(ResourceKey.create(Registries.ITEM, registryName));
    }

    static <TItem extends Item> DeferredItem<TItem> createItem(Holder<?> holder) {
        return remap(holder, Holders::createItem);
    }
    // endregion

    // region MenuType
    static <TMenu extends AbstractContainerMenu> DeferredMenuType<TMenu> createMenuType(ResourceKey<MenuType<?>> registryKey) {
        return new DeferredMenuType<>(registryKey);
    }

    static <TMenu extends AbstractContainerMenu> DeferredMenuType<TMenu> createMenuType(Identifier registryName) {
        return createMenuType(ResourceKey.create(Registries.MENU, registryName));
    }

    static <TMenu extends AbstractContainerMenu> DeferredMenuType<TMenu> createMenuType(Holder<?> holder) {
        return remap(holder, Holders::createMenuType);
    }
    // endregion
}
