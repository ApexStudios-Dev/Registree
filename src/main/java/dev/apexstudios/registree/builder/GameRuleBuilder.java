package dev.apexstudios.registree.builder;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.serialization.Codec;
import dev.apexstudios.registree.BaseRegistree;
import dev.apexstudios.registree.holder.DeferredGameRule;
import dev.apexstudios.registree.holder.Holders;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRules;
import org.jetbrains.annotations.ApiStatus;

public final class GameRuleBuilder<TRuleType> extends AbstractBuilder<GameRule<?>, GameRule<TRuleType>, DeferredGameRule<TRuleType>, GameRuleBuilder<TRuleType>> {
    private GameRuleCategory category = GameRuleCategory.MISC;
    private final GameRuleType gameRuleType;
    private final ArgumentType<TRuleType> argumentType;
    private final GameRules.VisitorCaller<TRuleType> visitorCaller;
    private final Codec<TRuleType> codec;
    private final ToIntFunction<TRuleType> commandResult;
    private final Supplier<TRuleType> defaultValue;
    private FeatureFlagSet requiredFeatures = FeatureFlagSet.of();
    private RegistryEventHelper.GameRuleListener<TRuleType> changeListener = (server, gameRule, newValue) -> { };

    @ApiStatus.Internal
    public GameRuleBuilder(BaseRegistree<?> registree, String identifier, GameRuleType gameRuleType, ArgumentType<TRuleType> argumentType, GameRules.VisitorCaller<TRuleType> visitorCaller, Codec<TRuleType> codec, ToIntFunction<TRuleType> commandResult, Supplier<TRuleType> defaultValue) {
        super(registree, Registries.GAME_RULE, identifier, Holders::createGameRule);

        this.gameRuleType = gameRuleType;
        this.argumentType = argumentType;
        this.visitorCaller = visitorCaller;
        this.codec = codec;
        this.commandResult = commandResult;
        this.defaultValue = defaultValue;
    }

    public GameRuleBuilder<TRuleType> category(GameRuleCategory category) {
        this.category = category;
        return this;
    }

    public GameRuleBuilder<TRuleType> category(String identifier) {
        return category(registree.gameRuleCategory(identifier));
    }

    public GameRuleBuilder<TRuleType> requiredFeatures(FeatureFlagSet requiredFeatures) {
        this.requiredFeatures = this.requiredFeatures.join(requiredFeatures);
        return this;
    }

    public GameRuleBuilder<TRuleType> requiredFeatures(FeatureFlag flag) {
        return requiredFeatures(FeatureFlagSet.of(flag));
    }

    public GameRuleBuilder<TRuleType> requiredFeatures(FeatureFlag flag, FeatureFlag... flags) {
        return requiredFeatures(FeatureFlagSet.of(flag, flags));
    }

    public GameRuleBuilder<TRuleType> whenChanged(RegistryEventHelper.GameRuleListener<TRuleType> changeListener) {
        this.changeListener = this.changeListener.andThen(changeListener);
        return this;
    }

    public GameRuleBuilder<TRuleType> whenChanged(BiConsumer<GameRule<TRuleType>, TRuleType> changeListener) {
        return whenChanged((server, gameRule, newValue) -> changeListener.accept(gameRule, newValue));
    }

    public GameRuleBuilder<TRuleType> whenChanged(Consumer<TRuleType> changeListener) {
        return whenChanged((gameRule, newValue) -> changeListener.accept(newValue));
    }

    @Override
    protected GameRule<TRuleType> createValue(ResourceKey<GameRule<?>> registryKey) {
        return new GameRule<>(category, gameRuleType, argumentType, visitorCaller, codec, commandResult, defaultValue.get(), requiredFeatures);
    }

    @Override
    protected void registerEvents() {
        super.registerEvents();

        RegistryEventHelper.registerGameRuleChanged(this::value, changeListener);
    }
}
