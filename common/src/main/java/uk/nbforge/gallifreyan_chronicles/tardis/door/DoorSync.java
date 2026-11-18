package uk.nbforge.gallifreyan_chronicles.tardis.door;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.compat.portal.TardisPortalBridge;
import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.InteriorDoorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.lifecycle.ServerState;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;

public final class DoorSync {
    private static final Map<UUID, Set<InteriorDoorBlockEntity>> INTERIOR_DOORS = ServerState.track(new HashMap<>());

    private DoorSync() {}

    public static synchronized void clear() {
        INTERIOR_DOORS.clear();
    }

    public static synchronized void register(UUID tardisId, InteriorDoorBlockEntity door) {
        INTERIOR_DOORS
                .computeIfAbsent(tardisId, k -> Collections.newSetFromMap(new WeakHashMap<>()))
                .add(door);
    }

    public static synchronized void unregister(UUID tardisId, InteriorDoorBlockEntity door) {
        Set<InteriorDoorBlockEntity> doors = INTERIOR_DOORS.get(tardisId);
        if (doors == null) return;
        doors.remove(door);
        if (doors.isEmpty()) INTERIOR_DOORS.remove(tardisId);
    }

    /** Use {@link Tardis#interiorDoors()}, this is what it reads. */
    public static synchronized List<InteriorDoorBlockEntity> doorsOf(UUID tardisId) {
        Set<InteriorDoorBlockEntity> doors = INTERIOR_DOORS.get(tardisId);
        if (doors == null) return List.of();
        List<InteriorDoorBlockEntity> live = new ArrayList<>();
        for (InteriorDoorBlockEntity door : doors) {
            if (!door.isRemoved()) live.add(door);
        }
        return live;
    }

    /** Use {@link Tardis#primaryInteriorDoor()}, this is what it reads. */
    public static @Nullable InteriorDoorBlockEntity primaryDoor(UUID tardisId) {
        for (InteriorDoorBlockEntity door : doorsOf(tardisId)) {
            if (door.isPrimary()) return door;
        }
        return null;
    }

    public static void syncInteriorDoorControl(Tardis tardis, DoorState state) {
        applyControl(tardis, GCTardisControls.INTERIOR_DOOR_CONTROL, !state.isClosed(), false);
    }

    /** Quietly keeps the interior door switch of the console matching the real state of the primary door. */
    public static void reconcileInteriorDoorControl(Tardis tardis, DoorState state) {
        applyControl(tardis, GCTardisControls.INTERIOR_DOOR_CONTROL, !state.isClosed(), true);
    }

    public static void onExteriorDoorChanged(Tardis tardis, DoorState state, String animation, boolean opening) {
        for (InteriorDoorBlockEntity door : tardis.interiorDoors()) {
            door.applySyncedDoor(state, animation, opening);
        }
        syncDoorControl(tardis, state);
        TardisPortalBridge.onDoorsChanged(tardis);
    }

    public static void onInteriorDoorChanged(
            Tardis tardis, InteriorDoorBlockEntity source, DoorState state, String animation, boolean opening) {
        tardis.exterior().ifPresent(exterior -> exterior.doors().applySyncedDoor(state, animation, opening));
        for (InteriorDoorBlockEntity door : tardis.interiorDoors()) {
            if (door != source) door.applySyncedDoor(state, animation, opening);
        }
        syncDoorControl(tardis, state);
        TardisPortalBridge.onDoorsChanged(tardis);
    }

    public static void syncDoorControl(Tardis tardis, DoorState state) {
        applyDoorControl(tardis, state, false);
    }

    private static void applyDoorControl(Tardis tardis, DoorState state, boolean silent) {
        applyControl(tardis, GCTardisControls.DOOR_CONTROL, !state.isClosed(), silent);
    }

    private static void applyLockControl(Tardis tardis, boolean locked, boolean silent) {
        applyControl(tardis, GCTardisControls.DOOR_LOCK_CONTROL, locked, silent);
    }

    private static void applyControl(Tardis tardis, String controlId, boolean activated, boolean silent) {
        tardis.console().ifPresent(console -> {
            if (silent) {
                console.reconcileControl(controlId, activated);
            } else {
                console.setControlActivated(controlId, activated);
            }
        });
    }

    public static void onLockChanged(Tardis tardis, boolean locked, boolean fromConsole) {
        for (InteriorDoorBlockEntity door : tardis.interiorDoors()) {
            door.applySyncedLock(locked);
        }
        if (!fromConsole) {
            applyLockControl(tardis, locked, false);
        }
    }

    public static void announceSealed(Tardis tardis) {
        for (InteriorDoorBlockEntity door : tardis.interiorDoors()) {
            door.announceLocked();
        }
    }

    public static void toggleAllDoors(Tardis tardis) {
        TardisExteriorBlockEntity exterior = tardis.exterior().orElse(null);
        if (exterior != null) {
            exterior.doors().interact(true, true);
            syncDoorControl(tardis, exterior.doors().getState());
            return;
        }
        List<InteriorDoorBlockEntity> doors = tardis.interiorDoors();
        if (doors.isEmpty()) return;
        doors.getFirst().interact(true);
        syncDoorControl(tardis, doors.getFirst().getDoorState());
    }

    public static void reconcileConsole(Tardis tardis) {
        TardisExteriorBlockEntity exterior = tardis.exterior().orElse(null);
        if (exterior == null) return;
        applyDoorControl(tardis, exterior.doors().getState(), true);
        applyLockControl(tardis, exterior.lock().isLocked(), true);
        tardis.primaryInteriorDoor()
                .ifPresent(primary -> applyControl(
                        tardis,
                        GCTardisControls.INTERIOR_DOOR_CONTROL,
                        !primary.getDoorState().isClosed(),
                        true));
    }

    public static void pullFromExterior(Tardis tardis, InteriorDoorBlockEntity door) {
        tardis.exterior().ifPresent(exterior -> {
            door.snapTo(exterior.doors().getState());
            applyDoorControl(tardis, exterior.doors().getState(), true);
            applyLockControl(tardis, exterior.lock().isLocked(), true);
        });
    }
}
