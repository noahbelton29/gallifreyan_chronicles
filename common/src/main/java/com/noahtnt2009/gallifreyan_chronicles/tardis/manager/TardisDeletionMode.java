package com.noahtnt2009.gallifreyan_chronicles.tardis.manager;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TardisDeletionMode {
    private static final Set<UUID> ENABLED = ConcurrentHashMap.newKeySet();

    private TardisDeletionMode() {
    }

    public static boolean isEnabled(UUID playerId) {
        return playerId != null && ENABLED.contains(playerId);
    }

    public static void setEnabled(UUID playerId, boolean enabled) {
        if (playerId == null) return;
        if (enabled) {
            ENABLED.add(playerId);
        } else {
            ENABLED.remove(playerId);
        }
    }

    public static void clear(UUID playerId) {
        if (playerId == null) return;
        ENABLED.remove(playerId);
    }
}