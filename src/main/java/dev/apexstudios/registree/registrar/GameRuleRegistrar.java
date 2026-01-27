package dev.apexstudios.registree.registrar;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;
import dev.apexstudios.registree.Registree;
import dev.apexstudios.registree.holder.DeferredGameRule;
import java.util.function.ToIntFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.neoforge.event.RegisterGameRuleCategoryEvent;

public class GameRuleRegistrar extends Registrar<GameRule<?>> {
    private static final GameRules.VisitorCaller<?> EMPTY_CALLER = (visitor, gameRule) -> { };
    private static final ToIntFunction<?> ALWAYS_SUCCESS = value -> Command.SINGLE_SUCCESS;

    public GameRuleRegistrar(Registree registree) {
        super(registree, Registries.GAME_RULE);
    }

    public <TValue> DeferredGameRule<TValue> register(String identifier, GameRuleCategory category, GameRuleType type, ArgumentType<TValue> argumentType, Codec<TValue> codec, TValue defaultValue, FeatureFlagSet requiredFeatures, GameRules.VisitorCaller<TValue> visitorCaller, ToIntFunction<TValue> commandResult) {
        return registerForHolder(identifier, () -> new GameRule<>(category, type, argumentType, visitorCaller, codec, commandResult, defaultValue, requiredFeatures), DeferredGameRule::createGameRule);
    }

    public <TValue> DeferredGameRule<TValue> register(String identifier, GameRuleCategory category, GameRuleType type, ArgumentType<TValue> argumentType, Codec<TValue> codec, TValue defaultValue, GameRules.VisitorCaller<TValue> visitorCaller, ToIntFunction<TValue> commandResult) {
        return register(identifier, category, type, argumentType, codec, defaultValue, FeatureFlags.DEFAULT_FLAGS, visitorCaller, commandResult);
    }

    public <TValue> DeferredGameRule<TValue> register(String identifier, GameRuleCategory category, GameRuleType type, ArgumentType<TValue> argumentType, Codec<TValue> codec, TValue defaultValue, FeatureFlagSet requiredFeatures, ToIntFunction<TValue> commandResult) {
        return register(identifier, category, type, argumentType, codec, defaultValue, requiredFeatures, emptyVistor(), commandResult);
    }

    public <TValue> DeferredGameRule<TValue> register(String identifier, GameRuleCategory category, GameRuleType type, ArgumentType<TValue> argumentType, Codec<TValue> codec, TValue defaultValue, FeatureFlagSet requiredFeatures, GameRules.VisitorCaller<TValue> visitorCaller) {
        return register(identifier, category, type, argumentType, codec, defaultValue, requiredFeatures, visitorCaller, alwaysSuccess());
    }

    public <TValue> DeferredGameRule<TValue> register(String identifier, GameRuleCategory category, GameRuleType type, ArgumentType<TValue> argumentType, Codec<TValue> codec, TValue defaultValue, FeatureFlagSet requiredFeatures) {
        return register(identifier, category, type, argumentType, codec, defaultValue, requiredFeatures, emptyVistor(), alwaysSuccess());
    }

    public <TValue> DeferredGameRule<TValue> register(String identifier, GameRuleCategory category, GameRuleType type, ArgumentType<TValue> argumentType, Codec<TValue> codec, TValue defaultValue, GameRules.VisitorCaller<TValue> visitorCaller) {
        return register(identifier, category, type, argumentType, codec, defaultValue, FeatureFlags.DEFAULT_FLAGS, visitorCaller, alwaysSuccess());
    }

    public <TValue> DeferredGameRule<TValue> register(String identifier, GameRuleCategory category, GameRuleType type, ArgumentType<TValue> argumentType, Codec<TValue> codec, TValue defaultValue, ToIntFunction<TValue> commandResult) {
        return register(identifier, category, type, argumentType, codec, defaultValue, FeatureFlags.DEFAULT_FLAGS, emptyVistor(), commandResult);
    }

