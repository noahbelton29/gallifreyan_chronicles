package com.noahtnt2009.gallifreyan_chronicles;

import com.noahtnt2009.gallifreyan_chronicles.config.GCConfig;
import com.noahtnt2009.gallifreyan_chronicles.init.*;

import java.util.concurrent.atomic.AtomicBoolean;

public class CommonClass {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    public static void init() {
        if (!INITIALIZED.compareAndSet(false, true)) {
            return;
        }

        GCConfig.load();
        GCGameRuleCategories.registerGameRuleCategories();
        GCGameRules.registerGameRules();
        GCDataComponents.registerDataComponents();
        GCTardisControls.registerTardisControls();
        GCItems.registerItems();
        GCBlocks.registerBlocks();
        GCFeatures.registerFeatures();
        GCTreeDecoratorTypes.registerTreeDecoratorTypes();
        GCSounds.registerSounds();
    }
}