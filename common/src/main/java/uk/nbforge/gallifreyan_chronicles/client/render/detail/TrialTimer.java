package uk.nbforge.gallifreyan_chronicles.client.render.detail;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import java.io.InputStream;
import java.time.Instant;
import java.util.Properties;

public final class TrialTimer {
    private static final String RESOURCE = "/gc_build.properties";
    private static final String FORCE_PROPERTY = "gc.trial.force";
    private static final int CHECK_INTERVAL_TICKS = 20;

    private static boolean loaded;
    private static long expiresAt = -1L;
    private static int ticks;

    private TrialTimer() {
    }

    private static synchronized void load() {
        if (loaded) return;
        loaded = true;
        try (InputStream in = TrialTimer.class.getResourceAsStream(RESOURCE)) {
            if (in == null) return;
            Properties props = new Properties();
            props.load(in);
            String value = props.getProperty("expires_at", "").trim();
            if (!value.isEmpty()) expiresAt = Long.parseLong(value);
        } catch (Exception e) {
            return;
        }
        if (expiresAt > 0L) {
            Constants.LOG.info("[GC] Build window ends at {}", Instant.ofEpochMilli(expiresAt));
        }
    }

    private static boolean active() {
        load();
        if (expiresAt <= 0L) return false;
        return !Services.PLATFORM.isDevelopmentEnvironment() || Boolean.getBoolean(FORCE_PROPERTY);
    }

    public static long remainingMillis() {
        if (!active()) return -1L;
        return Math.max(0L, expiresAt - System.currentTimeMillis());
    }

    public static boolean isExpired() {
        return active() && System.currentTimeMillis() >= expiresAt;
    }

    public static void checkAtStartup() {
        if (isExpired()) {
            throw new IllegalStateException("This test build of " + Constants.MOD_NAME + " expired at "
                    + Instant.ofEpochMilli(expiresAt) + ". Ask for a new build to keep testing.");
        }
    }

    public static void tick(Minecraft minecraft) {
        if (++ticks % CHECK_INTERVAL_TICKS != 0) return;
        if (minecraft.level == null || !isExpired()) return;

        Component title = Component.translatable("gc.build.expired.title");
        Component reason = Component.translatable("gc.build.expired.message");
        minecraft.disconnectFromWorld(reason);
        minecraft.gui.setScreen(new DisconnectedScreen(new TitleScreen(), title, reason));
    }
}
