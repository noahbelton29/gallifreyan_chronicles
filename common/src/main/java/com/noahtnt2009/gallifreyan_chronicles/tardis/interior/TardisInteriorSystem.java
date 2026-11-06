package com.noahtnt2009.gallifreyan_chronicles.tardis.interior;

import com.noahtnt2009.gallifreyan_chronicles.compat.portal.TardisPortalBridge;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisApi;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.block.InteriorDoorBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.InteriorDoorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsole;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsoleRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.ConsoleLinkSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.DoorSyncSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.manager.TardisManager;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.UUID;

public final class TardisInteriorSystem {
    private static final double ZONE_NEAR = -0.5;
    private static final double ZONE_FAR = 0.3;
    private static final double ZONE_HALF_WIDTH = 0.5;
    private static final double ZONE_BOTTOM = -0.5;
    private static final double ZONE_TOP = 2.0;

    public static final BlockPos ORIGIN = new BlockPos(0, 64, 0);

    private static final double EXIT_DISTANCE = 2.0;
    private static final double ARRIVAL_DISTANCE = 1.0;

    private static final Vec3i FALLBACK_CLEAR_SIZE = new Vec3i(64, 64, 64);
    private static final int CLEAR_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS;

    private record ConsoleCarryover(TardisConsole console, @Nullable String variant, CompoundTag controlStates) {
    }

    public enum ChangeResult { CHANGED, UNCHANGED, UNKNOWN_INTERIOR, MISSING_TEMPLATE }

    private TardisInteriorSystem() {
    }

    public static void tickEntryZone(Level level, TardisExteriorBlockEntity exterior) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (exterior.getDoorState().isClosed() || exterior.isLocked()) return;
        if (exterior.isFullyInvisible()) return;

        UUID tardisId = exterior.getTardisId();
        if (tardisId == null) return;
        if (TardisPortalBridge.isActive(tardisId)) return;

        BlockPos pos = exterior.getBlockPos();
        double yawRad = Math.toRadians(exterior.getYaw());
        double frontX = -Math.sin(yawRad);
        double frontZ = Math.cos(yawRad);

