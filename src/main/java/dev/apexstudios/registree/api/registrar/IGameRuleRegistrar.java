package dev.apexstudios.registree.api.registrar;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;
import dev.apexstudios.registree.api.builder.IGameRuleBuilder;
import dev.apexstudios.registree.api.holder.DeferredGameRule;
import dev.apexstudios.registree.core.builder.GameRuleBuilder;
import java.util.function.ToIntFunction;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.minecraft.world.level.gamerules.GameRules;

public interface IGameRuleRegistrar extends IRegistrar<GameRule<?>, DeferredGameRule<?>> {
    GameRules.VisitorCaller<?> EMPTY_VISITOR = (visitor, gameRule) -> { };

    default <TType> DeferredGameRule<TType> registerGameRule(String identifier, GameRuleCategory category, GameRuleType type, ArgumentType<TType> argumentType, Codec<TType> codec, TType defaultValue, FeatureFlagSet requiredFeatures, GameRules.VisitorCaller<TType> visitor, ToIntFunction<TType> commandResult) {
        return registerForHolder(identifier, registryName -> new GameRule<>(category, type, argumentType, visitor, codec, commandResult, defaultValue, requiredFeatures));
    }

    default <TType> DeferredGameRule<TType> registerGameRule(String identifier, GameRuleCategory category, GameRuleType type, ArgumentType<TType> argumentType, Codec<TType> codec, TType defaultValue, FeatureFlagSet requiredFeatures, ToIntFunction<TType> commandResult) {
        return registerGameRule(identifier, category, type, argumentType, codec, defaultValue, requiredFeatures, emptyVisitor(), commandResult);
    }

    default <TType> DeferredGameRule<TType> registerGameRule(String identifier, GameRuleCategory category, GameRuleType type, ArgumentType<TType> argumentType, Codec<TType> codec, TType defaultValue, GameRules.VisitorCaller<TType> visitor, ToIntFunction<TType> commandResult) {
        return registerGameRule(identifier, category, type, argumentType, codec, defaultValue, FeatureFlags.DEFAULT_FLAGS, visitor, commandResult);
    }

    default <TType> DeferredGameRule<TType> registerGameRule(String identifier, GameRuleCategory category, GameRuleType type, ArgumentType<TType> argumentType, Codec<TType> codec, TType defaultValue, ToIntFunction<TType> commandResult) {
        return registerGameRule(identifier, category, type, argumentType, codec, defaultValue, FeatureFlags.DEFAULT_FLAGS, emptyVisitor(), commandResult);
    }

    default DeferredGameRule<Integer> registerIntegerGameRule(String identifier, GameRuleCategory category, int defaultValue, int min, int max, FeatureFlagSet requiredFeatures) {
        return registerGameRule(identifier, category, GameRuleType.INT, IntegerArgumentType.integer(min, max), Codec.intRange(min, max), defaultValue, requiredFeatures, GameRuleTypeVisitor::visitInteger, value -> value);
    }

    default DeferredGameRule<Integer> registerIntegerGameRule(String identifier, GameRuleCategory category, int defaultValue, int min, int max) {
        return registerIntegerGameRule(identifier, category, defaultValue, min, max, FeatureFlags.DEFAULT_FLAGS);
    }

    default DeferredGameRule<Integer> registerIntegerGameRule(String identifier, GameRuleCategory category, int defaultValue, int min, FeatureFlagSet requiredFeatures) {
        return registerIntegerGameRule(identifier, category, defaultValue, min, Integer.MAX_VALUE, requiredFeatures);
    }

    default DeferredGameRule<Integer> registerIntegerGameRule(String identifier, GameRuleCategory category, int defaultValue, int min) {
        return registerIntegerGameRule(identifier, category, defaultValue, min, Integer.MAX_VALUE, FeatureFlags.DEFAULT_FLAGS);
    }

    default DeferredGameRule<Integer> registerIntegerGameRule(String identifier, GameRuleCategory category, int defaultValue, FeatureFlagSet requiredFeatures) {
        return registerIntegerGameRule(identifier, category, defaultValue, 0, Integer.MAX_VALUE, requiredFeatures);
    }

