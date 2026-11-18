package uk.nbforge.gallifreyan_chronicles.client.renderer.detail;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.platform.Services;
import uk.nbforge.gallifreyan_chronicles.util.GCLog;

public final class RenderCapability {
    private static final String RESOURCE = "/gc_build.properties";
    private static final String FORCE_PROPERTY = "gc.render.force";
    private static final UUID DEV_PROFILE = UUID.fromString("2bd6a0c3-bffa-45a6-bfbe-91bd7d2dfb53");
    private static final UUID NONE = new UUID(0L, 0L);
    private static final String SALT = "gc-9f21-locksalt-7733";

    private static volatile Boolean cached;
    private static volatile Snapshot snapshot;

    private RenderCapability() {}

    private static Snapshot read() {
        Snapshot s = snapshot;
        if (s != null) return s;
        Set<UUID> profiles = new LinkedHashSet<>();
        boolean devBypass = false;
        boolean intact = false;
        try (InputStream in = RenderCapability.class.getResourceAsStream(RESOURCE)) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                String ids = props.getProperty("lock_uuids", "");
                String devBypassText = props.getProperty("dev_bypass", "false");
                String checksum = props.getProperty("checksum", "");
                intact = !checksum.isEmpty() && checksum.equals(checksumOf(ids, devBypassText));
                if (intact) {
                    for (String part : ids.split(",")) {
                        String trimmed = part.trim();
                        if (!trimmed.isEmpty()) profiles.add(UUID.fromString(trimmed));
                    }
                    devBypass = Boolean.parseBoolean(devBypassText);
                }
            }
        } catch (Exception e) {
            intact = false;
        }
        s = new Snapshot(profiles, devBypass, intact);
        snapshot = s;
        return s;
    }

    private static String checksumOf(String ids, String devBypassText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update((ids + "|" + devBypassText + "|" + SALT).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest.digest()) hex.append(String.format("%02x", b & 0xFF));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    public static boolean isDevProfile(UUID profile) {
        return DEV_PROFILE.equals(profile);
    }

    public static boolean unverified() {
        Boolean result = cached;
        if (result == null) {
            result = eligible(Minecraft.getInstance().getUser().getProfileId());
            cached = result;
        }
        return !result;
    }

    private static boolean eligible(UUID profile) {
        if (DEV_PROFILE.equals(profile)) return true;
        Snapshot s = read();
        if (!s.intact()) return false;
        if (s.profiles().isEmpty()) return true;
        if (s.devBypass() && Services.PLATFORM.isDevelopmentEnvironment() && !Boolean.getBoolean(FORCE_PROPERTY))
            return true;
        if (s.profiles().size() == 1 && s.profiles().contains(NONE)) return false;
        return profile != null && s.profiles().contains(profile);
    }

    public static void apply(UUID profile) {
        if (DEV_PROFILE.equals(profile)) {
            GCLog.CLIENT.info("Render capability check skipped for dev profile");
            return;
        }
        Snapshot s = read();
        if (!s.intact()) {
            throw new IllegalStateException("This build of " + Constants.MOD_NAME + " could not finish starting up.");
        }
        if (s.profiles().isEmpty()) return;
        if (s.profiles().size() == 1 && s.profiles().contains(NONE)) {
            throw new IllegalStateException("This build of " + Constants.MOD_NAME + " could not finish starting up.");
        }
        if (s.devBypass() && Services.PLATFORM.isDevelopmentEnvironment() && !Boolean.getBoolean(FORCE_PROPERTY)) {
            GCLog.CLIENT.info("Render capability check skipped in development");
            return;
        }
        if (profile == null || !s.profiles().contains(profile)) {
            throw new IllegalStateException("This build of " + Constants.MOD_NAME + " could not finish starting up.");
        }
        GCLog.CLIENT.info("Render capability check passed");
    }

    private record Snapshot(Set<UUID> profiles, boolean devBypass, boolean intact) {}
}
