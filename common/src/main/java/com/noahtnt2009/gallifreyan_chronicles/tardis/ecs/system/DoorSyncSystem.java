package com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.InteriorDoorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.DoorState;
import com.noahtnt2009.gallifreyan_chronicles.tardis.interior.TardisDimensionManager;
import com.noahtnt2009.gallifreyan_chronicles.tardis.manager.TardisManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
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
    private static final String INTERIOR_PREFIX = "tardis/";
    private static final String HANDBRAKE_ID = "handbrake";
    private static final Map<UUID, Set<InteriorDoorBlockEntity>> INTERIOR_DOORS = new HashMap<>();

    private DoorSyncSystem() {
    }

    public static @Nullable UUID tardisIdOf(Level level) {
        Identifier dimension = level.dimension().identifier();
        if (!dimension.getNamespace().equals(Constants.MOD_ID)) return null;
        String path = dimension.getPath();
        if (!path.startsWith(INTERIOR_PREFIX)) return null;
        try {
            return UUID.fromString(path.substring(INTERIOR_PREFIX.length()));
        } catch (IllegalArgumentException e) {
            return null;
        }
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

    private static synchronized List<InteriorDoorBlockEntity> doorsOf(UUID tardisId) {
        Set<InteriorDoorBlockEntity> doors = INTERIOR_DOORS.get(tardisId);
        if (doors == null) return List.of();
        List<InteriorDoorBlockEntity> live = new ArrayList<>();
        for (InteriorDoorBlockEntity door : doors) {
            if (!door.isRemoved()) live.add(door);
        }
        return live;
    }

    public static @Nullable TardisExteriorBlockEntity findExterior(MinecraftServer server, UUID tardisId) {
        TardisManager manager = TardisManager.get(server);
        BlockPos pos = manager.resolveExteriorBlockPos(tardisId);
        if (pos == null) return null;
        ServerLevel level = server.getLevel(manager.getExteriorDimension(tardisId));
        if (level == null) return null;
        level.getChunkAt(pos);
        return level.getBlockEntity(pos) instanceof TardisExteriorBlockEntity exterior ? exterior : null;
    }

    public static void exteriorChanged(MinecraftServer server, UUID tardisId, DoorState state,
                                       String animation, boolean opening) {
        for (InteriorDoorBlockEntity door : doorsOf(tardisId)) {
            door.applySyncedDoor(state, animation, opening);
        }
        syncHandbrake(server, tardisId, state);
    }

    public static void interiorChanged(MinecraftServer server, UUID tardisId, InteriorDoorBlockEntity source,
                                       DoorState state, String animation, boolean opening) {
        TardisExteriorBlockEntity exterior = findExterior(server, tardisId);
        if (exterior != null) {
            exterior.applySyncedDoor(state, animation, opening);
        }
        for (InteriorDoorBlockEntity door : doorsOf(tardisId)) {
            if (door != source) door.applySyncedDoor(state, animation, opening);
        }
        syncHandbrake(server, tardisId, state);
    }

    public static void syncHandbrake(MinecraftServer server, UUID tardisId, DoorState state) {
        applyHandbrake(server, tardisId, state, false);
    }

    private static void applyHandbrake(MinecraftServer server, UUID tardisId, DoorState state, boolean silent) {
        ServerLevel interior = TardisDimensionManager.get(server, tardisId);
        if (interior == null) return;
        BlockPos pos = TardisManager.get(server).getConsoleBlockPos(tardisId);
        if (pos == null) return;
        interior.getChunkAt(pos);
        if (interior.getBlockEntity(pos) instanceof TardisConsoleBlockEntity console) {
            boolean activated = !state.isClosed();
            if (silent) {
                console.reconcileControl(HANDBRAKE_ID, activated);
            } else {
                console.setControlActivated(HANDBRAKE_ID, activated);
            }
        }
    }

    public static void toggleAll(MinecraftServer server, UUID tardisId) {
        TardisExteriorBlockEntity exterior = findExterior(server, tardisId);
        if (exterior != null) {
            exterior.interact(true, true);
            syncHandbrake(server, tardisId, exterior.getDoorState());
            return;
        }
        List<InteriorDoorBlockEntity> doors = doorsOf(tardisId);
        if (doors.isEmpty()) return;
        doors.getFirst().interact(true);
        syncHandbrake(server, tardisId, doors.getFirst().getDoorState());
    }

    public static void pullFromExterior(MinecraftServer server, UUID tardisId, InteriorDoorBlockEntity door) {
        TardisExteriorBlockEntity exterior = findExterior(server, tardisId);
        if (exterior != null) {
            door.snapTo(exterior.getDoorState());
            applyHandbrake(server, tardisId, exterior.getDoorState(), true);
        }
    }
}