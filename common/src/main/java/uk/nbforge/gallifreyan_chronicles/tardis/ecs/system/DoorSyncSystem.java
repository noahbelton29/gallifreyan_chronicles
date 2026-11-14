package uk.nbforge.gallifreyan_chronicles.tardis.ecs.system;

import uk.nbforge.gallifreyan_chronicles.compat.portal.TardisPortalBridge;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.InteriorDoorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisApi;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.component.DoorState;
import net.minecraft.server.MinecraftServer;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

public final class DoorSyncSystem {
    private static final Map<UUID, Set<InteriorDoorBlockEntity>> INTERIOR_DOORS = new HashMap<>();

    private DoorSyncSystem() {
    }

    private static @Nullable TardisExteriorBlockEntity exteriorOf(MinecraftServer server, UUID tardisId) {
        return TardisApi.get(server, tardisId).flatMap(Tardis::exterior).orElse(null);
    }

    public static synchronized void clear() {
        INTERIOR_DOORS.clear();
    }

    public static synchronized void register(UUID tardisId, InteriorDoorBlockEntity door) {
        INTERIOR_DOORS.computeIfAbsent(tardisId, k -> Collections.newSetFromMap(new WeakHashMap<>())).add(door);
    }

    public static synchronized void unregister(UUID tardisId, InteriorDoorBlockEntity door) {
        Set<InteriorDoorBlockEntity> doors = INTERIOR_DOORS.get(tardisId);
        if (doors == null) return;
        doors.remove(door);
        if (doors.isEmpty()) INTERIOR_DOORS.remove(tardisId);
    }

    public static synchronized List<InteriorDoorBlockEntity> doorsOf(UUID tardisId) {
        Set<InteriorDoorBlockEntity> doors = INTERIOR_DOORS.get(tardisId);
        if (doors == null) return List.of();
        List<InteriorDoorBlockEntity> live = new ArrayList<>();
        for (InteriorDoorBlockEntity door : doors) {
            if (!door.isRemoved()) live.add(door);
        }
        return live;
    }

    public static @Nullable InteriorDoorBlockEntity primaryDoor(UUID tardisId) {
        for (InteriorDoorBlockEntity door : doorsOf(tardisId)) {
            if (door.isPrimary()) return door;
        }
        return null;
    }

    public static void syncInteriorDoorControl(MinecraftServer server, UUID tardisId, DoorState state) {
        applyControl(server, tardisId, GCTardisControls.INTERIOR_DOOR_CONTROL, !state.isClosed(), false);
    }

    /* silently keeps the console's interior door switch matching the primary door's real state */
    public static void reconcileInteriorDoorControl(MinecraftServer server, UUID tardisId, DoorState state) {
        applyControl(server, tardisId, GCTardisControls.INTERIOR_DOOR_CONTROL, !state.isClosed(), true);
    }

    public static void onExteriorDoorChanged(MinecraftServer server, UUID tardisId, DoorState state,
                                             String animation, boolean opening) {
        for (InteriorDoorBlockEntity door : doorsOf(tardisId)) {
            door.applySyncedDoor(state, animation, opening);
        }
        syncDoorControl(server, tardisId, state);
        TardisPortalBridge.onDoorsChanged(server, tardisId);
    }

    public static void onInteriorDoorChanged(MinecraftServer server, UUID tardisId, InteriorDoorBlockEntity source,
                                             DoorState state, String animation, boolean opening) {
        TardisExteriorBlockEntity exterior = exteriorOf(server, tardisId);
        if (exterior != null) {
            exterior.applySyncedDoor(state, animation, opening);
        }
        for (InteriorDoorBlockEntity door : doorsOf(tardisId)) {
            if (door != source) door.applySyncedDoor(state, animation, opening);
        }
        syncDoorControl(server, tardisId, state);
        TardisPortalBridge.onDoorsChanged(server, tardisId);
    }

    public static void syncDoorControl(MinecraftServer server, UUID tardisId, DoorState state) {
        applyDoorControl(server, tardisId, state, false);
    }

    private static void applyDoorControl(MinecraftServer server, UUID tardisId, DoorState state, boolean silent) {
        applyControl(server, tardisId, GCTardisControls.DOOR_CONTROL, !state.isClosed(), silent);
    }

    private static void applyLockControl(MinecraftServer server, UUID tardisId, boolean locked, boolean silent) {
        applyControl(server, tardisId, GCTardisControls.DOOR_LOCK_CONTROL, locked, silent);
    }

    private static void applyControl(MinecraftServer server, UUID tardisId, String controlId,
                                     boolean activated, boolean silent) {
        TardisApi.get(server, tardisId).flatMap(Tardis::console).ifPresent(console -> {
            if (silent) {
                console.reconcileControl(controlId, activated);
            } else {
                console.setControlActivated(controlId, activated);
            }
        });
    }

    public static void onLockChanged(MinecraftServer server, UUID tardisId, boolean locked, boolean fromConsole) {
        for (InteriorDoorBlockEntity door : doorsOf(tardisId)) {
            door.applySyncedLock(locked);
        }
        if (!fromConsole) {
            applyLockControl(server, tardisId, locked, false);
        }
    }

    public static void announceSealed(UUID tardisId) {
        for (InteriorDoorBlockEntity door : doorsOf(tardisId)) {
            door.announceLocked();
        }
    }

    public static void toggleAllDoors(MinecraftServer server, UUID tardisId) {
        TardisExteriorBlockEntity exterior = exteriorOf(server, tardisId);
        if (exterior != null) {
            exterior.interact(true, true);
            syncDoorControl(server, tardisId, exterior.getDoorState());
            return;
        }
        List<InteriorDoorBlockEntity> doors = doorsOf(tardisId);
        if (doors.isEmpty()) return;
        doors.getFirst().interact(true);
        syncDoorControl(server, tardisId, doors.getFirst().getDoorState());
    }

    public static void reconcileConsole(MinecraftServer server, UUID tardisId) {
        TardisExteriorBlockEntity exterior = exteriorOf(server, tardisId);
        if (exterior == null) return;
        applyDoorControl(server, tardisId, exterior.getDoorState(), true);
        applyLockControl(server, tardisId, exterior.isLocked(), true);
        InteriorDoorBlockEntity primary = primaryDoor(tardisId);
        if (primary != null) {
            applyControl(server, tardisId, GCTardisControls.INTERIOR_DOOR_CONTROL, !primary.getDoorState().isClosed(), true);
        }
    }

    public static void pullFromExterior(MinecraftServer server, UUID tardisId, InteriorDoorBlockEntity door) {
        TardisExteriorBlockEntity exterior = exteriorOf(server, tardisId);
        if (exterior != null) {
            door.snapTo(exterior.getDoorState());
            applyDoorControl(server, tardisId, exterior.getDoorState(), true);
            applyLockControl(server, tardisId, exterior.isLocked(), true);
        }
    }
}