        AABB search = new AABB(pos).inflate(2.5, 2.5, 2.5);
        for (ServerPlayer player : serverLevel.getEntitiesOfClass(ServerPlayer.class, search)) {
            if (player.isSpectator()) continue;
            if (!inEntryZone(player, pos, frontX, frontZ)) continue;
            TardisDimensionManager.requestEntry(serverLevel.getServer(), player, tardisId);
        }
    }

    private static boolean inEntryZone(ServerPlayer player, BlockPos pos, double frontX, double frontZ) {
        double dx = player.getX() - (pos.getX() + 0.5);
        double dz = player.getZ() - (pos.getZ() + 0.5);
        double dy = player.getY() - pos.getY();

        double forward = dx * frontX + dz * frontZ;
        double lateral = dx * frontZ - dz * frontX;

        return forward >= ZONE_NEAR && forward <= ZONE_FAR
                && Math.abs(lateral) <= ZONE_HALF_WIDTH
                && dy >= ZONE_BOTTOM && dy <= ZONE_TOP;
    }

    public static void exitInterior(MinecraftServer server, ServerPlayer player, UUID tardisId) {
        TardisManager manager = TardisManager.get(server);
        BlockPos exteriorPos = manager.resolveExteriorBlockPos(tardisId);
        if (exteriorPos == null) return;

        ServerLevel target = server.getLevel(manager.getExteriorDimension(tardisId));
        if (target == null) target = server.overworld();

        Vec3 spot = Vec3.atBottomCenterOf(exteriorPos).add(0.0, 1.0, 0.0);
        float yaw = 0.0f;

        Optional<TardisExteriorBlockEntity> exterior = TardisApi.get(server, tardisId).flatMap(Tardis::exterior);
        if (exterior.isPresent()) {
            double yawRad = Math.toRadians(exterior.get().getYaw());
            spot = Vec3.atBottomCenterOf(exteriorPos)
                    .add(-Math.sin(yawRad) * EXIT_DISTANCE, 0.0, Math.cos(yawRad) * EXIT_DISTANCE);
            yaw = exterior.get().getYaw();
        }

        clearArriving(player);
        player.teleportTo(target, spot.x, spot.y, spot.z, Set.of(), yaw, 0.0f, true);
    }

    public record Arrival(Vec3 pos, float yaw) {
    }

    private static final Set<UUID> ARRIVING = ConcurrentHashMap.newKeySet();

    public static void markArriving(ServerPlayer player) {
        ARRIVING.add(player.getUUID());
    }

    public static boolean isArriving(ServerPlayer player) {
        return ARRIVING.contains(player.getUUID());
    }

    public static void clearArriving(ServerPlayer player) {
        ARRIVING.remove(player.getUUID());
    }

    public static Arrival arrival(ServerLevel interior, UUID tardisId) {
        BlockPos doorPos = locateInteriorDoor(interior, tardisId);
        if (doorPos == null) {
            Constants.LOG.warn("no interior door found for tardis {}, arriving at interior origin", tardisId);
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

    private static @Nullable BlockPos locateInteriorDoor(ServerLevel interior, UUID tardisId) {
        for (InteriorDoorBlockEntity door : DoorSyncSystem.doorsOf(tardisId)) {
            if (door.getLevel() != interior) continue;
            BlockState state = interior.getBlockState(door.getBlockPos());
            if (state.is(GCBlocks.TARDIS_INTERIOR_DOOR) && InteriorDoorBlock.isMaster(state)) {
                return door.getBlockPos();
            }
        }
        return findInteriorDoor(interior, tardisId);
    }

    public static TardisInterior getInteriorDefinition(MinecraftServer server, UUID tardisId) {
        TardisManager manager = TardisManager.get(server);
        String stored = manager.getInteriorId(tardisId);
        if (stored != null) return TardisInteriorRegistry.get(stored);

        TardisInterior chosen = TardisInteriorRegistry.get(TardisInteriorRegistry.defaultId());
        manager.setInteriorId(tardisId, chosen.id());
        return chosen;
    }

    public static boolean buildInteriorIfMissing(ServerLevel interior, UUID tardisId) {
        return buildInteriorIfMissing(interior, tardisId, null);
    }

    public static @Nullable ServerLevel prepareInterior(MinecraftServer server, UUID tardisId) {
        try {
            ServerLevel interior = TardisDimensionManager.getOrCreate(server, tardisId);
            buildInteriorIfMissing(interior, tardisId);
            return interior;
        } catch (Exception e) {
            Constants.LOG.error("failed to prepare tardis interior {}", tardisId, e);
            return null;
        }
    }

    public static @Nullable BlockPos findInteriorDoor(ServerLevel interior, UUID tardisId) {
        MinecraftServer server = interior.getServer();
        Identifier templateId = getInteriorDefinition(server, tardisId).structure();
        Vec3i size = server.getStructureTemplateManager().get(templateId)
                .map(StructureTemplate::getSize)
                .orElse(FALLBACK_CLEAR_SIZE);
        BlockPos stored = TardisManager.get(server).getInteriorOrigin(tardisId);
        BlockPos origin = stored != null ? stored : ORIGIN;
        loadInteriorChunks(interior, origin, size);

        BlockPos max = origin.offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1);
        for (BlockPos p : BlockPos.betweenClosed(origin, max)) {
            BlockState state = interior.getBlockState(p);
            if (state.is(GCBlocks.TARDIS_INTERIOR_DOOR) && InteriorDoorBlock.isMaster(state)) {
                return p.immutable();
            }
        }
        return null;
    }

    private static boolean buildInteriorIfMissing(ServerLevel interior, UUID tardisId, @Nullable ConsoleCarryover carry) {
        TardisManager manager = TardisManager.get(interior.getServer());
        if (manager.getInteriorOrigin(tardisId) != null) return false;

        Identifier templateId = getInteriorDefinition(interior.getServer(), tardisId).structure();
        Optional<StructureTemplate> template = interior.getServer().getStructureTemplateManager().get(templateId);
        if (template.isEmpty()) {
            Constants.LOG.error("missing interior template {}", templateId);
            return false;
        }

        BlockPos origin = ORIGIN;
        Vec3i size = template.get().getSize();
        loadInteriorChunks(interior, origin, size);

        template.get().placeInWorld(interior, origin, origin, new StructurePlaceSettings(),
                interior.getRandom(), 3);

        BlockPos consolePos = findConsole(interior, origin, size);
        if (consolePos == null) {
            consolePos = origin.offset(size.getX() / 2, 1, size.getZ() / 2);
            interior.setBlock(consolePos, GCBlocks.TARDIS_CONSOLE.defaultBlockState(), 3);
        }

        if (interior.getBlockEntity(consolePos) instanceof TardisConsoleBlockEntity console) {
            ConsoleLinkSystem.link(interior, tardisId, consolePos);
            applyInteriorConsole(console, carry);
            ConsoleLinkSystem.spawnDefaultControls(console);
            console.sync();
        } else {
            Constants.LOG.error("no console block entity at {} in tardis interior", consolePos);
        }

        manager.setInteriorOrigin(tardisId, origin);
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

    public static ChangeResult changeInterior(MinecraftServer server, UUID tardisId, String newId) {
        if (!TardisInteriorRegistry.contains(newId)) return ChangeResult.UNKNOWN_INTERIOR;

        TardisManager manager = TardisManager.get(server);
        TardisInterior oldDef = getInteriorDefinition(server, tardisId);
        TardisInterior newDef = TardisInteriorRegistry.get(newId);
        if (oldDef.id().equals(newDef.id())) return ChangeResult.UNCHANGED;

        Optional<StructureTemplate> newTemplate = server.getStructureTemplateManager().get(newDef.structure());
        if (newTemplate.isEmpty()) return ChangeResult.MISSING_TEMPLATE;

        if (manager.getInteriorOrigin(tardisId) == null) {
            manager.setInteriorId(tardisId, newDef.id());
            return ChangeResult.CHANGED;
        }

        ServerLevel level = TardisDimensionManager.getOrCreate(server, tardisId);

        ConsoleCarryover carry = null;
        Optional<TardisConsoleBlockEntity> oldConsole = TardisApi.get(server, tardisId).flatMap(Tardis::console);
        if (oldConsole.isPresent() && oldConsole.get().getConsole() != null) {
            carry = new ConsoleCarryover(oldConsole.get().getConsole(), oldConsole.get().getVariant(),
                    oldConsole.get().copyControlStates());
        }

        List<ServerPlayer> occupants = List.copyOf(level.players());

        Vec3i oldSize = server.getStructureTemplateManager().get(oldDef.structure())
                .map(StructureTemplate::getSize).orElse(FALLBACK_CLEAR_SIZE);
        Vec3i newSize = newTemplate.get().getSize();
        Vec3i size = new Vec3i(Math.max(oldSize.getX(), newSize.getX()),
                Math.max(oldSize.getY(), newSize.getY()),
                Math.max(oldSize.getZ(), newSize.getZ()));

        clearArea(level, size);

        manager.clearInteriorOrigin(tardisId);
        manager.setInteriorId(tardisId, newDef.id());
        buildInteriorIfMissing(level, tardisId, carry);
        BlockPos doorPos = findInteriorDoor(level, tardisId);
        if (doorPos != null && level.getBlockEntity(doorPos) instanceof InteriorDoorBlockEntity door) {
            door.matchTardis(server, tardisId);
        }
        DoorSyncSystem.reconcileConsole(server, tardisId);
        TardisDimensionManager.scheduleControlPlacement(tardisId);

        Arrival arrival = arrival(level, tardisId);
        for (ServerPlayer player : occupants) {
            markArriving(player);
            player.teleportTo(level, arrival.pos().x, arrival.pos().y, arrival.pos().z, Set.of(), arrival.yaw(), 0.0f, true);
        }
        return ChangeResult.CHANGED;
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

        AABB box = new AABB(
                Vec3.atLowerCornerOf(ORIGIN), Vec3.atLowerCornerOf(max.offset(1, 1, 1)));
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

    private static @Nullable BlockPos findConsole(ServerLevel level, BlockPos origin, Vec3i size) {
        BlockPos max = origin.offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1);
        for (BlockPos p : BlockPos.betweenClosed(ORIGIN, max)) {
            if (level.getBlockEntity(p) instanceof TardisConsoleBlockEntity) {
                return p.immutable();
            }
        }
        return null;
    }
}