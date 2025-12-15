package dev.apexstudios.registree.core.registrar;

import dev.apexstudios.registree.api.IRegistree;
import dev.apexstudios.registree.api.registrar.IEntityTypeRegistrar;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;

public class EntityTypeRegistrar extends Registrar<EntityType<?>> implements IEntityTypeRegistrar {
    public EntityTypeRegistrar(IRegistree registree) {
        super(registree, Registries.ENTITY_TYPE);
    }
}