    default DeferredGameRule<Integer> registerIntegerGameRule(String identifier, GameRuleCategory category, int defaultValue) {
        return registerIntegerGameRule(identifier, category, defaultValue, 0, Integer.MAX_VALUE, FeatureFlags.DEFAULT_FLAGS);
    }

    default DeferredGameRule<Boolean> registerBooleanGameRule(String identifier, GameRuleCategory category, boolean defaultValue, FeatureFlagSet requiredFeatures) {
        return registerGameRule(identifier, category, GameRuleType.BOOL, BoolArgumentType.bool(), Codec.BOOL, defaultValue, requiredFeatures, GameRuleTypeVisitor::visitBoolean, value -> value ? Command.SINGLE_SUCCESS : 0);
    }

    default DeferredGameRule<Boolean> registerBooleanGameRule(String identifier, GameRuleCategory category, boolean defaultValue) {
        return registerBooleanGameRule(identifier, category, defaultValue, FeatureFlags.DEFAULT_FLAGS);
    }

    default DeferredGameRule<Boolean> registerBooleanGameRule(String identifier, GameRuleCategory category, FeatureFlagSet requiredFeatures) {
        return registerBooleanGameRule(identifier, category, false, requiredFeatures);
    }

    default DeferredGameRule<Boolean> registerBooleanGameRule(String identifier, GameRuleCategory category) {
        return registerBooleanGameRule(identifier, category, false, FeatureFlags.DEFAULT_FLAGS);
    }

    GameRuleCategory registerGameRuleCategory(String identifier);

    default GameRuleType registerGameRuleType(String identifier) {
        // note that this assumes you have a valid enum_extensions.json defined
        // with the appropriate extension info for your game rule type
        // this looks for a game rule type matching the follwing id '<namespace>:<identifier>'
        return GameRuleType.valueOf(registryId(identifier));
    }

    default IGameRuleBuilder.Vanilla<Integer> integerBuilder(String identifier, GameRuleCategory category, int defaultValue, int min, int max) {
        return new GameRuleBuilder.Vanilla<>(this, identifier, category, GameRuleType.INT, IntegerArgumentType.integer(min, max), Codec.intRange(min, max), defaultValue, GameRuleTypeVisitor::visitInteger, value -> value);
    }

    default IGameRuleBuilder.Vanilla<Integer> integerBuilder(String identifier, GameRuleCategory category, int defaultValue, int min) {
        return integerBuilder(identifier, category, defaultValue, min, Integer.MAX_VALUE);
    }

    default IGameRuleBuilder.Vanilla<Integer> integerBuilder(String identifier, GameRuleCategory category, int defaultValue) {
        return integerBuilder(identifier, category, defaultValue, 0, Integer.MAX_VALUE);
    }

    default IGameRuleBuilder.Vanilla<Boolean> booleanBuilder(String identifier, GameRuleCategory category, boolean defaultValue) {
        return new GameRuleBuilder.Vanilla<>(this, identifier, category, GameRuleType.BOOL, BoolArgumentType.bool(), Codec.BOOL, defaultValue, GameRuleTypeVisitor::visitBoolean, value -> value ? Command.SINGLE_SUCCESS : 0);
    }

    default IGameRuleBuilder.Vanilla<Boolean> booleanBuilder(String identifier, GameRuleCategory category) {
        return booleanBuilder(identifier, category, false);
    }

    default <TType> IGameRuleBuilder.Modded<TType> builder(String identifier, GameRuleCategory category, GameRuleType type, ArgumentType<TType> argumentType, Codec<TType> codec, TType defaultValue) {
        return new GameRuleBuilder.Modded<>(this, identifier, category, type, argumentType, codec, defaultValue);
    }

    @SuppressWarnings("unchecked")
    static <TType> GameRules.VisitorCaller<TType> emptyVisitor() {
        return (GameRules.VisitorCaller<TType>) EMPTY_VISITOR;
    }
}
