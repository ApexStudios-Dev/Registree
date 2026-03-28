package dev.apexstudios.registree.xplat.holder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class DeferredItem<TItem extends Item> extends DeferredHolder<Item, TItem> {
    public DeferredItem(ResourceKey<Item> registryKey) {
        super(registryKey);
    }
}
