package dev.apexstudios.registree.core.registrar;

import dev.apexstudios.registree.api.IRegistree;
import dev.apexstudios.registree.api.holder.DeferredEntityType;
import dev.apexstudios.registree.api.holder.Holders;
import dev.apexstudios.registree.api.registrar.IEntityTypeRegistrar;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;

public class EntityTypeRegistrar extends Registrar.WithHolder<EntityType<?>, DeferredEntityType<?>> implements IEntityTypeRegistrar {
    public EntityTypeRegistrar(IRegistree registree) {
        super(registree, Registries.ENTITY_TYPE, Holders::createEntityType);
    }
}
