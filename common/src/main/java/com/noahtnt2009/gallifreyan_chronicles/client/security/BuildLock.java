package com.noahtnt2009.gallifreyan_chronicles.client.security;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;

import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

public final class BuildLock {
    private static final String RESOURCE = "/gc_build.properties";
    private static final String FORCE_PROPERTY = "gc.lock.force";

    private BuildLock() {
    }

    public static Set<UUID> allowedAccounts() {
        Set<UUID> allowed = new LinkedHashSet<>();
        try (InputStream in = BuildLock.class.getResourceAsStream(RESOURCE)) {
            if (in == null) return allowed;
            Properties props = new Properties();
            props.load(in);
            for (String part : props.getProperty("lock_uuids", "").split(",")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) allowed.add(UUID.fromString(trimmed));
            }
        } catch (Exception e) {
            throw new IllegalStateException("This build of " + Constants.MOD_NAME + " has an unreadable account lock", e);
        }
        return allowed;
    }

    public static void enforce(UUID account) {
        Set<UUID> allowed = allowedAccounts();
        if (allowed.isEmpty()) return;
        if (Services.PLATFORM.isDevelopmentEnvironment() && !Boolean.getBoolean(FORCE_PROPERTY)) {
            Constants.LOG.info("[GC] Account lock skipped in development (locked to {})", allowed);
            return;
        }
        if (account == null || !allowed.contains(account)) {
            throw new IllegalStateException("This build of " + Constants.MOD_NAME
                    + " is licensed to a different Minecraft account and cannot be used here.");
        }
        Constants.LOG.info("[GC] Account lock verified");
    }
}
