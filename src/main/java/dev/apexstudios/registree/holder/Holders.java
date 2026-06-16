package dev.apexstudios.registree.holder;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public interface Holders {
    // region Generic
    static <TRegistry, TValue extends TRegistry> DeferredHolder<TRegistry, TValue> create(ResourceKey<TRegistry> registryKey) {
        return DeferredHolder.create(registryKey);
    }

    static <TRegistry, TValue extends TRegistry> DeferredHolder<TRegistry, TValue> create(ResourceKey<? extends Registry<TRegistry>> registryType, Identifier registryName) {
        return create(ResourceKey.create(registryType, registryName));
    }

    static <TRegistry, TValue extends TRegistry> DeferredHolder<TRegistry, TValue> create(ResourceKey<? extends Registry<TRegistry>> registryType, Holder.Reference<?> parent) {
        return create(registryType, parent.key().identifier());
    }

    static <TRegistry, TValue extends TRegistry> DeferredHolder<TRegistry, TValue> create(ResourceKey<? extends Registry<TRegistry>> registryType, DeferredHolder<?, ?> parent) {
        return create(registryType, parent.getId());
    }
    // endregion

    // region Block
    static <TBlock extends Block> DeferredBlock<TBlock> createBlock(ResourceKey<Block> registryKey) {
        return DeferredBlock.createBlock(registryKey);
    }

    static <TBlock extends Block> DeferredBlock<TBlock> createBlock(Identifier registryName) {
        return createBlock(ResourceKey.create(Registries.BLOCK, registryName));
    }

    static <TBlock extends Block> DeferredBlock<TBlock> createBlock(Holder.Reference<?> parent) {
        return createBlock(parent.key().identifier());
    }

    static <TBlock extends Block> DeferredBlock<TBlock> createBlock(DeferredHolder<?, ?> parent) {
        return createBlock(parent.getId());
    }
    // endregion

    // region Item
    static <TItem extends Item> DeferredItem<TItem> createItem(ResourceKey<Item> registryKey) {
        return DeferredItem.createItem(registryKey);
    }

    static <TItem extends Item> DeferredItem<TItem> createItem(Identifier registryName) {
        return createItem(ResourceKey.create(Registries.ITEM, registryName));
    }

    static <TItem extends Item> DeferredItem<TItem> createItem(Holder.Reference<?> parent) {
        return createItem(parent.key().identifier());
    }

    static <TItem extends Item> DeferredItem<TItem> createItem(DeferredHolder<?, ?> parent) {
        return createItem(parent.getId());
    }
    // endregion

    // region BlockEntity
    static <TBlockEntity extends BlockEntity> DeferredBlockEntity<TBlockEntity> createBlockEntity(ResourceKey<BlockEntityType<?>> registryKey) {
        return new DeferredBlockEntity<>(registryKey);
    }

    static <TBlockEntity extends BlockEntity> DeferredBlockEntity<TBlockEntity> createBlockEntity(Identifier registryName) {
        return createBlockEntity(ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, registryName));
    }

    static <TBlockEntity extends BlockEntity> DeferredBlockEntity<TBlockEntity> createBlockEntity(Holder.Reference<?> parent) {
        return createBlockEntity(parent.key().identifier());
    }

    static <TBlockEntity extends BlockEntity> DeferredBlockEntity<TBlockEntity> createBlockEntity(DeferredHolder<?, ?> parent) {
        return createBlockEntity(parent.getId());
    }
    // endregion

    // region Entity
    static <TEntity extends Entity> DeferredEntity<TEntity> createEntity(ResourceKey<EntityType<?>> registryKey) {
        return new DeferredEntity<>(registryKey);
    }

    static <TEntity extends Entity> DeferredEntity<TEntity> createEntity(Identifier registryName) {
        return createEntity(ResourceKey.create(Registries.ENTITY_TYPE, registryName));
    }

    static <TEntity extends Entity> DeferredEntity<TEntity> createEntity(Holder.Reference<?> parent) {
        return createEntity(parent.key().identifier());
    }

    static <TEntity extends Entity> DeferredEntity<TEntity> createEntity(DeferredHolder<?, ?> parent) {
        return createEntity(parent.getId());
    }
    // endregion

    // region GameRule
    static <TRuleType> DeferredGameRule<TRuleType> createGameRule(ResourceKey<GameRule<?>> registryKey) {
        return new DeferredGameRule<>(registryKey);
    }

    static <TRuleType> DeferredGameRule<TRuleType> createGameRule(Identifier registryName) {
        return createGameRule(ResourceKey.create(Registries.GAME_RULE, registryName));
    }

    static <TRuleType> DeferredGameRule<TRuleType> createGameRule(Holder.Reference<?> parent) {
        return createGameRule(parent.key().identifier());
    }

    static <TRuleType> DeferredGameRule<TRuleType> createGameRule(DeferredHolder<?, ?> parent) {
        return createGameRule(parent.getId());
    }
    // endregion

    // region FluidType
    static <TFluidType extends FluidType> DeferredFluidType<TFluidType> createFluidType(ResourceKey<FluidType> registryKey) {
        return new DeferredFluidType<>(registryKey);
    }

    static <TFluidType extends FluidType> DeferredFluidType<TFluidType> createFluidType(Identifier registryName) {
        return createFluidType(ResourceKey.create(NeoForgeRegistries.Keys.FLUID_TYPES, registryName));
    }

    static <TFluidType extends FluidType> DeferredFluidType<TFluidType> createFluidType(Holder.Reference<?> parent) {
        return createFluidType(parent.key().identifier());
    }

    static <TFluidType extends FluidType> DeferredFluidType<TFluidType> createFluidType(DeferredHolder<?, ?> parent) {
        return createFluidType(parent.getId());
    }
    // endregion

    // region Fluid
    static <TFluid extends Fluid> DeferredFluid<TFluid> createFluid(ResourceKey<Fluid> registryKey) {
        return new DeferredFluid<>(registryKey);
    }

    static <TFluid extends Fluid> DeferredFluid<TFluid> createFluid(Identifier registryName) {
        return createFluid(ResourceKey.create(Registries.FLUID, registryName));
    }

    static <TFluid extends Fluid> DeferredFluid<TFluid> createFluid(Holder.Reference<?> parent) {
        return createFluid(parent.key().identifier());
    }

    static <TFluid extends Fluid> DeferredFluid<TFluid> createFluid(DeferredHolder<?, ?> parent) {
        return createFluid(parent.getId());
    }
    // endregion

    // region RecipeSerializer
    static <TRecipe extends Recipe<?>> DeferredRecipeSerializer<TRecipe> createRecipeSerializer(ResourceKey<RecipeSerializer<?>> registryKey) {
        return new DeferredRecipeSerializer<>(registryKey);
    }

    static <TRecipe extends Recipe<?>> DeferredRecipeSerializer<TRecipe> createRecipeSerializer(Identifier registryName) {
        return createRecipeSerializer(ResourceKey.create(Registries.RECIPE_SERIALIZER, registryName));
    }

    static <TRecipe extends Recipe<?>> DeferredRecipeSerializer<TRecipe> createRecipeSerializer(Holder.Reference<?> parent) {
        return createRecipeSerializer(parent.key().identifier());
    }

    static <TRecipe extends Recipe<?>> DeferredRecipeSerializer<TRecipe> createRecipeSerializer(DeferredHolder<?, ?> parent) {
        return createRecipeSerializer(parent.getId());
    }
    // endregion

    // region DataComponent
    static <TType> DeferredDataComponent<TType> createDataComponent(ResourceKey<DataComponentType<?>> registryKey) {
        return new DeferredDataComponent<>(registryKey);
    }

    static <TType> DeferredDataComponent<TType> createDataComponent(Identifier registryName) {
        return createDataComponent(ResourceKey.create(Registries.DATA_COMPONENT_TYPE, registryName));
    }

    static <TType> DeferredDataComponent<TType> createDataComponent(Holder.Reference<?> parent) {
        return createDataComponent(parent.key().identifier());
    }

    static <TType> DeferredDataComponent<TType> createDataComponent(DeferredHolder<?, ?> parent) {
        return createDataComponent(parent.getId());
    }
    // endregion

    // region Menu
    static <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> createMenu(ResourceKey<MenuType<?>> registryKey) {
        return new DeferredMenu<>(registryKey);
    }

    static <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> createMenu(Identifier registryName) {
        return createMenu(ResourceKey.create(Registries.MENU, registryName));
    }

    static <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> createMenu(Holder.Reference<?> parent) {
        return createMenu(parent.key().identifier());
    }

    static <TMenu extends AbstractContainerMenu> DeferredMenu<TMenu> createMenu(DeferredHolder<?, ?> parent) {
        return createMenu(parent.getId());
    }
    // endregion

    // region RecipeType
    static <TRecipe extends Recipe<?>> DeferredRecipeType<TRecipe> createRecipeType(ResourceKey<RecipeType<?>> registryKey) {
        return new DeferredRecipeType<>(registryKey);
    }

    static <TRecipe extends Recipe<?>> DeferredRecipeType<TRecipe> createRecipeType(Identifier registryName) {
        return createRecipeType(ResourceKey.create(Registries.RECIPE_TYPE, registryName));
    }

    static <TRecipe extends Recipe<?>> DeferredRecipeType<TRecipe> createRecipeType(Holder.Reference<?> parent) {
        return createRecipeType(parent.key().identifier());
    }

    static <TRecipe extends Recipe<?>> DeferredRecipeType<TRecipe> createRecipeType(DeferredHolder<?, ?> parent) {
        return createRecipeType(parent.getId());
    }
    // endregion

    // region RecipeBookCategory
    static DeferredRecipeBookCategory createRecipeBookCategory(ResourceKey<RecipeBookCategory> registryKey) {
        return new DeferredRecipeBookCategory(registryKey);
    }

    static DeferredRecipeBookCategory createRecipeBookCategory(Identifier registryName) {
        return createRecipeBookCategory(ResourceKey.create(Registries.RECIPE_BOOK_CATEGORY, registryName));
    }

    static DeferredRecipeBookCategory createRecipeBookCategory(Holder.Reference<?> parent) {
        return createRecipeBookCategory(parent.key().identifier());
    }

    static DeferredRecipeBookCategory createRecipeBookCategory(DeferredHolder<?, ?> parent) {
        return createRecipeBookCategory(parent.getId());
    }
    // endregion

    // region Particle
    static <TParticleType extends ParticleType<TOptions>, TOptions extends ParticleOptions> DeferredParticle<TParticleType, TOptions> createParticle(ResourceKey<ParticleType<?>> registryKey) {
        return new DeferredParticle<>(registryKey);
    }

    static <TParticleType extends ParticleType<TOptions>, TOptions extends ParticleOptions> DeferredParticle<TParticleType, TOptions> createParticle(Identifier registryName) {
        return createParticle(ResourceKey.create(Registries.PARTICLE_TYPE, registryName));
    }

    static <TParticleType extends ParticleType<TOptions>, TOptions extends ParticleOptions> DeferredParticle<TParticleType, TOptions> createParticle(Holder.Reference<?> parent) {
        return createParticle(parent.key().identifier());
    }

    static <TParticleType extends ParticleType<TOptions>, TOptions extends ParticleOptions> DeferredParticle<TParticleType, TOptions> createParticle(DeferredHolder<?, ?> parent) {
        return createParticle(parent.getId());
    }
    // endregion
}
