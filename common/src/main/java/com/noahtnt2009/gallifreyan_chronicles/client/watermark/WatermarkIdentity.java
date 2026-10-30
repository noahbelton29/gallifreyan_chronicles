package com.noahtnt2009.gallifreyan_chronicles.client.watermark;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;

public final class WatermarkIdentity {
    private static final String RESOURCE = "/gc_build.properties";
    private static final String ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
    private static final String FORCE_PROPERTY = "gc.watermark.force";

    private static boolean loaded;
    private static boolean enabled = true;
    private static boolean fileEnabled = true;
    private static String buildId = "NOBUILD";

    private WatermarkIdentity() {
    }

    private static synchronized void load() {
        if (loaded) return;
        loaded = true;

        try (InputStream in = WatermarkIdentity.class.getResourceAsStream(RESOURCE)) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                enabled = !"false".equalsIgnoreCase(props.getProperty("watermark", "true").trim());
                String id = props.getProperty("build_id", "").trim();
                if (!id.isEmpty()) buildId = id.toUpperCase(Locale.ROOT);
            }
        } catch (Exception e) {
            Constants.LOG.error("Could not read {}", RESOURCE, e);
        }

        fileEnabled = enabled;

        if (Services.PLATFORM.isDevelopmentEnvironment() && !Boolean.getBoolean(FORCE_PROPERTY)) {
            enabled = false;
        }
        Constants.LOG.info("[GC] Watermark config: enabled={}, build ID={}", enabled, buildId);
    }

    public static boolean enabled() {
        load();
        return enabled;
    }

    public static boolean bannerEnabled() {
        load();
        return fileEnabled;
    }

    public static String buildId() {
        load();
        return buildId;
    }

    public static String label(String username, UUID accountId) {
        String name = username == null ? "UNKNOWN" : username.toUpperCase(Locale.ROOT);
        return name + " " + buildId() + "-" + check(buildId(), accountId);
    }

    public static String check(String buildId, UUID accountId) {
        byte[] hash = sha256(buildId + ":" + accountId);
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) sb.append(ALPHABET.charAt((hash[i] & 0xFF) % 32));
        return sb.toString();
    }

    public static long seed(String label) {
        byte[] hash = sha256(label);
        long seed = 0;
        for (int i = 0; i < 8; i++) seed = (seed << 8) | (hash[i] & 0xFF);
        return seed;
    }

    private static byte[] sha256(String s) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
