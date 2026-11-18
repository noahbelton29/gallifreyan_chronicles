package uk.nbforge.gallifreyan_chronicles;

import java.util.concurrent.atomic.AtomicBoolean;
import uk.nbforge.gallifreyan_chronicles.addon.AddonManager;
import uk.nbforge.gallifreyan_chronicles.config.GCConfig;
import uk.nbforge.gallifreyan_chronicles.core.GCBlocks;
import uk.nbforge.gallifreyan_chronicles.core.GCDataComponents;
import uk.nbforge.gallifreyan_chronicles.core.GCEffects;
import uk.nbforge.gallifreyan_chronicles.core.GCFeatures;
import uk.nbforge.gallifreyan_chronicles.core.GCGameRules;
import uk.nbforge.gallifreyan_chronicles.core.GCItems;
import uk.nbforge.gallifreyan_chronicles.core.GCSounds;
import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;
import uk.nbforge.gallifreyan_chronicles.core.GCTreeDecoratorTypes;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisAutopilot;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisTravel;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMinds;

public class CommonClass {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    public static void init() {
        if (!INITIALIZED.compareAndSet(false, true)) {
            return;
        }

        GCConfig.load();
        GCGameRules.registerGameRules();
        GCDataComponents.registerDataComponents();
        GCTardisControls.registerTardisControls();
        GCItems.registerItems();
        GCBlocks.registerBlocks();
        GCFeatures.registerFeatures();
        GCTreeDecoratorTypes.registerTreeDecoratorTypes();
        GCSounds.registerSounds();
        GCEffects.registerEffects();
        TardisMinds.init();
        TardisTravel.init();
        TardisAutopilot.init();
        AddonManager.init();
    }
}
