package dev.apexstudios.testmod.xplat;

import dev.apexstudios.registree.xplat.Registree;
import dev.apexstudios.registree.xplat.holder.DeferredItem;
import net.minecraft.world.item.Item;

public interface TestModXplat {
    String ID = "testmod";
    Registree REGISTREE = Registree.create(ID);
    DeferredItem<Item> TEST_ITEM = REGISTREE.registerSimpleItem("test_item");

    default void init() {

    }
}
