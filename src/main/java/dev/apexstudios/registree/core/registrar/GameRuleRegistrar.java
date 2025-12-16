package dev.apexstudios.registree.core.registrar;

import dev.apexstudios.registree.api.IRegistree;
import dev.apexstudios.registree.api.holder.DeferredGameRule;
import dev.apexstudios.registree.api.holder.Holders;
import dev.apexstudios.registree.api.registrar.IGameRuleRegistrar;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public class GameRuleRegistrar extends Registrar<GameRule<?>, DeferredGameRule<?>> implements IGameRuleRegistrar {
    public GameRuleRegistrar(IRegistree registree) {
        super(registree, Registries.GAME_RULE, Holders::createGameRule);
    }

    @Override
    public GameRuleCategory registerGameRuleCategory(String identifier) {
        var category = new GameRuleCategory(registryName(identifier));
        // TODO: Update to RegisterGameRuleCategoryEvent once 'https://github.com/neoforged/NeoForge/pull/2877' is merged
        registree().event(FMLCommonSetupEvent.class, event -> event.enqueueWork(() -> GameRuleCategory.register(category.id())));
        return category;
    }
}
