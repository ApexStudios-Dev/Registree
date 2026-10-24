package dev.apexstudios.registree.api.util;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;
import dev.apexstudios.registree.api.Registree;
import dev.apexstudios.registree.api.holders.DeferredGameRule;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;

public interface GameRuleRegistrations {
    static <TProperties extends AbstractProperties<TProperties, TValue>, TValue> DeferredGameRule<TValue> register(Registree registree, String identifier, Supplier<TProperties> propertiesFactory, UnaryOperator<TProperties> propertiesModifier) {
        return Registrations.registerForHolder(
                registree,
                Registries.GAME_RULE,
                identifier,
                () -> propertiesModifier.andThen(AbstractProperties::build)
                        .apply(propertiesFactory.get()),
                DeferredGameRule::new
        );
    }

    static DeferredGameRule<Boolean> registerBoolean(Registree registree, String identifier, UnaryOperator<BooleanProperties> properties) {
        return register(registree, identifier, BooleanProperties::new, properties);
    }

    static DeferredGameRule<Boolean> registerBoolean(Registree registree, String identifier) {
        return registerBoolean(registree, identifier, UnaryOperator.identity());
    }

    static DeferredGameRule<Integer> registerInteger(Registree registree, String identifier, UnaryOperator<IntegerProperties> properties) {
        return register(registree, identifier, IntegerProperties::new, properties);
    }

    static DeferredGameRule<Integer> registerInteger(Registree registree, String identifier) {
        return registerInteger(registree, identifier, UnaryOperator.identity());
    }

    abstract class AbstractProperties<TSelf extends AbstractProperties<TSelf, TValue>, TValue> {
        protected Supplier<GameRuleCategory> category = () -> GameRuleCategory.MISC;
        protected Supplier<FeatureFlagSet> requiredFeatures = () -> FeatureFlags.DEFAULT_FLAGS;

        protected AbstractProperties() {
        }

        public TSelf category(Supplier<GameRuleCategory> category) {
            this.category = category;
            return self();
        }

        public TSelf requiredFeatures(Supplier<FeatureFlagSet> requiredFeatures) {
            this.requiredFeatures = requiredFeatures;
            return self();
        }

        /*public TSelf requiredFeatures(FeatureFlag requiredFeature) {
            return requiredFeatures(FeatureFlagSet.of(requiredFeature));
        }

        public TSelf requiredFeatures(FeatureFlag requiredFeature, FeatureFlag requiredFeatures) {
            return requiredFeatures(FeatureFlagSet.of(requiredFeature, requiredFeatures));
        }*/

        @SuppressWarnings("unchecked")
        protected TSelf self() {
            return (TSelf) this;
        }

        protected abstract GameRule<TValue> build();
    }

    final class BooleanProperties extends AbstractProperties<BooleanProperties, Boolean> {
        private boolean defaultValue = false;

        private BooleanProperties() {
        }

        public BooleanProperties defaultValue(boolean defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        @Override
        protected GameRule<Boolean> build() {
            return new GameRule<>(
                    category.get(),
                    GameRuleType.BOOL,
                    BoolArgumentType.bool(),
                    GameRuleTypeVisitor::visitBoolean,
                    Codec.BOOL,
                    value -> value ? Command.SINGLE_SUCCESS : 0,
                    defaultValue,
                    requiredFeatures.get()
            );
        }
    }

    final class IntegerProperties extends AbstractProperties<IntegerProperties, Integer> {
        private int defaultValue = 0;
        private int minValue = Integer.MIN_VALUE;
        private int maxValue = Integer.MAX_VALUE;

        private IntegerProperties() {
        }

        public IntegerProperties value(int defaultValue, int minValue, int maxValue) {
            this.maxValue = Math.max(minValue, maxValue);
            this.minValue = Math.min(minValue, maxValue);
            this.defaultValue = Math.clamp(defaultValue, minValue, maxValue);

            return this;
        }

        public IntegerProperties value(int defaultValue, int minValue) {
            return value(defaultValue, minValue, maxValue);
        }

        public IntegerProperties value(int defaultValue) {
            return value(defaultValue, minValue, maxValue);
        }

        @Override
        protected GameRule<Integer> build() {
            return new GameRule<>(
                    category.get(),
                    GameRuleType.INT,
                    IntegerArgumentType.integer(minValue, maxValue),
                    GameRuleTypeVisitor::visitInteger,
                    Codec.intRange(minValue, maxValue),
                    value -> value,
                    defaultValue,
                    requiredFeatures.get()
            );
        }
    }
}
