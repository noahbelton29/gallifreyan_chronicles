package uk.nbforge.gallifreyan_chronicles.core.lifecycle;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.util.GCSchedulerUtil;
import net.minecraft.server.MinecraftServer;

public final class CiBootTest {
    public static final String PROPERTY = Constants.MOD_ID + ".bootTest";
    public static final String PASS_LINE = "[GC] BOOT TEST PASSED";

    private static final int TICKS = 100;

    private CiBootTest() {
    }

    public static void onLevelsCreated(MinecraftServer server) {
        if (!Boolean.getBoolean(PROPERTY)) return;
        Constants.LOG.info("[GC] Boot test: levels loaded, ticking for {} ticks before shutting down", TICKS);
        GCSchedulerUtil.runLater(server, TICKS, () -> {
            Constants.LOG.info(PASS_LINE);
            server.halt(false);
        });
    }
}
