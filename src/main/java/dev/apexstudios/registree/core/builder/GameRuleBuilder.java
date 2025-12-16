package dev.apexstudios.registree.core.builder;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.serialization.Codec;
import dev.apexstudios.registree.api.builder.IBuilderContext;
import dev.apexstudios.registree.api.builder.IGameRuleBuilder;
import dev.apexstudios.registree.api.holder.DeferredGameRule;
import dev.apexstudios.registree.api.registrar.IGameRuleRegistrar;
import java.util.function.ToIntFunction;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRules;

public abstract class GameRuleBuilder<TType, TSelf extends IGameRuleBuilder<TType, TSelf>> extends Builder<GameRule<?>, GameRule<TType>, DeferredGameRule<TType>, TSelf> implements IGameRuleBuilder<TType, TSelf> {
    private final GameRuleCategory category;
    private final GameRuleType type;
    private final ArgumentType<TType> argumentType;
    private final Codec<TType> codec;
    private final TType defaultValue;
    private FeatureFlagSet requiredFeatures = FeatureFlags.DEFAULT_FLAGS;

    public GameRuleBuilder(IGameRuleRegistrar registrar, String identifier, GameRuleCategory category, GameRuleType type, ArgumentType<TType> argumentType, Codec<TType> codec, TType defaultValue) {
        super(registrar, identifier);

        this.category = category;
        this.type = type;
        this.argumentType = argumentType;
        this.codec = codec;
        this.defaultValue = defaultValue;
    }

    protected abstract GameRule<TType> createElement(GameRuleCategory category, GameRuleType type, ArgumentType<TType> argumentType, Codec<TType> codec, TType defaultValue, FeatureFlagSet requiredFeatures);

    @Override
    protected GameRule<TType> createElement(IBuilderContext<GameRule<?>> context) {
        return createElement(category, type, argumentType, codec, defaultValue, requiredFeatures);
    }

    @SuppressWarnings("unchecked")
    @Override
    public TSelf requiredFeatures(FeatureFlagSet requiredFeatures) {
        this.requiredFeatures = requiredFeatures;
        return (TSelf) this;
    }

    public static class Modded<TType> extends GameRuleBuilder<TType, IGameRuleBuilder.Modded<TType>> implements IGameRuleBuilder.Modded<TType> {
        private GameRules.VisitorCaller<TType> visitor = IGameRuleRegistrar.emptyVisitor();
        private ToIntFunction<TType> commandResult = value -> Command.SINGLE_SUCCESS;

        public Modded(IGameRuleRegistrar registrar, String identifier, GameRuleCategory category, GameRuleType type, ArgumentType<TType> argumentType, Codec<TType> codec, TType defaultValue) {
            super(registrar, identifier, category, type, argumentType, codec, defaultValue);
        }

        @Override
        protected GameRule<TType> createElement(GameRuleCategory category, GameRuleType type, ArgumentType<TType> argumentType, Codec<TType> codec, TType defaultValue, FeatureFlagSet requiredFeatures) {
            return new GameRule<>(category, type, argumentType, visitor, codec, commandResult, defaultValue, requiredFeatures);
        }

        @Override
        public IGameRuleBuilder.Modded<TType> commandResult(ToIntFunction<TType> commandResult) {
            this.commandResult = commandResult;
            return this;
        }

        @Override
        public IGameRuleBuilder.Modded<TType> visitor(GameRules.VisitorCaller<TType> visitor) {
            this.visitor = visitor;
            return this;
        }
    }

    public static class Vanilla<TType> extends GameRuleBuilder<TType, IGameRuleBuilder.Vanilla<TType>> implements IGameRuleBuilder.Vanilla<TType> {
        private final GameRules.VisitorCaller<TType> visitor;
        private final ToIntFunction<TType> commandResult;

        public Vanilla(IGameRuleRegistrar registrar, String identifier, GameRuleCategory category, GameRuleType type, ArgumentType<TType> argumentType, Codec<TType> codec, TType defaultValue, GameRules.VisitorCaller<TType> visitor, ToIntFunction<TType> commandResult) {
            super(registrar, identifier, category, type, argumentType, codec, defaultValue);

            this.visitor = visitor;
            this.commandResult = commandResult;
        }

        @Override
        protected GameRule<TType> createElement(GameRuleCategory category, GameRuleType type, ArgumentType<TType> argumentType, Codec<TType> codec, TType defaultValue, FeatureFlagSet requiredFeatures) {
            return new GameRule<>(category, type, argumentType, visitor, codec, commandResult, defaultValue, requiredFeatures);
        }
    }
}