    public <TValue> DeferredGameRule<TValue> register(String identifier, GameRuleCategory category, GameRuleType type, ArgumentType<TValue> argumentType, Codec<TValue> codec, TValue defaultValue) {
        return register(identifier, category, type, argumentType, codec, defaultValue, FeatureFlags.DEFAULT_FLAGS, emptyVistor(), alwaysSuccess());
    }

    public DeferredGameRule<Integer> registerInteger(String identifier, GameRuleCategory category, int defaultValue, int min, int max, FeatureFlagSet requiredFeatures) {
        return register(identifier, category, GameRuleType.INT, IntegerArgumentType.integer(min, max), Codec.intRange(min, max), defaultValue, requiredFeatures, GameRuleTypeVisitor::visitInteger, value -> value);
    }

    public DeferredGameRule<Integer> registerInteger(String identifier, GameRuleCategory category, int defaultValue, int min, int max) {
        return registerInteger(identifier, category, defaultValue, min, max, FeatureFlags.DEFAULT_FLAGS);
    }

    public DeferredGameRule<Integer> registerInteger(String identifier, GameRuleCategory category, int defaultValue, int min, FeatureFlagSet requiredFeatures) {
        return registerInteger(identifier, category, defaultValue, min, Integer.MAX_VALUE, requiredFeatures);
    }

    public DeferredGameRule<Integer> registerInteger(String identifier, GameRuleCategory category, int defaultValue, int min) {
        return registerInteger(identifier, category, defaultValue, min, Integer.MAX_VALUE, FeatureFlags.DEFAULT_FLAGS);
    }

    public DeferredGameRule<Integer> registerInteger(String identifier, GameRuleCategory category, int defaultValue, FeatureFlagSet requiredFeatures) {
        return registerInteger(identifier, category, defaultValue, 0, Integer.MAX_VALUE, requiredFeatures);
    }

    public DeferredGameRule<Integer> registerInteger(String identifier, GameRuleCategory category, int defaultValue) {
        return registerInteger(identifier, category, defaultValue, 0, Integer.MAX_VALUE, FeatureFlags.DEFAULT_FLAGS);
    }

    public DeferredGameRule<Boolean> registerBoolean(String identifier, GameRuleCategory category, boolean defaultValue, FeatureFlagSet requiredFeatures) {
        return register(identifier, category, GameRuleType.BOOL, BoolArgumentType.bool(), Codec.BOOL, defaultValue, requiredFeatures, GameRuleTypeVisitor::visitBoolean, value -> value ? Command.SINGLE_SUCCESS : 0);
    }

    public DeferredGameRule<Boolean> registerBoolean(String identifier, GameRuleCategory category, boolean defaultValue) {
        return registerBoolean(identifier, category, defaultValue, FeatureFlags.DEFAULT_FLAGS);
    }

    public DeferredGameRule<Boolean> registerBoolean(String identifier, GameRuleCategory category, FeatureFlagSet requiredFeatures) {
        return registerBoolean(identifier, category, false, requiredFeatures);
    }

    public DeferredGameRule<Boolean> registerBoolean(String identifier, GameRuleCategory category) {
        return registerBoolean(identifier, category, false, FeatureFlags.DEFAULT_FLAGS);
    }

    public GameRuleCategory registerCategory(String identifier) {
        var category = new GameRuleCategory(registryName(identifier));
        registree().event(RegisterGameRuleCategoryEvent.class, event -> event.register(category));
        return category;
    }

    @SuppressWarnings("unchecked")
    public static <TValue> GameRules.VisitorCaller<TValue> emptyVistor() {
        return (GameRules.VisitorCaller<TValue>) EMPTY_CALLER;
    }

    @SuppressWarnings("unchecked")
    public static <TValue> ToIntFunction<TValue> alwaysSuccess() {
        return (ToIntFunction<TValue>) ALWAYS_SUCCESS;
    }
}
