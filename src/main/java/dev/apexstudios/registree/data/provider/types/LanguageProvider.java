package dev.apexstudios.registree.data.provider.types;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.apexstudios.registree.registrar.CreativeModeTabRegistrar;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gamerules.GameRule;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.extensions.ILevelExtension;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface LanguageProvider {
    @CanIgnoreReturnValue
    LanguageProvider add(String key, String value);

    @CanIgnoreReturnValue
    default LanguageProvider add(String key, String value, String descriptionValue) {
        return add(key, value).add(key + ".description", descriptionValue);
    }

    @CanIgnoreReturnValue
    default LanguageProvider addBlock(Supplier<? extends Block> key, String name) {
        return add(key.get(), name);
    }

    @CanIgnoreReturnValue
    default LanguageProvider add(Block key, String name) {
        return add(key.getDescriptionId(), name);
    }

    @CanIgnoreReturnValue
    default LanguageProvider addItem(Supplier<? extends Item> key, String name) {
        return add(key.get(), name);
    }

    @CanIgnoreReturnValue
    default LanguageProvider add(Item key, String name) {
        return add(key.getDescriptionId(), name);
    }

    @CanIgnoreReturnValue
    default LanguageProvider addEntityType(Supplier<? extends EntityType<?>> key, String name) {
        return add(key.get(), name);
    }

    @CanIgnoreReturnValue
    default LanguageProvider add(EntityType<?> key, String name) {
        return add(key.getDescriptionId(), name);
    }

    @CanIgnoreReturnValue
    default LanguageProvider addTag(Supplier<? extends TagKey<?>> key, String name) {
        return add(key.get(), name);
    }

    @CanIgnoreReturnValue
    default LanguageProvider add(TagKey<?> tagKey, String name) {
        return add(Tags.getTagTranslationKey(tagKey), name);
    }

    @CanIgnoreReturnValue
    default LanguageProvider add(ResourceKey<?> registyrKey, String translationPrefix, String value) {
        return add(registyrKey.identifier().toLanguageKey(translationPrefix), value);
    }

    @CanIgnoreReturnValue
    default LanguageProvider addDimension(ResourceKey<Level> dimension, String value) {
        return add(dimension, ILevelExtension.TRANSLATION_PREFIX, value);
    }

    @CanIgnoreReturnValue
    default LanguageProvider addCreativeModeTab(ResourceKey<CreativeModeTab> creativeModeTab, String value) {
        return add(CreativeModeTabRegistrar.descriptionId(creativeModeTab), value);
    }

    @CanIgnoreReturnValue
    default LanguageProvider add(GameRule<?> gameRule, String value, String descriptionValue) {
        return add(gameRule.getDescriptionId(), value, descriptionValue);
    }

    @CanIgnoreReturnValue
    default LanguageProvider addGameRule(Supplier<? extends GameRule<?>> gameRule, String value, String descriptionValue) {
        return add(gameRule.get(), value, descriptionValue);
    }
}
