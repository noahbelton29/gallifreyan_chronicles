package com.noahtnt2009.gallifreyan_chronicles.tardis.interior;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import com.noahtnt2009.gallifreyan_chronicles.init.GCDimensions;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsoleRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.ConsoleLinkSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.ExteriorLinkSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.manager.TardisManager;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class TardisInteriorSystem {
    private static final Identifier TEMPLATE_ID = GCUtils.of("template_interior");

    // entry zone in the exterior's local frame, measured from the block center along the door facing
    // negative values are inside the block, so the player has to walk into the open exterior
    private static final double ZONE_NEAR = -0.5;
    private static final double ZONE_FAR = 0.3;
    private static final double ZONE_HALF_WIDTH = 0.5;
    private static final double ZONE_BOTTOM = -0.5;
    private static final double ZONE_TOP = 2.0;

    // every tardis has its own dimension so the interior always sits at the same origin
    public static final BlockPos ORIGIN = new BlockPos(0, 64, 0);

    // arrival point relative to the interior origin, facing the console
    private static final Vec3 ARRIVAL_OFFSET = new Vec3(3.5, 1.0, 5.5);
    public static final float ARRIVAL_YAW = 180.0f;

    // exit zone: the last row of the platform, one step behind the arrival point
    private static final double EXIT_MIN_X = 2.5;
    private static final double EXIT_MAX_X = 4.5;
    private static final double EXIT_MIN_Z = 6.0;
    private static final double EXIT_MAX_Z = 7.0;
    private static final double EXIT_MIN_Y = 0.5;
    private static final double EXIT_MAX_Y = 3.0;

    // how far in front of the exterior door the player is placed, outside the entry zone
    private static final double EXIT_DISTANCE = 2.0;

    private static final String INTERIOR_PREFIX = "tardis/";

    private TardisInteriorSystem() {
    }

    public static void tickEntry(Level level, TardisExteriorBlockEntity exterior) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (exterior.getDoorState().isClosed() || exterior.isLocked()) return;

        UUID tardisId = exterior.getTardisId();
        if (tardisId == null) return;

        BlockPos pos = exterior.getBlockPos();
        double yawRad = Math.toRadians(exterior.getYaw());
        double frontX = -Math.sin(yawRad);
        double frontZ = Math.cos(yawRad);

        AABB search = new AABB(pos).inflate(2.5, 2.5, 2.5);
        for (ServerPlayer player : serverLevel.getEntitiesOfClass(ServerPlayer.class, search)) {
            if (player.isSpectator()) continue;
            if (!inEntryZone(player, pos, frontX, frontZ)) continue;
            TardisDimensionManager.request(serverLevel.getServer(), player, tardisId);
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

    public static void tickExits(MinecraftServer server) {
        for (ServerPlayer player : List.copyOf(server.getPlayerList().getPlayers())) {
            Identifier dimension = player.level().dimension().identifier();
            if (!dimension.getNamespace().equals(Constants.MOD_ID)) continue;
            String path = dimension.getPath();
            if (!path.startsWith(INTERIOR_PREFIX)) continue;
            if (player.isSpectator()) continue;

            double dx = player.getX() - ORIGIN.getX();
            double dy = player.getY() - ORIGIN.getY();
            double dz = player.getZ() - ORIGIN.getZ();
            if (dx < EXIT_MIN_X || dx > EXIT_MAX_X) continue;
            if (dz < EXIT_MIN_Z || dz > EXIT_MAX_Z) continue;
            if (dy < EXIT_MIN_Y || dy > EXIT_MAX_Y) continue;

            UUID tardisId;
            try {
                tardisId = UUID.fromString(path.substring(INTERIOR_PREFIX.length()));
            } catch (IllegalArgumentException e) {
                continue;
            }
            exit(server, player, tardisId);
        }
    }

    private static void exit(MinecraftServer server, ServerPlayer player, UUID tardisId) {
        TardisManager manager = TardisManager.get(server);
        BlockPos exteriorPos = manager.resolveExteriorBlockPos(tardisId);
        if (exteriorPos == null) return;

        ServerLevel target = server.getLevel(manager.getExteriorDimension(tardisId));
        if (target == null) target = server.overworld();

        Vec3 spot = Vec3.atBottomCenterOf(exteriorPos).add(0.0, 1.0, 0.0);
        float yaw = 0.0f;

        Optional<TardisExteriorBlockEntity> exterior = ExteriorLinkSystem.resolve(target, tardisId);
        if (exterior.isPresent()) {
            double yawRad = Math.toRadians(exterior.get().getYaw());
            spot = Vec3.atBottomCenterOf(exteriorPos)
                    .add(-Math.sin(yawRad) * EXIT_DISTANCE, 0.0, Math.cos(yawRad) * EXIT_DISTANCE);
            yaw = exterior.get().getYaw();
        }

        player.teleportTo(target, spot.x, spot.y, spot.z, Set.of(), yaw, 0.0f, true);
    }

    public static Vec3 arrivalPoint() {
        return Vec3.atLowerCornerOf(ORIGIN).add(ARRIVAL_OFFSET);
    }

    public static void ensureBuilt(ServerLevel interior, UUID tardisId) {
        TardisManager manager = TardisManager.get(interior.getServer());
        if (manager.getInteriorOrigin(tardisId) != null) return;

        Optional<StructureTemplate> template = interior.getServer().getStructureTemplateManager().get(TEMPLATE_ID);
        if (template.isEmpty()) {
            Constants.LOG.error("missing interior template {}", TEMPLATE_ID);
            return;
        }

        BlockPos origin = ORIGIN;
        Vec3i size = template.get().getSize();
        loadChunks(interior, origin, size);

        template.get().placeInWorld(interior, origin, origin, new StructurePlaceSettings(),
                interior.getRandom(), 3);

        BlockPos consolePos = findConsole(interior, origin, size);
        if (consolePos == null) {
            consolePos = origin.offset(size.getX() / 2, 1, size.getZ() / 2);
            interior.setBlock(consolePos, GCBlocks.TARDIS_CONSOLE.defaultBlockState(), 3);
        }

        if (interior.getBlockEntity(consolePos) instanceof TardisConsoleBlockEntity console) {
            ConsoleLinkSystem.link(interior, tardisId, consolePos);
            if (console.getConsole() == null) {
                console.setConsole(TardisConsoleRegistry.get(TardisConsoleRegistry.DEFAULT_ID));
            }
            ConsoleLinkSystem.spawnDefaultControls(console);
            console.sync();
        } else {
            Constants.LOG.error("no console block entity at {} in tardis interior", consolePos);
        }

        manager.setInteriorOrigin(tardisId, origin);
    }

    private static void loadChunks(ServerLevel level, BlockPos origin, Vec3i size) {
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
        for (BlockPos p : BlockPos.betweenClosed(origin, max)) {
            if (level.getBlockEntity(p) instanceof TardisConsoleBlockEntity) {
                return p.immutable();
            }
        }
        return null;
    }
}
