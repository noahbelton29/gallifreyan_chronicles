package uk.nbforge.gallifreyan_chronicles;

import java.util.concurrent.atomic.AtomicBoolean;
import uk.nbforge.gallifreyan_chronicles.addon.AddonManager;
import uk.nbforge.gallifreyan_chronicles.config.GCConfig;
import uk.nbforge.gallifreyan_chronicles.core.*;
import uk.nbforge.gallifreyan_chronicles.core.GCGameRuleCategories;
import uk.nbforge.gallifreyan_chronicles.core.GCGameRules;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.TardisMindSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisAutopilot;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisTravel;

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
        GCEffects.registerEffects();
        TardisMindSystem.init();
        TardisTravel.init();
        TardisAutopilot.init();
        AddonManager.init();
    }
}
