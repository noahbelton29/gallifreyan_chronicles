package com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.manager.TardisManager;
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
            id = DoorSyncSystem.tardisIdOf(console.getLevel());
        }
        return id;
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
