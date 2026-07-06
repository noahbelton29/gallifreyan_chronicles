package com.noahtnt2009.gallifreyan_chronicles.init;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.Codec;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;

public class GCGameRules {
    public static final GameRule<Boolean> DAYLIGHT_CYCLE_AFFECTS_GLOW = register(
            "gc_daylight_cycle_affects_glow",
            GCGameRuleCategories.GALLIFREYAN_CHRONICLES,
            true
    );

    private static GameRule<Boolean> register(String id, GameRuleCategory category, boolean defaultValue) {
        Identifier identifier = GCUtils.of(id);

        GameRule<Boolean> rule = new GameRule<>(
                category,
                GameRuleType.BOOL,
                BoolArgumentType.bool(),
                GameRuleTypeVisitor::visitBoolean,
                Codec.BOOL,
                (b) -> b ? 1 : 0,
                defaultValue,
                FeatureFlagSet.of()
        );

        return Registry.register(BuiltInRegistries.GAME_RULE, identifier, rule);
    }

    public static void registerGameRules() {
        Constants.LOG.info("GC Registered Game Rules");
    }
}