package uk.nbforge.gallifreyan_chronicles.tardis.manager.deletion;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TardisDeletionMode {
    private static final Set<UUID> ENABLED = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Long> LAST_WARNING = new ConcurrentHashMap<>();
    private static final long WARNING_COOLDOWN_TICKS = 200L;

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

    public static boolean shouldWarn(UUID playerId, long gameTime) {
        if (playerId == null) return false;
        Long last = LAST_WARNING.get(playerId);
        if (last != null && gameTime >= last && gameTime - last < WARNING_COOLDOWN_TICKS) return false;
        LAST_WARNING.put(playerId, gameTime);
        return true;
    }

    public static void clear(UUID playerId) {
        if (playerId == null) return;
        ENABLED.remove(playerId);
        LAST_WARNING.remove(playerId);
    }
}