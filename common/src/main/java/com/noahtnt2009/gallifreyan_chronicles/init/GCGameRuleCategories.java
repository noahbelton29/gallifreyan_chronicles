package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRuleCategory;

public class GCGameRuleCategories {
    public static final GameRuleCategory GALLIFREYAN_CHRONICLES = register("gallifreyan_chronicles");

    private static GameRuleCategory register(String name) {
        Identifier id = GCUtils.of(name);
        return GameRuleCategory.register(id);
    }

    public static void registerGameRuleCategories() {
        Constants.LOG.info("GC Registered Game Rule Categories");
    }
}