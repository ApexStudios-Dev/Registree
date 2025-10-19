package dev.apexstudios.registree.api.holder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class DeferredItemLike<TRegistry extends ItemLike, TElement extends TRegistry> extends ApexDeferredHolder<TRegistry, TElement> implements ItemLike {
    public DeferredItemLike(ResourceKey<TRegistry> registryKey) {
        super(registryKey);
    }

    @Override
    public Item asItem() {
        return value().asItem();
    }

    public ItemStack toStack(int count) {
        var stack = asItem().getDefaultInstance();
        stack.setCount(count);
        return stack;
    }

    public ItemStack toStack() {
        return toStack(1);
    }
}
