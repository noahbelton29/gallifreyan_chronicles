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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;
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
    private static final double DOOR_EXIT_NEAR = 0.3;
    private static final double DOOR_EXIT_FAR = 1.5;
    private static final double DOOR_EXIT_HALF_WIDTH = 1.0;

    private static final String INTERIOR_PREFIX = "tardis/";

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

    private static final int EXIT_CHECK_INTERVAL_TICKS = 4;
    private static final Map<Identifier, Optional<UUID>> DIMENSION_TARDIS_IDS = new ConcurrentHashMap<>();

    private static Optional<UUID> parseTardisId(Identifier dimension) {
        if (!dimension.getNamespace().equals(Constants.MOD_ID)) return Optional.empty();
        String path = dimension.getPath();
        if (!path.startsWith(INTERIOR_PREFIX)) return Optional.empty();
        try {
            return Optional.of(UUID.fromString(path.substring(INTERIOR_PREFIX.length())));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public static void tickExitZones(MinecraftServer server) {
        if (server.getTickCount() % EXIT_CHECK_INTERVAL_TICKS != 0) return;

        for (ServerPlayer player : List.copyOf(server.getPlayerList().getPlayers())) {
            Optional<UUID> interiorId = DIMENSION_TARDIS_IDS.computeIfAbsent(
                    player.level().dimension().identifier(), TardisInteriorSystem::parseTardisId);
            if (interiorId.isEmpty()) continue;
            if (player.isSpectator()) continue;

            UUID tardisId = interiorId.get();
            if (TardisPortalBridge.isActive(tardisId)) continue;
            if (isBehindOpenInteriorDoor(player, tardisId)) {
                exitInterior(server, player, tardisId);
                continue;
            }
            AABB exitBox = getInteriorDefinition(server, tardisId).exitBox();
            double dx = player.getX() - ORIGIN.getX();
            double dy = player.getY() - ORIGIN.getY();
            double dz = player.getZ() - ORIGIN.getZ();
            if (dx < exitBox.minX || dx > exitBox.maxX) continue;
            if (dy < exitBox.minY || dy > exitBox.maxY) continue;
            if (dz < exitBox.minZ || dz > exitBox.maxZ) continue;
            exitInterior(server, player, tardisId);
        }
    }

    private static boolean isBehindOpenInteriorDoor(ServerPlayer player, UUID tardisId) {
        for (InteriorDoorBlockEntity door : DoorSyncSystem.doorsOf(tardisId)) {
            if (door.isRemoved() || door.getLevel() != player.level()) continue;
            if (door.getDoorState().isClosed()) continue;
            BlockState state = door.getBlockState();
            if (!(state.getBlock() instanceof InteriorDoorBlock)) continue;
            Direction facing = state.getValue(InteriorDoorBlock.FACING);
            Direction right = InteriorDoorBlock.rightDirectionOf(facing);
            Direction back = facing.getOpposite();
            Vec3 centre = Vec3.atBottomCenterOf(door.getBlockPos())
                    .add(right.getStepX() * 0.5, 0.0, right.getStepZ() * 0.5);
            double dx = player.getX() - centre.x;
            double dz = player.getZ() - centre.z;
            double dy = player.getY() - centre.y;
            double depth = dx * back.getStepX() + dz * back.getStepZ();
            double lateral = dx * right.getStepX() + dz * right.getStepZ();
            if (depth >= DOOR_EXIT_NEAR && depth <= DOOR_EXIT_FAR
                    && Math.abs(lateral) <= DOOR_EXIT_HALF_WIDTH
                    && dy >= ZONE_BOTTOM && dy <= InteriorDoorBlock.HEIGHT) {
                return true;
            }
        }
        return false;
    }

    private static void exitInterior(MinecraftServer server, ServerPlayer player, UUID tardisId) {
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

        player.teleportTo(target, spot.x, spot.y, spot.z, Set.of(), yaw, 0.0f, true);
    }

    public static Vec3 arrivalPoint(TardisInterior definition) {
        return Vec3.atLowerCornerOf(ORIGIN).add(definition.spawn());
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
            applyInteriorConsole(console, getInteriorDefinition(interior.getServer(), tardisId), carry);
            ConsoleLinkSystem.spawnDefaultControls(console);
            console.sync();
        } else {
            Constants.LOG.error("no console block entity at {} in tardis interior", consolePos);
        }

        manager.setInteriorOrigin(tardisId, origin);
        return true;
    }

    private static void applyInteriorConsole(TardisConsoleBlockEntity console, TardisInterior def,
                                             @Nullable ConsoleCarryover carry) {
        TardisConsole model = null;
        String variant = null;

        if (def.console().isPresent()) {
            String consoleId = def.console().get();
            if (!consoleId.contains(":")) consoleId = Constants.MOD_ID + ":" + consoleId;
            if (TardisConsoleRegistry.contains(consoleId)) {
                model = TardisConsoleRegistry.get(consoleId);
            } else {
                Constants.LOG.warn("interior {} names unknown console {}", def.id(), consoleId);
            }
        }
        if (model == null && console.getConsole() != null) {
            model = console.getConsole();
            variant = console.getVariant();
        }
        if (model == null && carry != null) {
            model = carry.console();
            variant = carry.variant();
        }
        if (model == null) {
            model = TardisConsoleRegistry.get(TardisConsoleRegistry.defaultId());
        }

        if (def.consoleVariant().isPresent()) {
            variant = def.consoleVariant().get();
            if (!model.hasVariant(variant)) {
                Constants.LOG.warn("interior {} console variant {} does not belong to console {}",
                        def.id(), variant, model.id());
            }
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

        Vec3 target = arrivalPoint(newDef);
        for (ServerPlayer player : occupants) {
            player.teleportTo(level, target.x, target.y, target.z, Set.of(), newDef.spawnYaw(), 0.0f, true);
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