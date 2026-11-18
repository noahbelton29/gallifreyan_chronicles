package uk.nbforge.gallifreyan_chronicles.tardis.interior;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.compat.portal.TardisPortalBridge;
import uk.nbforge.gallifreyan_chronicles.core.block.InteriorDoorBlock;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.TardisBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.InteriorDoorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.TardisConsoleRegistry;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.TardisInteriorRegistry;
import uk.nbforge.gallifreyan_chronicles.core.lifecycle.ServerState;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisData;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisDimensionManager;
import uk.nbforge.gallifreyan_chronicles.tardis.console.ConsoleLink;
import uk.nbforge.gallifreyan_chronicles.tardis.console.TardisConsole;
import uk.nbforge.gallifreyan_chronicles.tardis.door.DoorState;
import uk.nbforge.gallifreyan_chronicles.tardis.door.DoorSync;
import uk.nbforge.gallifreyan_chronicles.util.GCLog;

public final class TardisInteriors {
    private static final double ZONE_NEAR = -0.5;
    private static final double ZONE_FAR = 0.3;
    private static final double ZONE_HALF_WIDTH = 0.5;
    private static final double ZONE_BOTTOM = -0.5;
    private static final double ZONE_TOP = 2.0;

    public static final BlockPos ORIGIN = new BlockPos(0, 64, 0);

    private static final double EXIT_DISTANCE = 2.0;
    private static final double ARRIVAL_DISTANCE = 1.0;

    private static final Vec3i FALLBACK_CLEAR_SIZE = new Vec3i(64, 64, 64);
    private static final int CLEAR_FLAGS =
            Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS;

    private static final Map<Identifier, BlockPos> DOOR_OFFSET_CACHE = ServerState.track(new ConcurrentHashMap<>());
    private static final Map<Identifier, BlockPos> CONSOLE_OFFSET_CACHE = ServerState.track(new ConcurrentHashMap<>());

    private record ConsoleCarryover(
            TardisConsole console, @Nullable String variant, CompoundTag controlStates) {}

    public enum ChangeResult {
        CHANGED,
        UNCHANGED,
        UNKNOWN_INTERIOR,
        MISSING_TEMPLATE
    }

    private TardisInteriors() {}

    public static void tickEntryZone(Level level, TardisExteriorBlockEntity exterior) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (exterior.doors().getState().isClosed() || exterior.lock().isLocked()) return;
        if (exterior.travel().isFullyInvisible()) return;

        UUID tardisId = exterior.getTardisId();
        if (tardisId == null) return;
        if (TardisPortalBridge.isActive(tardisId)) return;
        Tardis tardis = Tardis.get(serverLevel.getServer(), tardisId).orElse(null);
        if (tardis == null) return;

        BlockPos pos = exterior.getBlockPos();
        double yawRad = Math.toRadians(exterior.getYaw());
        double frontX = -Math.sin(yawRad);
        double frontZ = Math.cos(yawRad);

