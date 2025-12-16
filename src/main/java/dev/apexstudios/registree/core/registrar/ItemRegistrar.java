package dev.apexstudios.registree.core.registrar;

import dev.apexstudios.registree.api.IRegistree;
import dev.apexstudios.registree.api.holder.DeferredItem;
import dev.apexstudios.registree.api.holder.Holders;
import dev.apexstudios.registree.api.registrar.IItemRegistrar;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

public class ItemRegistrar extends Registrar<Item, DeferredItem<?>> implements IItemRegistrar {
    public ItemRegistrar(IRegistree registree) {
        super(registree, Registries.ITEM, Holders::createItem);
    }
}
