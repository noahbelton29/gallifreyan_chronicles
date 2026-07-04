package com.noahtnt2009.gallifreyan_chronicles;

import com.mojang.datafixers.kinds.Const;
import com.noahtnt2009.gallifreyan_chronicles.init.*;

public class CommonClass {
    public static void init() {
        GCGameRuleCategories.registerGameRuleCategories();
        GCGameRules.registerGameRules();
        GCDataComponents.registerDataComponents();
        GCItems.registerItems();
        GCBlocks.registerBlocks();
        GCFeatures.registerFeatures();
        GCTreeDecoratorTypes.registerTreeDecoratorTypes();
        GCSounds.registerSounds();
        Constants.LOG.info("dunnit");
    }
}