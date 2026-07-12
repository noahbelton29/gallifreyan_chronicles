package com.noahtnt2009.gallifreyan_chronicles;

import com.noahtnt2009.gallifreyan_chronicles.init.*;

public class CommonClass {
    public static void init() {
        GCGameRuleCategories.registerGameRuleCategories();
        GCGameRules.registerGameRules();
        GCDataComponents.registerDataComponents();
        GCTardisControls.registerTardisControls();
        GCItems.registerItems();
        GCBlocks.registerBlocks();
        GCFeatures.registerFeatures();
        GCTreeDecoratorTypes.registerTreeDecoratorTypes();
        GCSounds.registerSounds();
        Constants.LOG.info("dunnit");
    }
}