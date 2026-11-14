package uk.nbforge.gallifreyan_chronicles.core;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRuleCategory;

public class GCGameRuleCategories {
    public static final GameRuleCategory GALLIFREYAN_CHRONICLES = register("gallifreyan_chronicles");

    private static GameRuleCategory register(String name) {
        Identifier id = GCUtil.modIdentifier(name);
        return GameRuleCategory.register(id);
    }

    public static void registerGameRuleCategories() {
        Constants.LOG.info("Registered GC Game Rule Categories");
    }
}