        AABB search = new AABB(pos).inflate(2.5, 2.5, 2.5);
        for (ServerPlayer player : serverLevel.getEntitiesOfClass(ServerPlayer.class, search)) {
            if (player.isSpectator()) continue;
            if (outsideEntryZone(player, pos, frontX, frontZ)) continue;
            TardisDimensionManager.requestEntry(tardis, player);
        }
    }

    private static boolean outsideEntryZone(ServerPlayer player, BlockPos pos, double frontX, double frontZ) {
        double dx = player.getX() - (pos.getX() + 0.5);
        double dz = player.getZ() - (pos.getZ() + 0.5);
        double dy = player.getY() - pos.getY();

        double forward = dx * frontX + dz * frontZ;
        double lateral = dx * frontZ - dz * frontX;

        return forward < ZONE_NEAR
                || forward > ZONE_FAR
                || Math.abs(lateral) > ZONE_HALF_WIDTH
                || dy < ZONE_BOTTOM
                || dy > ZONE_TOP;
    }

    public static void exitInterior(Tardis tardis, ServerPlayer player) {
        BlockPos exteriorPos = tardis.exteriorPos().orElse(null);
        if (exteriorPos == null) return;

        ServerLevel target = tardis.exteriorLevel().orElse(tardis.server().overworld());

        Vec3 spot = Vec3.atBottomCenterOf(exteriorPos).add(0.0, 1.0, 0.0);
        float yaw = 0.0f;

        Optional<TardisExteriorBlockEntity> exterior = tardis.exterior();
        if (exterior.isPresent()) {
            double yawRad = Math.toRadians(exterior.get().getYaw());
            spot = Vec3.atBottomCenterOf(exteriorPos)
                    .add(-Math.sin(yawRad) * EXIT_DISTANCE, 0.0, Math.cos(yawRad) * EXIT_DISTANCE);
            yaw = exterior.get().getYaw();
        }

        clearArriving(player);
        player.teleportTo(target, spot.x, spot.y, spot.z, Set.of(), yaw, 0.0f, true);
    }

    public record Arrival(Vec3 pos, float yaw) {}

    private static final Set<UUID> ARRIVING = ServerState.track(ConcurrentHashMap.newKeySet());

    public static void markArriving(ServerPlayer player) {
        ARRIVING.add(player.getUUID());
    }

    public static boolean isArriving(ServerPlayer player) {
        return ARRIVING.contains(player.getUUID());
    }

    public static void clearArriving(ServerPlayer player) {
        ARRIVING.remove(player.getUUID());
    }

    public static Arrival arrival(Tardis tardis, ServerLevel interior) {
        BlockPos doorPos = locateInteriorDoor(tardis, interior);
        if (doorPos == null) {
            GCLog.TARDIS.warn("No interior door found for TARDIS {}, arriving at interior origin", tardis.id());
            return new Arrival(Vec3.atBottomCenterOf(ORIGIN.above()), 0.0f);
        }
        BlockState state = interior.getBlockState(doorPos);
        Direction facing = state.getValue(InteriorDoorBlock.FACING);
        Direction right = InteriorDoorBlock.rightDirectionOf(state);
        BlockPos bottom = InteriorDoorBlock.isFlipped(state) ? doorPos.below(InteriorDoorBlock.HEIGHT - 1) : doorPos;
        Vec3 centre = Vec3.atBottomCenterOf(bottom).add(right.getStepX() * 0.5, 0.0, right.getStepZ() * 0.5);
        Vec3 pos = centre.add(facing.getStepX() * ARRIVAL_DISTANCE, 0.0, facing.getStepZ() * ARRIVAL_DISTANCE);
        return new Arrival(pos, facing.toYRot());
    }

    private static @Nullable BlockPos locateInteriorDoor(Tardis tardis, ServerLevel interior) {
        for (InteriorDoorBlockEntity door : tardis.interiorDoors()) {
            if (door.getLevel() != interior) continue;
            BlockState state = interior.getBlockState(door.getBlockPos());
            if (state.is(TardisBlocks.TARDIS_INTERIOR_DOOR) && InteriorDoorBlock.isMaster(state)) {
                return door.getBlockPos();
            }
        }
        return findInteriorDoor(tardis, interior);
    }

    /** use {@link Tardis#interior()}, this is what it reads */
    public static TardisInterior getInteriorDefinition(Tardis tardis) {
        TardisData data = tardis.data();
        String stored = data.getInteriorId();
        if (stored != null) return TardisInteriorRegistry.get(stored);

        TardisInterior chosen = TardisInteriorRegistry.get(TardisInteriorRegistry.defaultId());
        data.setInteriorId(chosen.id());
        return chosen;
    }

    public static boolean buildInteriorIfMissing(Tardis tardis, ServerLevel interior) {
        return buildInteriorIfMissing(tardis, interior, null);
    }

    public static @Nullable ServerLevel prepareInterior(Tardis tardis) {
        try {
            ServerLevel interior = TardisDimensionManager.getOrCreate(tardis);
            buildInteriorIfMissing(tardis, interior);
            return interior;
        } catch (Exception e) {
            GCLog.TARDIS.error("Couldn't prepare TARDIS interior {}", tardis.id(), e);
            return null;
        }
    }

    public static @Nullable BlockPos findInteriorDoor(Tardis tardis, ServerLevel interior) {
        MinecraftServer server = interior.getServer();
        Identifier templateId = tardis.interior().structure();
        Vec3i size = server.getStructureTemplateManager()
                .get(templateId)
                .map(StructureTemplate::getSize)
                .orElse(FALLBACK_CLEAR_SIZE);
        BlockPos stored = tardis.data().getInteriorOrigin();
        BlockPos origin = stored != null ? stored : ORIGIN;
        loadInteriorChunks(interior, origin, size);

        BlockPos cachedOffset = DOOR_OFFSET_CACHE.get(templateId);
        if (cachedOffset != null) {
            BlockPos candidate = origin.offset(cachedOffset.getX(), cachedOffset.getY(), cachedOffset.getZ());
            BlockState state = interior.getBlockState(candidate);
            if (state.is(TardisBlocks.TARDIS_INTERIOR_DOOR) && InteriorDoorBlock.isMaster(state)) {
                return candidate.immutable();
            }
        }

        BlockPos max = origin.offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1);
        for (BlockPos p : BlockPos.betweenClosed(origin, max)) {
            BlockState state = interior.getBlockState(p);
            if (state.is(TardisBlocks.TARDIS_INTERIOR_DOOR) && InteriorDoorBlock.isMaster(state)) {
                BlockPos found = p.immutable();
                DOOR_OFFSET_CACHE.put(templateId, found.subtract(origin));
                return found;
            }
        }
        return null;
    }

    private static boolean buildInteriorIfMissing(
            Tardis tardis, ServerLevel interior, @Nullable ConsoleCarryover carry) {
        TardisData data = tardis.data();
        if (data.getInteriorOrigin() != null) return false;

        Identifier templateId = tardis.interior().structure();
        Optional<StructureTemplate> template =
                interior.getServer().getStructureTemplateManager().get(templateId);
        if (template.isEmpty()) {
            GCLog.TARDIS.error("Missing interior template {}", templateId);
            return false;
        }

        BlockPos origin = ORIGIN;
        Vec3i size = template.get().getSize();
        loadInteriorChunks(interior, origin, size);

        template.get()
                .placeInWorld(
                        interior,
                        origin,
                        origin,
                        new StructurePlaceSettings(),
                        interior.getRandom(),
                        Block.UPDATE_CLIENTS);

        BlockPos consolePos = findConsole(interior, origin, size, templateId);
        if (consolePos == null) {
            consolePos = origin.offset(size.getX() / 2, 1, size.getZ() / 2);
            interior.setBlock(consolePos, TardisBlocks.TARDIS_CONSOLE.defaultBlockState(), 3);
        }

        if (interior.getBlockEntity(consolePos) instanceof TardisConsoleBlockEntity console) {
            ConsoleLink.link(tardis, interior, consolePos);
            applyInteriorConsole(console, carry);
            ConsoleLink.spawnDefaultControls(console);
            console.sync();
        } else {
            GCLog.TARDIS.error("No console block entity at {} in TARDIS interior", consolePos);
        }

        data.setInteriorOrigin(origin);
        return true;
    }

    private static void applyInteriorConsole(TardisConsoleBlockEntity console, @Nullable ConsoleCarryover carry) {
        TardisConsole model = console.getConsole();
        String variant = model != null ? console.getVariant() : null;

        if (model == null && carry != null) {
            model = carry.console();
            variant = carry.variant();
        }
        if (model == null) {
            model = TardisConsoleRegistry.get(TardisConsoleRegistry.defaultId());
        }

        if (variant != null) console.setConsole(model, variant);
        else console.setConsole(model);

        if (carry != null) console.applyCarriedControlStates(carry.controlStates());
    }

    public static ChangeResult changeInterior(Tardis tardis, String newId) {
        if (TardisInteriorRegistry.isUnknown(newId)) return ChangeResult.UNKNOWN_INTERIOR;

        MinecraftServer server = tardis.server();
        TardisData data = tardis.data();
        TardisInterior oldDef = tardis.interior();
        TardisInterior newDef = TardisInteriorRegistry.get(newId);
        if (oldDef.id().equals(newDef.id())) return ChangeResult.UNCHANGED;

        Optional<StructureTemplate> newTemplate =
                server.getStructureTemplateManager().get(newDef.structure());
        if (newTemplate.isEmpty()) return ChangeResult.MISSING_TEMPLATE;

        if (data.getInteriorOrigin() == null) {
            data.setInteriorId(newDef.id());
            return ChangeResult.CHANGED;
        }

        ServerLevel level = TardisDimensionManager.getOrCreate(tardis);

        ConsoleCarryover carry = null;
        Optional<TardisConsoleBlockEntity> oldConsole = tardis.console();
        if (oldConsole.isPresent() && oldConsole.get().getConsole() != null) {
            carry = new ConsoleCarryover(
                    oldConsole.get().getConsole(),
                    oldConsole.get().getVariant(),
                    oldConsole.get().copyControlStates());
        }

        List<ServerPlayer> occupants = List.copyOf(level.players());

        Vec3i oldSize = server.getStructureTemplateManager()
                .get(oldDef.structure())
                .map(StructureTemplate::getSize)
                .orElse(FALLBACK_CLEAR_SIZE);
        Vec3i newSize = newTemplate.get().getSize();
        Vec3i size = new Vec3i(
                Math.max(oldSize.getX(), newSize.getX()),
                Math.max(oldSize.getY(), newSize.getY()),
                Math.max(oldSize.getZ(), newSize.getZ()));

        DoorState carriedPrimary = primaryDoorState(tardis, level);

        clearArea(level, size);

        data.setInteriorOrigin(null);
        data.setInteriorId(newDef.id());
        buildInteriorIfMissing(tardis, level, carry);
        if (carriedPrimary != null && !carriedPrimary.isClosed()) {
            snapPrimaryDoors(tardis, level, size, carriedPrimary);
        }
        BlockPos doorPos = findInteriorDoor(tardis, level);
        if (doorPos != null && level.getBlockEntity(doorPos) instanceof InteriorDoorBlockEntity door) {
            door.matchTardis(tardis);
        }
        DoorSync.reconcileConsole(tardis);
        TardisDimensionManager.scheduleControlPlacement(tardis);

        Arrival arrival = arrival(tardis, level);
        for (ServerPlayer player : occupants) {
            markArriving(player);
            player.teleportTo(
                    level, arrival.pos().x, arrival.pos().y, arrival.pos().z, Set.of(), arrival.yaw(), 0.0f, true);
        }
        return ChangeResult.CHANGED;
    }

    private static @Nullable DoorState primaryDoorState(Tardis tardis, ServerLevel interior) {
        InteriorDoorBlockEntity door = tardis.primaryInteriorDoor().orElse(null);
        return door != null && door.getLevel() == interior ? door.getDoorState() : null;
    }

    /** uses whatever state the old interiors doors were in */
    private static void snapPrimaryDoors(Tardis tardis, ServerLevel interior, Vec3i size, DoorState state) {
        BlockPos stored = tardis.data().getInteriorOrigin();
        BlockPos origin = stored != null ? stored : ORIGIN;
        BlockPos max = origin.offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1);
        for (BlockPos p : BlockPos.betweenClosed(origin, max)) {
            if (interior.getBlockEntity(p) instanceof InteriorDoorBlockEntity door && door.isPrimary()) {
                door.snapTo(state);
            }
        }
    }

    private static void clearArea(ServerLevel level, Vec3i size) {
        loadInteriorChunks(level, ORIGIN, size);
        BlockPos max = ORIGIN.offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1);

        for (BlockPos p : BlockPos.betweenClosed(ORIGIN, max)) {
            BlockEntity be = level.getBlockEntity(p);
            if (be instanceof Clearable container) container.clearContent();
            if (!level.getBlockState(p).isAir()) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), CLEAR_FLAGS);
            }
        }

        AABB box = new AABB(Vec3.atLowerCornerOf(ORIGIN), Vec3.atLowerCornerOf(max.offset(1, 1, 1)));
        for (Entity entity : level.getEntities((Entity) null, box, e -> !(e instanceof Player))) {
            entity.discard();
        }
    }

    private static void loadInteriorChunks(ServerLevel level, BlockPos origin, Vec3i size) {
        int minCx = origin.getX() >> 4;
        int maxCx = (origin.getX() + size.getX() - 1) >> 4;
        int minCz = origin.getZ() >> 4;
        int maxCz = (origin.getZ() + size.getZ() - 1) >> 4;
        for (int cx = minCx; cx <= maxCx; cx++) {
            for (int cz = minCz; cz <= maxCz; cz++) {
                level.getChunk(cx, cz);
            }
        }
    }

    private static @Nullable BlockPos findConsole(
            ServerLevel level, BlockPos origin, Vec3i size, Identifier templateId) {
        BlockPos cachedOffset = CONSOLE_OFFSET_CACHE.get(templateId);
        if (cachedOffset != null) {
            BlockPos candidate = origin.offset(cachedOffset.getX(), cachedOffset.getY(), cachedOffset.getZ());
            if (level.getBlockEntity(candidate) instanceof TardisConsoleBlockEntity) {
                return candidate.immutable();
            }
        }

        BlockPos max = origin.offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1);
        for (BlockPos p : BlockPos.betweenClosed(ORIGIN, max)) {
            if (level.getBlockEntity(p) instanceof TardisConsoleBlockEntity) {
                BlockPos found = p.immutable();
                CONSOLE_OFFSET_CACHE.put(templateId, found.subtract(origin));
                return found;
            }
        }
        return null;
    }
}
