package com.noahtnt2009.gallifreyan_chronicles.compat.portal;

import com.noahtnt2009.gallifreyan_chronicles.block.InteriorDoorBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.InteriorDoorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisApi;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.DoorSyncSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import com.noahtnt2009.gallifreyan_chronicles.tardis.interior.TardisInteriorSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.manager.TardisManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import qouteall.imm_ptl.core.api.PortalAPI;
import qouteall.imm_ptl.core.block_manipulation.BlockManipulationServer;
import qouteall.imm_ptl.core.chunk_loading.ImmPtlChunkTickets;
import qouteall.imm_ptl.core.chunk_loading.ImmPtlChunkTracking;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.q_misc_util.dimension.DimensionIntId;
import qouteall.q_misc_util.my_util.DQuaternion;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class TardisPortals {
    public static final String PORTAL_TAG = TardisPortalBridge.PORTAL_TAG;

    private static final double PORTAL_WIDTH = 1.0;
    private static final double PORTAL_HEIGHT = 34.0 / 16.0;
    private static final Vec3 EXTERIOR_PORTAL_OFFSET = new Vec3(0.0, 18.0 / 16.0, -10.05 / 16.0);
    private static final Vec3 INTERIOR_PORTAL_OFFSET = new Vec3(0.0, 1.0, 8.05 / 16.0);
    private static final double INTERIOR_VIEW_Z = 7.95 / 16.0;
    private static final Vec3 UP = new Vec3(0.0, 1.0, 0.0);
    private static final Vec3 DOWN = new Vec3(0.0, -1.0, 0.0);
    private static final double FLUSH_DOOR_HEIGHT = 0.75;
    private static final double FLUSH_PORTAL_Z = 0.253;
    private static final double FLUSH_VIEW_Z = 0.247;
    private static final int CLOSE_LINGER_TICKS = 12;
    private static final int PURGE_INTERVAL_TICKS = 20;
    private static final double PURGE_RADIUS = 2.0;
    private static final int DIMENSION_CHECK_INTERVAL_TICKS = 20;

    private static final int TICKET_REFRESH_TICKS = 100;
    private static final int TICKET_RADIUS = 2;
    private static final int SCAN_RETRY_TICKS = 40;
    private static final int MISSING_GRACE_TICKS = 100;
    private static final double EXTERIOR_PLAYER_RANGE = 64.0;

    private static final Map<UUID, Link> LINKS = new HashMap<>();
    private static final Map<UUID, DoorLocation> DOORS = new HashMap<>();
    private static final Map<UUID, Long> LAST_TICKET = new HashMap<>();
    private static final Map<UUID, Long> NEXT_SCAN = new HashMap<>();

    private record DoorLocation(ResourceKey<Level> dimension, BlockPos pos) {
    }

    private record Placement(ResourceKey<Level> exteriorDimension, BlockPos exteriorPos, float exteriorYaw,
                             ResourceKey<Level> interiorDimension, BlockPos doorPos, Direction doorFacing, boolean doorFlipped,
                             boolean doorFlush) {
    }

    private static final class Link {
        private final Placement placement;
        private final Portal exteriorPortal;
        private final Portal interiorPortal;
        private boolean visible;
        private boolean exteriorShown;
        private boolean interiorShown;
        private long hideAt = -1L;
        private long missingSince = -1L;

        private Link(Placement placement, Portal exteriorPortal, Portal interiorPortal) {
            this.placement = placement;
            this.exteriorPortal = exteriorPortal;
            this.interiorPortal = interiorPortal;
        }

        private boolean isAlive() {
            return !exteriorPortal.isRemoved() && !interiorPortal.isRemoved();
        }
    }

    private TardisPortals() {
    }

    static void reset() {
        LINKS.clear();
        DOORS.clear();
        LAST_TICKET.clear();
        NEXT_SCAN.clear();
    }

    static void removeTardis(UUID tardisId) {
        Link link = LINKS.remove(tardisId);
        if (link != null) discard(link);
        DOORS.remove(tardisId);
        LAST_TICKET.remove(tardisId);
        NEXT_SCAN.remove(tardisId);
    }

    static void beforeDimensionRemoved(ServerLevel level) {
        try {
            ImmPtlChunkTracking.onDimensionRemove(level);
        } catch (RuntimeException e) {
            Constants.LOG.error("[GC] Failed to clear portal chunk tracking for {}", level.dimension().identifier(), e);
        }
        try {
            ImmPtlChunkTickets.onDimensionRemove(level);
        } catch (RuntimeException e) {
            Constants.LOG.error("[GC] Failed to clear portal chunk tickets for {}", level.dimension().identifier(), e);
        }
    }

    static @Nullable ServerLevel redirectedActionLevel() {
        BlockManipulationServer.Context context = BlockManipulationServer.REDIRECT_CONTEXT.get();
        return context != null ? context.world() : null;
    }

    static void syncDimensionIds(MinecraftServer server) {
        try {
            DimensionIntId.onServerDimensionChanged(server);
        } catch (RuntimeException ignored) {
        }
    }

    private static boolean dimensionIdsMissing(MinecraftServer server) {
        try {
            var map = DimensionIntId.getServerMap(server);
            for (ResourceKey<Level> key : server.levelKeys()) {
                if (!map.containsDimId(key)) return true;
            }
            return false;
        } catch (RuntimeException e) {
            return false;
        }
    }

    static void onDoorsChanged(MinecraftServer server, UUID tardisId) {
        Tardis tardis = TardisApi.get(server, tardisId).orElse(null);
        if (tardis == null) return;
        tardis.exterior();
        if (!DOORS.containsKey(tardisId)) locateDoor(server, tardisId, true);
        DoorLocation door = DOORS.get(tardisId);
        if (door != null) {
            ServerLevel level = server.getLevel(door.dimension());
            if (level != null) level.getChunkAt(door.pos());
        }
        LAST_TICKET.remove(tardisId);
        tickTardis(server, tardisId);
    }

    static void tick(MinecraftServer server) {
        if (server.getTickCount() % DIMENSION_CHECK_INTERVAL_TICKS == 0 && dimensionIdsMissing(server)) {
            syncDimensionIds(server);
        }
        Set<UUID> ids = new HashSet<>(TardisManager.get(server).getAllIds());
        for (UUID tardisId : ids) {
            tickTardis(server, tardisId);
        }
        Iterator<Map.Entry<UUID, Link>> iterator = LINKS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Link> entry = iterator.next();
            if (ids.contains(entry.getKey())) continue;
            discard(entry.getValue());
            TardisPortalBridge.setActive(entry.getKey(), false);
            iterator.remove();
        }
        DOORS.keySet().retainAll(ids);
        LAST_TICKET.keySet().retainAll(ids);
        NEXT_SCAN.keySet().retainAll(ids);
    }

    private static void keepLinkedChunksLoaded(MinecraftServer server, UUID tardisId, Tardis tardis) {
        ServerLevel exteriorLevel = tardis.exteriorLevel().orElse(null);
        BlockPos exteriorPos = tardis.exteriorPos().orElse(null);
        DoorLocation door = DOORS.get(tardisId);
        ServerLevel interiorLevel = door != null ? server.getLevel(door.dimension()) : null;

        boolean playersInside = interiorLevel != null && !interiorLevel.players().isEmpty();
        boolean playersOutside = exteriorLevel != null && exteriorPos != null
                && exteriorLevel.getNearestPlayer(exteriorPos.getX() + 0.5, exteriorPos.getY(), exteriorPos.getZ() + 0.5,
                EXTERIOR_PLAYER_RANGE, false) != null;
        if (!playersInside && !playersOutside) return;

        long now = server.getTickCount();
        Long last = LAST_TICKET.get(tardisId);
        if (last != null && now - last < TICKET_REFRESH_TICKS) return;
        LAST_TICKET.put(tardisId, now);

        if (exteriorLevel != null && exteriorPos != null) {
            exteriorLevel.getChunkSource().addTicketWithRadius(TicketType.PORTAL, ChunkPos.containing(exteriorPos), TICKET_RADIUS);
        }
        if (interiorLevel != null) {
            interiorLevel.getChunkSource().addTicketWithRadius(TicketType.PORTAL, ChunkPos.containing(door.pos()), TICKET_RADIUS);
        }
    }

    /** @return true when the remembered interior door was confirmed destroyed (its chunk is loaded and the door is gone) */
    private static boolean forgetMissingDoor(MinecraftServer server, UUID tardisId) {
        DoorLocation door = DOORS.get(tardisId);
        if (door == null) return false;
        ServerLevel level = server.getLevel(door.dimension());
        if (level == null) {
            DOORS.remove(tardisId);
            return false;
        }
        if (!level.hasChunkAt(door.pos())) return false;
        BlockState state = level.getBlockState(door.pos());
        if (!state.is(GCBlocks.TARDIS_INTERIOR_DOOR) || !InteriorDoorBlock.isMaster(state)) {
            DOORS.remove(tardisId);
            NEXT_SCAN.remove(tardisId);
            return true;
        }
        return false;
    }

    private static void registerDoor(MinecraftServer server, @Nullable DoorLocation location) {
        if (location == null) return;
        ServerLevel level = server.getLevel(location.dimension());
        if (level == null || !level.hasChunkAt(location.pos())) return;
        if (level.getBlockEntity(location.pos()) instanceof InteriorDoorBlockEntity door) {
            door.serverTick();
        }
    }

    private static void locateDoor(MinecraftServer server, UUID tardisId, boolean force) {
        long now = server.getTickCount();
        Long next = NEXT_SCAN.get(tardisId);
        if (!force && next != null && now < next) return;
        NEXT_SCAN.put(tardisId, now + SCAN_RETRY_TICKS);

        ServerLevel interior = TardisInteriorSystem.prepareInterior(server, tardisId);
        if (interior == null) return;
        BlockPos pos = TardisInteriorSystem.findInteriorDoor(interior, tardisId);
        if (pos == null) {
            Constants.LOG.warn("[GC] No TARDIS interior door found in the interior of {}, portals disabled", tardisId);
            return;
        }
        DOORS.put(tardisId, new DoorLocation(interior.dimension(), pos));
        LAST_TICKET.remove(tardisId);
    }

    static void onExteriorPlaced(MinecraftServer server, UUID tardisId) {
        locateDoor(server, tardisId, true);
        LAST_TICKET.remove(tardisId);
        tickTardis(server, tardisId);
    }

    private static void tickTardis(MinecraftServer server, UUID tardisId) {
        Tardis tardis = TardisApi.get(server, tardisId).orElse(null);
        InteriorDoorBlockEntity door = findDoor(tardisId);
        if (door != null && door.getLevel() instanceof ServerLevel doorLevel) {
            DOORS.put(tardisId, new DoorLocation(doorLevel.dimension(), door.getBlockPos()));
        }
        boolean doorDestroyed = door == null && forgetMissingDoor(server, tardisId);
        TardisExteriorBlockEntity exterior = tardis != null ? tardis.exteriorIfLoaded().orElse(null) : null;
        if (exterior != null && !DOORS.containsKey(tardisId)) {
            locateDoor(server, tardisId, false);
        }
        if (tardis != null) {
            keepLinkedChunksLoaded(server, tardisId, tardis);
        }
        if (door == null) {
            registerDoor(server, DOORS.get(tardisId));
        }
        Placement placement = placementOf(server, exterior, DOORS.get(tardisId));
        Link link = LINKS.get(tardisId);

        if (placement == null) {
            boolean definitelyGone = doorDestroyed && !DOORS.containsKey(tardisId)
                    || exterior != null && (exterior.isTravelling() || exterior.isFullyInvisible());
            if (link != null && !definitelyGone && link.isAlive()) {
                long now = server.getTickCount();
                if (link.missingSince < 0L) link.missingSince = now;
                if (now - link.missingSince < MISSING_GRACE_TICKS) return;
            }
            if (link != null) {
                discard(link);
                LINKS.remove(tardisId);
            }
            TardisPortalBridge.setActive(tardisId, false);
            if (exterior != null) exterior.setPortalActive(false);
            return;
        }
        if (link != null) link.missingSince = -1L;

        if (link == null || !link.isAlive() || !link.placement.equals(placement)) {
            if (link != null) discard(link);
            link = spawn(server, tardisId, placement);
            if (link == null) {
                LINKS.remove(tardisId);
                TardisPortalBridge.setActive(tardisId, false);
                exterior.setPortalActive(false);
                return;
            }
            LINKS.put(tardisId, link);
            Constants.LOG.info("[GC] Linked TARDIS {} doors with portals: exterior {} in {}, interior door {} in {}",
                    tardisId, placement.exteriorPos(), placement.exteriorDimension().identifier(),
                    placement.doorPos(), placement.interiorDimension().identifier());
        }

        long now = server.getTickCount();
        boolean open = !exterior.getDoorState().isClosed();
        if (open) {
            link.hideAt = -1L;
        } else if (link.visible && link.hideAt < 0L) {
            link.hideAt = now + CLOSE_LINGER_TICKS;
        }
        boolean visible = open || (link.hideAt >= 0L && now < link.hideAt);
        link.visible = visible;

        boolean exteriorShown = visible;
        if (exteriorShown != link.exteriorShown) {
            link.exteriorShown = exteriorShown;
            applyVisibility(link.exteriorPortal, exteriorShown);
        }
        boolean interiorShown = visible;
        if (interiorShown != link.interiorShown) {
            link.interiorShown = interiorShown;
            applyVisibility(link.interiorPortal, interiorShown);
        }

        exterior.setPortalActive(visible);
        TardisPortalBridge.setActive(tardisId, true);

        if (now % PURGE_INTERVAL_TICKS == 0) {
            purgeStrays(link.exteriorPortal, tardisId);
            purgeStrays(link.interiorPortal, tardisId);
        }
    }

    private static @Nullable InteriorDoorBlockEntity findDoor(UUID tardisId) {
        return DoorSyncSystem.doorsOf(tardisId).stream()
                .filter(door -> door.isLinked() && door.getBlockState().is(GCBlocks.TARDIS_INTERIOR_DOOR))
                .filter(door -> door.getLevel() instanceof ServerLevel)
                .min(Comparator.comparingLong(door -> door.getBlockPos().asLong()))
                .orElse(null);
    }

    private static @Nullable Placement placementOf(MinecraftServer server, @Nullable TardisExteriorBlockEntity exterior,
                                                   @Nullable DoorLocation door) {
        if (exterior == null || door == null) return null;
        if (!(exterior.getLevel() instanceof ServerLevel exteriorLevel)) return null;
        if (exterior.isTravelling() || exterior.isFullyInvisible()) return null;
        ServerLevel interiorLevel = server.getLevel(door.dimension());
        if (interiorLevel == null || !interiorLevel.hasChunkAt(door.pos())) return null;
        BlockState state = interiorLevel.getBlockState(door.pos());
        if (!state.is(GCBlocks.TARDIS_INTERIOR_DOOR) || !InteriorDoorBlock.isMaster(state)) return null;
        return new Placement(exteriorLevel.dimension(), exterior.getBlockPos(), exterior.getYaw(),
                interiorLevel.dimension(), door.pos(), state.getValue(InteriorDoorBlock.FACING),
                InteriorDoorBlock.isFlipped(state), InteriorDoorBlock.isFlush(state));
    }

    private static @Nullable Link spawn(MinecraftServer server, UUID tardisId, Placement placement) {
        ServerLevel exteriorLevel = server.getLevel(placement.exteriorDimension());
        ServerLevel interiorLevel = server.getLevel(placement.interiorDimension());
        if (exteriorLevel == null || interiorLevel == null) return null;

        float exteriorRotation = TardisExterior.modelRotationDegrees(placement.exteriorYaw());
        float interiorRotation = TardisExterior.modelRotationDegrees(placement.doorFacing().toYRot());

        boolean flipped = placement.doorFlipped();
        boolean flush = placement.doorFlush();
        Direction right = InteriorDoorBlock.rightDirectionOf(placement.doorFacing(), flipped);
        Direction facing = placement.doorFacing();

        Vec3 exteriorCenter = Vec3.atBottomCenterOf(placement.exteriorPos());
        Vec3 interiorCenter;
        double portalZ;
        double viewZ;
        Vec3 interiorPortalOffset;
        if (flush) {
            // lying flat against the ceiling, the model's origin is the middle of its bottom edge, tipped so that
            // its local up runs along the facing direction and its back points at the ceiling
            interiorCenter = Vec3.atBottomCenterOf(placement.doorPos())
                    .add(right.getStepX() * 0.5 - facing.getStepX() * 0.5, FLUSH_DOOR_HEIGHT,
                            right.getStepZ() * 0.5 - facing.getStepZ() * 0.5);
            portalZ = FLUSH_PORTAL_Z;
            viewZ = FLUSH_VIEW_Z;
            interiorPortalOffset = new Vec3(0.0, INTERIOR_PORTAL_OFFSET.y, portalZ);
        } else {
            interiorCenter = Vec3.atBottomCenterOf(placement.doorPos())
                    .add(right.getStepX() * 0.5, 0.0, right.getStepZ() * 0.5);
            portalZ = INTERIOR_PORTAL_OFFSET.z;
            viewZ = INTERIOR_VIEW_Z;
            // a door hanging from a ceiling has its master block on top, so the doorway mirrors downwards from it
            interiorPortalOffset = flipped ? flipY(INTERIOR_PORTAL_OFFSET) : INTERIOR_PORTAL_OFFSET;
        }

        Vec3 origin = exteriorCenter.add(rotate(EXTERIOR_PORTAL_OFFSET, exteriorRotation));
        Vec3 destination = interiorCenter.add(doorOffset(interiorPortalOffset, interiorRotation, flush));
        Vec3 normal = rotate(new Vec3(0.0, 0.0, -1.0), exteriorRotation);
        Vec3 axisW = UP.cross(normal);

        // portal frames: right, up, normal, with right = up x normal
        Vec3[] exteriorFrame = {axisW, UP, normal};
        Vec3 interiorFront = rotate(new Vec3(0.0, 0.0, -1.0), interiorRotation);
        Vec3[] interiorFrame;
        if (flush) {
            interiorFrame = frame(interiorFront, DOWN);
        } else {
            interiorFrame = frame(UP.scale(flipped ? -1.0 : 1.0), interiorFront);
        }

        Portal exteriorPortal = Portal.ENTITY_TYPE.create(exteriorLevel, EntitySpawnReason.TRIGGERED);
        if (exteriorPortal == null) return null;
        exteriorPortal.setOriginPos(origin);
        exteriorPortal.setOrientationAndSize(axisW, UP, PORTAL_WIDTH, PORTAL_HEIGHT);
        PortalAPI.setPortalTransformation(exteriorPortal, placement.interiorDimension(), destination,
                flipped || flush
                        ? transformation(exteriorFrame, interiorFrame)
                        : DQuaternion.rotationByDegrees(UP, interiorRotation + 180.0 - exteriorRotation), 1.0);
        configure(exteriorPortal, tardisId);

        Vec3 interiorViewOffset = new Vec3(0.0, interiorPortalOffset.y, viewZ);
        Vec3 exteriorReturnOffset = new Vec3(0.0,
                EXTERIOR_PORTAL_OFFSET.y + (interiorViewOffset.y - interiorPortalOffset.y),
                EXTERIOR_PORTAL_OFFSET.z + portalZ - viewZ);
        Vec3 interiorOrigin = interiorCenter.add(doorOffset(interiorViewOffset, interiorRotation, flush));
        Vec3 interiorDestination = exteriorCenter.add(rotate(exteriorReturnOffset, exteriorRotation));

        Portal interiorPortal = Portal.ENTITY_TYPE.create(interiorLevel, EntitySpawnReason.TRIGGERED);
        if (interiorPortal == null) return null;
        interiorPortal.setOriginPos(interiorOrigin);
        // the portal quad is symmetric, only its centre and normal matter, a hanging door is turned by its transformation
        interiorPortal.setOrientationAndSize(interiorFrame[0], interiorFrame[1], PORTAL_WIDTH, PORTAL_HEIGHT);
        PortalAPI.setPortalTransformation(interiorPortal, placement.exteriorDimension(), interiorDestination,
                flipped || flush
                        ? transformation(interiorFrame, exteriorFrame)
                        : DQuaternion.rotationByDegrees(UP, exteriorRotation + 180.0 - interiorRotation), 1.0);
        configure(interiorPortal, tardisId);

        purgeNear(exteriorLevel, origin, null, tardisId);
        purgeNear(interiorLevel, interiorOrigin, null, tardisId);

        if (!exteriorLevel.addFreshEntity(exteriorPortal)) return null;
        if (!interiorLevel.addFreshEntity(interiorPortal)) {
            exteriorPortal.discard();
            return null;
        }
        return new Link(placement, exteriorPortal, interiorPortal);
    }

    private static String ownerTag(UUID tardisId) {
        return PORTAL_TAG + "." + tardisId;
    }

    private static boolean belongsTo(Portal portal, UUID tardisId) {
        Set<String> tags = portal.entityTags();
        if (!tags.contains(PORTAL_TAG)) return false;
        if (tags.contains(ownerTag(tardisId))) return true;
        for (String tag : tags) {
            if (tag.startsWith(PORTAL_TAG + ".")) return false;
        }
        return true;
    }

    private static void configure(Portal portal, UUID tardisId) {
        portal.addTag(PORTAL_TAG);
        portal.addTag(ownerTag(tardisId));
        portal.setIsVisible(false);
        portal.setTeleportable(false);
    }

    private static void applyVisibility(Portal portal, boolean visible) {
        if (portal.isRemoved()) return;
        portal.setIsVisible(visible);
        portal.setTeleportable(visible);
        portal.reloadAndSyncToClient();
    }

    private static void discard(Link link) {
        if (!link.exteriorPortal.isRemoved()) link.exteriorPortal.discard();
        if (!link.interiorPortal.isRemoved()) link.interiorPortal.discard();
    }

    private static void purgeStrays(Portal keep, UUID tardisId) {
        if (keep.isRemoved() || !(keep.level() instanceof ServerLevel level)) return;
        purgeNear(level, keep.getOriginPos(), keep, tardisId);
    }

    private static void purgeNear(ServerLevel level, Vec3 center, @Nullable Portal keep, UUID tardisId) {
        AABB area = new AABB(center, center).inflate(PURGE_RADIUS);
        for (Portal stray : level.getEntitiesOfClass(Portal.class, area,
                portal -> portal != keep && belongsTo(portal, tardisId))) {
            stray.discard();
        }
    }

    private static Vec3 flipY(Vec3 offset) {
        return new Vec3(offset.x, 1.0 - offset.y, offset.z);
    }

    // an offset in the door model's own space (x right, y up, z towards the back) as a world offset, a door lying
    // against the ceiling is first tipped over so its up points along its facing and its back points up
    private static Vec3 doorOffset(Vec3 local, float rotation, boolean flush) {
        Vec3 tipped = flush ? new Vec3(local.x, local.z, -local.y) : local;
        return rotate(tipped, rotation);
    }

    // frame of a portal from its up and its front normal, right = up x normal
    private static Vec3[] frame(Vec3 up, Vec3 normal) {
        return new Vec3[]{up.cross(normal), up, normal};
    }

    /**
     * The turn a portal applies to what is seen through it. Going in through the source portal's front you come out
     * of the destination portal's front: normal -> -normal, up -> up, right -> -right. Written for any frames so that
     * doors that are upside down or lie against a ceiling come out right too. Frames are {right, up, normal}.
     */
    private static DQuaternion transformation(Vec3[] source, Vec3[] destination) {
        Vec3[] target = {destination[0].scale(-1.0), destination[1], destination[2].scale(-1.0)};
        double[][] m = new double[3][3];
        for (int k = 0; k < 3; k++) {
            double[] d = {target[k].x, target[k].y, target[k].z};
            double[] sv = {source[k].x, source[k].y, source[k].z};
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) m[i][j] += d[i] * sv[j];
            }
        }
        double cos = Math.max(-1.0, Math.min(1.0, (m[0][0] + m[1][1] + m[2][2] - 1.0) / 2.0));
        double angle = Math.acos(cos);
        if (angle < 1.0e-6) return DQuaternion.rotationByDegrees(UP, 0.0);
        Vec3 axis;
        if (Math.PI - angle < 1.0e-6) {
            // half turn: the axis is the column of (m + 1) / 2 with the biggest diagonal
            int best = 0;
            for (int i = 1; i < 3; i++) if (m[i][i] > m[best][best]) best = i;
            double x = (m[0][best] + (best == 0 ? 1.0 : 0.0)) / 2.0;
            double y = (m[1][best] + (best == 1 ? 1.0 : 0.0)) / 2.0;
            double z = (m[2][best] + (best == 2 ? 1.0 : 0.0)) / 2.0;
            axis = new Vec3(x, y, z).normalize();
        } else {
            axis = new Vec3(m[2][1] - m[1][2], m[0][2] - m[2][0], m[1][0] - m[0][1]).normalize();
        }
        return DQuaternion.rotationByDegrees(axis, Math.toDegrees(angle));
    }

    private static Vec3 rotate(Vec3 offset, float degrees) {
        double radians = Math.toRadians(degrees);
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);
        return new Vec3(offset.x * cos + offset.z * sin, offset.y, -offset.x * sin + offset.z * cos);
    }
}
