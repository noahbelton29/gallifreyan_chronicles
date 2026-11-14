package uk.nbforge.gallifreyan_chronicles.core;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.Codec;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;

public class GCGameRules {
    public static final GameRule<Boolean> TARDIS_GRIEFING = register(
            "gc_tardis_griefing",
            GCGameRuleCategories.GALLIFREYAN_CHRONICLES,
            true
    );

    private static GameRule<Boolean> register(String id, GameRuleCategory category, boolean defaultValue) {
        Identifier identifier = GCUtil.modIdentifier(id);

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
        Constants.LOG.info("Registered GC Game Rules");
    }
}