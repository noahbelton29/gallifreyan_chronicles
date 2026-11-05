package com.noahtnt2009.gallifreyan_chronicles.compat.soundphysics;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.client.gui.hud.GCPowerDarkness;
import com.noahtnt2009.gallifreyan_chronicles.init.GCDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;

public final class SoundPhysicsCompat {
    private static volatile boolean loggedActive;

    private SoundPhysicsCompat() {
    }

    public static void markActive() {
        if (loggedActive) return;
        loggedActive = true;
        Constants.LOG.info("[GC] Sound Physics compat active in TARDIS interior");
    }

    public static boolean inTardisInterior() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return false;
        if (level.dimension().equals(GCDimensions.TARDIS_INTERIOR_LEVEL_KEY)) return true;
        Identifier id = level.dimension().identifier();
        return id.getNamespace().equals(Constants.MOD_ID) && id.getPath().startsWith("tardis/");
    }

    public static float tardisEcho() {
        return inTardisInterior() ? GCPowerDarkness.powerOffAmount() : 0.0F;
    }

    public static float damp(float value, float scale, float amount) {
        return value + (value * scale - value) * amount;
    }

    public static float boost(float value, float scale, float lift, float amount) {
        float boosted = Math.min(1.0F, value * scale + lift);
        return value + (boosted - value) * amount;
    }
}
