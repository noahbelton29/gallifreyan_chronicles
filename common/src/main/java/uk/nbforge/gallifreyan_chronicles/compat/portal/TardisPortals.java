package uk.nbforge.gallifreyan_chronicles.compat.portal;

import uk.nbforge.gallifreyan_chronicles.core.block.InteriorDoorBlock;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.InteriorDoorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.GCBlocks;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisApi;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.DoorSyncSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisExterior;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.TardisInteriorSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisManager;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
    private static final double PORTAL_GAP = 0.05 / 16.0;
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
    private static final int FULL_TICK_INTERVAL_TICKS = 10;
    private static final Set<UUID> HOT = new HashSet<>();

    private record DoorLocation(ResourceKey<Level> dimension, BlockPos pos) {
    }

    private record Placement(ResourceKey<Level> exteriorDimension, BlockPos exteriorPos, float exteriorYaw,
                             ResourceKey<Level> interiorDimension, BlockPos doorPos, Direction doorFacing,
                             boolean doorFlipped,
                             boolean doorFlush, boolean doorBackBlocked,
                             String exteriorModel, String doorModel) {
    }

    private static final class Link {
        private final Placement placement;
        private final Portal exteriorPortal;
        private final Portal interiorPortal;
        private final @Nullable Portal arrivalPortal;
        private boolean visible;
        private boolean exteriorShown;
        private boolean interiorShown;
        private long hideAt = -1L;
        private long missingSince = -1L;

        private Link(Placement placement, Portal exteriorPortal, Portal interiorPortal, @Nullable Portal arrivalPortal) {
            this.placement = placement;
            this.exteriorPortal = exteriorPortal;
            this.interiorPortal = interiorPortal;
            this.arrivalPortal = arrivalPortal;
        }

        private boolean isAlive() {
            return !exteriorPortal.isRemoved() && !interiorPortal.isRemoved()
                    && (arrivalPortal == null || !arrivalPortal.isRemoved());
        }
    }

    private TardisPortals() {
    }

    static void reset() {
        PortalBone.clearCache();
        LINKS.clear();
        DOORS.clear();
        LAST_TICKET.clear();
        NEXT_SCAN.clear();
        HOT.clear();
    }

    static void disableAll() {
        if (LINKS.isEmpty() && HOT.isEmpty()) return;
        for (Map.Entry<UUID, Link> entry : LINKS.entrySet()) {
            discard(entry.getValue());
            TardisPortalBridge.setActive(entry.getKey(), false);
        }
        reset();
    }

    static void removeTardis(UUID tardisId) {
        Link link = LINKS.remove(tardisId);
        if (link != null) discard(link);
        DOORS.remove(tardisId);
        LAST_TICKET.remove(tardisId);
        NEXT_SCAN.remove(tardisId);
        HOT.remove(tardisId);
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
        long now = server.getTickCount();
        if (now % DIMENSION_CHECK_INTERVAL_TICKS == 0 && dimensionIdsMissing(server)) {
            syncDimensionIds(server);
        }
        if (now % FULL_TICK_INTERVAL_TICKS == 0) {
            fullTick(server);
            return;
        }
        for (UUID tardisId : new ArrayList<>(HOT)) {
            tickTardis(server, tardisId);
        }
    }

    private static void fullTick(MinecraftServer server) {
        Set<UUID> ids = new HashSet<>(TardisManager.get(server).getAllIds());
        HOT.clear();
        for (UUID tardisId : ids) {
            tickTardis(server, tardisId);
            if (isHot(server, tardisId)) HOT.add(tardisId);
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

    private static boolean isHot(MinecraftServer server, UUID tardisId) {
        Link link = LINKS.get(tardisId);
        if (link != null && (link.visible || link.hideAt >= 0L || link.exteriorShown || link.interiorShown)) {
            return true;
        }
        Tardis tardis = TardisApi.get(server, tardisId).orElse(null);
        if (tardis == null) return false;
        DoorLocation door = DOORS.get(tardisId);
        return hasPlayersNearby(tardis.exteriorLevel().orElse(null), tardis.exteriorPos().orElse(null),
                door != null ? server.getLevel(door.dimension()) : null);
    }

    private static boolean hasPlayersNearby(@Nullable ServerLevel exteriorLevel, @Nullable BlockPos exteriorPos,
                                            @Nullable ServerLevel interiorLevel) {
        if (interiorLevel != null && !interiorLevel.players().isEmpty()) return true;
        return exteriorLevel != null && exteriorPos != null
                && exteriorLevel.getNearestPlayer(exteriorPos.getX() + 0.5, exteriorPos.getY(), exteriorPos.getZ() + 0.5,
                EXTERIOR_PLAYER_RANGE, false) != null;
    }

    private static void keepLinkedChunksLoaded(MinecraftServer server, UUID tardisId, Tardis tardis) {
        ServerLevel exteriorLevel = tardis.exteriorLevel().orElse(null);
        BlockPos exteriorPos = tardis.exteriorPos().orElse(null);
        DoorLocation door = DOORS.get(tardisId);
        ServerLevel interiorLevel = door != null ? server.getLevel(door.dimension()) : null;

        if (!hasPlayersNearby(exteriorLevel, exteriorPos, interiorLevel)) return;

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
            applyVisibility(link.exteriorPortal, exteriorShown, exteriorShown && link.arrivalPortal == null);
            if (link.arrivalPortal != null) applyVisibility(link.arrivalPortal, false, exteriorShown);
        }
        boolean interiorShown = visible;
        if (interiorShown != link.interiorShown) {
            link.interiorShown = interiorShown;
            applyVisibility(link.interiorPortal, interiorShown, interiorShown);
        }

        exterior.setPortalActive(visible);
        TardisPortalBridge.setActive(tardisId, true);

        if (now % PURGE_INTERVAL_TICKS == 0) {
            purgeStrays(link.exteriorPortal, link.arrivalPortal, tardisId);
            purgeStrays(link.interiorPortal, null, tardisId);
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
        String doorModel = interiorLevel.getBlockEntity(door.pos()) instanceof InteriorDoorBlockEntity doorEntity
                ? doorEntity.getVariant() : "";
        return new Placement(exteriorLevel.dimension(), exterior.getBlockPos(), exterior.getYaw(),
                interiorLevel.dimension(), door.pos(), state.getValue(InteriorDoorBlock.FACING),
                InteriorDoorBlock.isFlipped(state), InteriorDoorBlock.isFlush(state),
                InteriorDoorBlock.isBackBlocked(interiorLevel, door.pos(), state),
                exterior.getExterior().id(), doorModel);
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

        /* portal bones from the models, old numbers if a model has none */
        PortalBone exteriorBone = PortalBone.of(placement.exteriorModel());
        PortalBone doorBone = PortalBone.of(placement.doorModel());
        double width = exteriorBone != null ? exteriorBone.width() : PORTAL_WIDTH;
        double height = exteriorBone != null ? exteriorBone.height() : PORTAL_HEIGHT;
        Vec3 exteriorOffset = exteriorBone != null
                ? new Vec3(exteriorBone.x(), exteriorBone.centerY(), exteriorBone.front() - PORTAL_GAP)
                : EXTERIOR_PORTAL_OFFSET;
        /* same size both ends, interior lines up on its bottom */
        Vec3 doorLocal = doorBone != null
                ? new Vec3(doorBone.x(), doorBone.bottom() + height / 2.0, doorBone.back() + PORTAL_GAP)
                : INTERIOR_PORTAL_OFFSET;
        double doorViewZ = doorBone != null ? doorBone.back() - PORTAL_GAP : INTERIOR_VIEW_Z;

        Vec3 exteriorCenter = Vec3.atBottomCenterOf(placement.exteriorPos());
        Vec3 interiorCenter;
        double portalZ;
        double viewZ;
        Vec3 interiorPortalOffset;
        Vec3 arrivalOffset;
        if (flush) {
            interiorCenter = Vec3.atBottomCenterOf(placement.doorPos())
                    .add(right.getStepX() * 0.5 - facing.getStepX() * 0.5, FLUSH_DOOR_HEIGHT,
                            right.getStepZ() * 0.5 - facing.getStepZ() * 0.5);
            portalZ = FLUSH_PORTAL_Z;
            viewZ = FLUSH_VIEW_Z;
            interiorPortalOffset = new Vec3(doorLocal.x, INTERIOR_PORTAL_OFFSET.y, portalZ);
            arrivalOffset = interiorPortalOffset;
        } else {
            interiorCenter = Vec3.atBottomCenterOf(placement.doorPos())
                    .add(right.getStepX() * 0.5, 0.0, right.getStepZ() * 0.5);
            portalZ = doorLocal.z;
            viewZ = doorViewZ;
            interiorPortalOffset = flipped ? flipY(doorLocal) : doorLocal;
            double shift = placement.doorBackBlocked() ? InteriorDoorBlock.WALL_ARRIVAL_SHIFT : 0.0;
            arrivalOffset = new Vec3(interiorPortalOffset.x, interiorPortalOffset.y, interiorPortalOffset.z - shift);
        }

        Vec3 origin = exteriorCenter.add(rotate(exteriorOffset, exteriorRotation));
        Vec3 destination = interiorCenter.add(doorOffset(interiorPortalOffset, interiorRotation, flush));
        Vec3 arrivalDestination = interiorCenter.add(doorOffset(arrivalOffset, interiorRotation, flush));
        Vec3 normal = rotate(new Vec3(0.0, 0.0, -1.0), exteriorRotation);
        Vec3 axisW = UP.cross(normal);

        Vec3[] exteriorFrame = {axisW, UP, normal};
        Vec3 interiorFront = rotate(new Vec3(0.0, 0.0, -1.0), interiorRotation);
        Vec3[] interiorFrame;
        if (flush) {
            interiorFrame = frame(interiorFront, DOWN);
        } else {
            interiorFrame = frame(UP.scale(flipped ? -1.0 : 1.0), interiorFront);
        }

        DQuaternion exteriorTurn = flipped || flush
                ? transformation(exteriorFrame, interiorFrame)
                : DQuaternion.rotationByDegrees(UP, interiorRotation + 180.0 - exteriorRotation);

        Portal exteriorPortal = Portal.ENTITY_TYPE.create(exteriorLevel, EntitySpawnReason.TRIGGERED);
        if (exteriorPortal == null) return null;
        exteriorPortal.setOriginPos(origin);
        exteriorPortal.setOrientationAndSize(axisW, UP, width, height);
        PortalAPI.setPortalTransformation(exteriorPortal, placement.interiorDimension(), destination, exteriorTurn, 1.0);
        configure(exteriorPortal, tardisId);

        Portal arrivalPortal = null;
        if (!arrivalDestination.equals(destination)) {
            arrivalPortal = Portal.ENTITY_TYPE.create(exteriorLevel, EntitySpawnReason.TRIGGERED);
            if (arrivalPortal == null) return null;
            arrivalPortal.setOriginPos(origin);
            arrivalPortal.setOrientationAndSize(axisW, UP, width, height);
            PortalAPI.setPortalTransformation(arrivalPortal, placement.interiorDimension(), arrivalDestination,
                    exteriorTurn, 1.0);
            configure(arrivalPortal, tardisId);
        }

        Vec3 interiorViewOffset = new Vec3(interiorPortalOffset.x, interiorPortalOffset.y, viewZ);
        Vec3 exteriorReturnOffset = new Vec3(exteriorOffset.x,
                exteriorOffset.y + (interiorViewOffset.y - interiorPortalOffset.y),
                exteriorOffset.z + portalZ - viewZ);
        Vec3 interiorOrigin = interiorCenter.add(doorOffset(interiorViewOffset, interiorRotation, flush));
        Vec3 interiorDestination = exteriorCenter.add(rotate(exteriorReturnOffset, exteriorRotation));

        Portal interiorPortal = Portal.ENTITY_TYPE.create(interiorLevel, EntitySpawnReason.TRIGGERED);
        if (interiorPortal == null) return null;
        interiorPortal.setOriginPos(interiorOrigin);
        interiorPortal.setOrientationAndSize(interiorFrame[0], interiorFrame[1], width, height);
        PortalAPI.setPortalTransformation(interiorPortal, placement.exteriorDimension(), interiorDestination,
                flipped || flush
                        ? transformation(interiorFrame, exteriorFrame)
                        : DQuaternion.rotationByDegrees(UP, exteriorRotation + 180.0 - interiorRotation), 1.0);
        configure(interiorPortal, tardisId);

        purgeNear(exteriorLevel, origin, null, null, tardisId);
        purgeNear(interiorLevel, interiorOrigin, null, null, tardisId);

        if (!exteriorLevel.addFreshEntity(exteriorPortal)) return null;
        if (arrivalPortal != null && !exteriorLevel.addFreshEntity(arrivalPortal)) {
            exteriorPortal.discard();
            return null;
        }
        if (!interiorLevel.addFreshEntity(interiorPortal)) {
            exteriorPortal.discard();
            if (arrivalPortal != null) arrivalPortal.discard();
            return null;
        }
        return new Link(placement, exteriorPortal, interiorPortal, arrivalPortal);
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
        portal.portalTag = PORTAL_TAG;
        portal.setIsVisible(false);
        portal.setTeleportable(false);
    }

    private static void applyVisibility(Portal portal, boolean visible, boolean teleportable) {
        if (portal.isRemoved()) return;
        portal.setIsVisible(visible);
        portal.setTeleportable(teleportable);
        portal.reloadAndSyncToClient();
    }

    private static void discard(Link link) {
        if (!link.exteriorPortal.isRemoved()) link.exteriorPortal.discard();
        if (!link.interiorPortal.isRemoved()) link.interiorPortal.discard();
        if (link.arrivalPortal != null && !link.arrivalPortal.isRemoved()) link.arrivalPortal.discard();
    }

    private static void purgeStrays(Portal keep, @Nullable Portal alsoKeep, UUID tardisId) {
        if (keep.isRemoved() || !(keep.level() instanceof ServerLevel level)) return;
        purgeNear(level, keep.getOriginPos(), keep, alsoKeep, tardisId);
    }

    private static void purgeNear(ServerLevel level, Vec3 center, @Nullable Portal keep, @Nullable Portal alsoKeep,
                                  UUID tardisId) {
        AABB area = new AABB(center, center).inflate(PURGE_RADIUS);
        for (Portal stray : level.getEntitiesOfClass(Portal.class, area,
                portal -> portal != keep && portal != alsoKeep && belongsTo(portal, tardisId))) {
            stray.discard();
        }
    }

    private static Vec3 flipY(Vec3 offset) {
        return new Vec3(offset.x, 1.0 - offset.y, offset.z);
    }

    private static Vec3 doorOffset(Vec3 local, float rotation, boolean flush) {
        Vec3 tipped = flush ? new Vec3(local.x, local.z, -local.y) : local;
        return rotate(tipped, rotation);
    }

    private static Vec3[] frame(Vec3 up, Vec3 normal) {
        return new Vec3[]{up.cross(normal), up, normal};
    }

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
        double cos = Math.clamp((m[0][0] + m[1][1] + m[2][2] - 1.0) / 2.0, -1.0, 1.0);
        double angle = Math.acos(cos);
        if (angle < 1.0e-6) return DQuaternion.rotationByDegrees(UP, 0.0);
        Vec3 axis;
        if (Math.PI - angle < 1.0e-6) {
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
