package dev.apexstudios.registree.registrar;

import dev.apexstudios.registree.Registree;
import dev.apexstudios.registree.builder.EntityTypeBuilder;
import dev.apexstudios.registree.holder.DeferredEntityType;
import java.util.function.Consumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.apache.commons.lang3.function.Consumers;

public class EntityTypeRegistrar extends Registrar<EntityType<?>> {
    public EntityTypeRegistrar(Registree registree) {
        super(registree, Registries.ENTITY_TYPE);
    }

    public <TEntity extends Entity> EntityTypeBuilder<TEntity> builder(String identifier, EntityType.EntityFactory<TEntity> factory, MobCategory category) {
        return new EntityTypeBuilder<>(this, identifier, factory, category);
    }

    public <TEntity extends Entity> DeferredEntityType<TEntity> register(String identifier, EntityType.EntityFactory<TEntity> factory, MobCategory category, Consumer<EntityType.Builder<TEntity>> propertiesModifier) {
        return registerForHolder(identifier, registryName -> {
            var builder = EntityType.Builder.of(factory, category);
            propertiesModifier.accept(builder);
            return builder.build(registryKey(registryName));
        }, DeferredEntityType::createEntityType);
    }

    public <TEntity extends Entity> DeferredEntityType<TEntity> register(String identifier, EntityType.EntityFactory<TEntity> factory, MobCategory category) {
        return register(identifier, factory, category, Consumers.nop());
    }
}
