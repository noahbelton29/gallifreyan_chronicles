package uk.nbforge.gallifreyan_chronicles.client.compat.soundphysics;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.client.gui.hud.PowerDarkness;
import uk.nbforge.gallifreyan_chronicles.core.GCDimensions;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisDimensionUtil;

public final class SoundPhysicsCompat {
    private static volatile boolean loggedActive;

    private SoundPhysicsCompat() {}

    public static void markActive() {
        if (loggedActive) return;
        loggedActive = true;
        Constants.LOG.info("[GC] Sound Physics compat active in TARDIS interior");
    }

    public static boolean inTardisInterior() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return false;
        if (level.dimension().equals(GCDimensions.TARDIS_INTERIOR_LEVEL_KEY)) return true;
        return TardisDimensionUtil.isInterior(level);
    }

    public static float tardisEcho() {
        return inTardisInterior() ? PowerDarkness.powerOffAmount() : 0.0f;
    }

    public static float damp(float value, float scale, float amount) {
        return value + (value * scale - value) * amount;
    }

    public static float boost(float value, float scale, float lift, float amount) {
        float boosted = Math.min(1.0f, value * scale + lift);
        return value + (boosted - value) * amount;
    }
}
