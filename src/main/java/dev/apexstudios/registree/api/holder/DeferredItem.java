package dev.apexstudios.registree.api.holder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class DeferredItem<TItem extends Item> extends DeferredItemLike<Item, TItem> {
    public DeferredItem(ResourceKey<Item> registryKey) {
        super(registryKey);
    }

    @Override
    public Item asItem() {
        return value();
    }

    public boolean is(ItemStack stack) {
        return stack.is(this);
    }
}
