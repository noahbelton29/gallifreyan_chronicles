package uk.nbforge.gallifreyan_chronicles.tardis.ecs.system;

import uk.nbforge.gallifreyan_chronicles.tardis.TardisApi;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.Nullable;

import java.util.*;

public final class ConsoleSyncSystem {
    private static final Map<UUID, Set<TardisConsoleBlockEntity>> CONSOLES = new HashMap<>();

    private ConsoleSyncSystem() {
    }

    private static @Nullable UUID keyOf(TardisConsoleBlockEntity console) {
        UUID id = console.getTardisId();
        if (id == null && console.getLevel() != null) {
            id = TardisApi.idOfLevel(console.getLevel()).orElse(null);
        }
        return id;
    }

    private static final int CONTROL_CHECK_INTERVAL_TICKS = 40;

    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % CONTROL_CHECK_INTERVAL_TICKS != 0) return;
        List<TardisConsoleBlockEntity> consoles;
        synchronized (ConsoleSyncSystem.class) {
            consoles = new ArrayList<>();
            for (Set<TardisConsoleBlockEntity> set : CONSOLES.values()) consoles.addAll(set);
        }
        for (TardisConsoleBlockEntity console : consoles) {
            if (!console.isRemoved()) console.ensureControls();
        }
    }

    public static synchronized void register(TardisConsoleBlockEntity console) {
        UUID key = keyOf(console);
        UUID old = console.syncedTardisId();
        if (old != null && !old.equals(key)) {
            removeFrom(old, console);
        }
        console.setSyncedTardisId(key);
        if (key != null) {
            CONSOLES.computeIfAbsent(key, k -> Collections.newSetFromMap(new WeakHashMap<>())).add(console);
        }
    }

    public static synchronized void unregister(TardisConsoleBlockEntity console) {
        UUID key = console.syncedTardisId();
        if (key == null) return;
        removeFrom(key, console);
        console.setSyncedTardisId(null);
        promoteIfPrimary(console, key);
    }

    private static void removeFrom(UUID key, TardisConsoleBlockEntity console) {
        Set<TardisConsoleBlockEntity> set = CONSOLES.get(key);
        if (set == null) return;
        set.remove(console);
        if (set.isEmpty()) CONSOLES.remove(key);
    }

    private static void promoteIfPrimary(TardisConsoleBlockEntity gone, UUID key) {
        if (!(gone.getLevel() instanceof ServerLevel level)) return;
        TardisManager manager = TardisManager.get(level.getServer());
        if (!gone.getBlockPos().equals(Objects.requireNonNull(manager.getConsoleBlockPos(key)))) return;
        List<TardisConsoleBlockEntity> live = consolesOf(key);
        if (!live.isEmpty()) {
            manager.setConsoleBlockPos(key, live.getFirst().getBlockPos());
        }
    }

    public static synchronized @Nullable TardisConsoleBlockEntity firstLiveOf(UUID tardisId) {
        Set<TardisConsoleBlockEntity> set = CONSOLES.get(tardisId);
        if (set == null) return null;
        for (TardisConsoleBlockEntity console : set) {
            if (!console.isRemoved()) return console;
        }
        return null;
    }

    public static synchronized List<TardisConsoleBlockEntity> consolesOf(UUID tardisId) {
        Set<TardisConsoleBlockEntity> set = CONSOLES.get(tardisId);
        if (set == null) return List.of();
        List<TardisConsoleBlockEntity> live = new ArrayList<>();
        for (TardisConsoleBlockEntity console : set) {
            if (!console.isRemoved()) live.add(console);
        }
        return live;
    }

    public static synchronized List<TardisConsoleBlockEntity> liveConsolesWith(TardisConsoleBlockEntity console) {
        UUID key = console.syncedTardisId();
        if (key == null) {
            return console.isRemoved() ? List.of() : List.of(console);
        }
        List<TardisConsoleBlockEntity> live = consolesOf(key);
        if (!console.isRemoved() && !live.contains(console)) {
            live = new ArrayList<>(live);
            live.add(console);
        }
        return live;
    }

    public static void mirrorActivated(TardisConsoleBlockEntity source, String controlId, boolean activated) {
        if (!(source.getLevel() instanceof ServerLevel)) return;
        for (TardisConsoleBlockEntity other : liveConsolesWith(source)) {
            if (other != source) other.setControlActivatedLocal(controlId, activated);
        }
    }

    public static void mirrorReconcile(TardisConsoleBlockEntity source, String controlId, boolean activated) {
        if (!(source.getLevel() instanceof ServerLevel)) return;
        for (TardisConsoleBlockEntity other : liveConsolesWith(source)) {
            if (other != source) other.reconcileControlLocal(controlId, activated);
        }
    }

    public static synchronized void clear() {
        CONSOLES.clear();
    }
}
