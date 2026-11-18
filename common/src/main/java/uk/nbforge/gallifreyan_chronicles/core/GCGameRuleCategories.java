package uk.nbforge.gallifreyan_chronicles.core;

import net.minecraft.world.level.gamerules.GameRuleCategory;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

/**
 * each loader registers these itself, neoforge thru its event and fabric thru the vanilla method cuz neo deprecated it
 * for some fucking reason
 */
public final class GCGameRuleCategories {
    public static final GameRuleCategory GALLIFREYAN_CHRONICLES =
            new GameRuleCategory(GCUtil.modIdentifier("gallifreyan_chronicles"));

    private GCGameRuleCategories() {}
}
