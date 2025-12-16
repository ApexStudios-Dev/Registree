package dev.apexstudios.registree.api.builder;

import dev.apexstudios.registree.api.holder.DeferredGameRule;
import java.util.function.ToIntFunction;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;

public interface IGameRuleBuilder<TType, TSelf extends IGameRuleBuilder<TType, TSelf>> extends IBuilder.WithHolder<GameRule<?>, GameRule<TType>, DeferredGameRule<TType>, TSelf> {
    TSelf requiredFeatures(FeatureFlagSet requiredFeatures);

    default TSelf requiredFeature(FeatureFlag requiredFeature) {
        return requiredFeatures(FeatureFlagSet.of(requiredFeature));
    }

    default TSelf requiredFeature(FeatureFlag requiredFeature, FeatureFlag... requiredFeatures) {
        return requiredFeatures(FeatureFlagSet.of(requiredFeature, requiredFeatures));
    }

    interface Modded<TType> extends IGameRuleBuilder<TType, Modded<TType>> {
        Modded<TType> commandResult(ToIntFunction<TType> commandResult);

        Modded<TType> visitor(GameRules.VisitorCaller<TType> visitor);
    }

    interface Vanilla<TType> extends IGameRuleBuilder<TType, Vanilla<TType>> {

    }
}
