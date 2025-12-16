package dev.apexstudios.registree.api.registrar;

import dev.apexstudios.registree.api.builder.IEntityTypeBuilder;
import dev.apexstudios.registree.api.holder.DeferredEntityType;
import dev.apexstudios.registree.core.builder.EntityTypeBuilder;
import java.util.function.Consumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.apache.commons.lang3.function.Consumers;

public interface IEntityTypeRegistrar extends IRegistrar.WithHolder<EntityType<?>, DeferredEntityType<?>> {
    default <TEntity extends Entity> DeferredEntityType<TEntity> registerEntity(String identifier, EntityType.EntityFactory<TEntity> factory, MobCategory category, Consumer<EntityType.Builder<TEntity>> propertiesModifier) {
        return registerForHolder(identifier, registryName -> {
            var builder = EntityType.Builder.of(factory, category);
            propertiesModifier.accept(builder);
            return builder.build(registryKey(registryName));
        });
    }

    default <TEntity extends Entity> DeferredEntityType<TEntity> registerEntity(String identifier, EntityType.EntityFactory<TEntity> factory, MobCategory category) {
        return registerEntity(identifier, factory, category, Consumers.nop());
    }

    default <TEntity extends Entity> IEntityTypeBuilder<TEntity> builder(String identifier, EntityType.EntityFactory<TEntity> factory, MobCategory category) {
        return new EntityTypeBuilder<>(this, identifier, factory, category);
    }
}
