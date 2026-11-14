package uk.nbforge.gallifreyan_chronicles.tardis.flight;

import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.network.tardis.RematCatchPayload;
import uk.nbforge.gallifreyan_chronicles.platform.Services;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.TardisInteriorSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisManager;
import uk.nbforge.gallifreyan_chronicles.util.GCSchedulerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/* a tardis materialising on top of a player pulls them inside. they see the interior
   fade in around them (floor first), everyone inside sees them fade in */
public final class RematCatch {
    private static final Vec3i FALLBACK_SIZE = new Vec3i(48, 32, 48);
    /* after the fade, send the real interior again so block entities come back right */
    private static final int RESEND_EXTRA_TICKS = 5;

    private RematCatch() {
    }

    /* the exterior just started materialising */
    public static void onRemat(TardisExteriorBlockEntity exterior, long startTick) {
        if (!(exterior.getLevel() instanceof ServerLevel level)) return;
        UUID tardisId = exterior.getTardisId();
        if (tardisId == null) return;
        BlockPos pos = exterior.getBlockPos();
        AABB footprint = new AABB(pos).expandTowards(0.0, 1.0, 0.0);
        List<ServerPlayer> caught = level.getEntitiesOfClass(ServerPlayer.class, footprint,
                player -> !player.isSpectator() && player.isAlive());
        if (caught.isEmpty()) return;

        MinecraftServer server = level.getServer();
        ServerLevel interior = TardisInteriorSystem.prepareInterior(server, tardisId);
        if (interior == null) return;
        TardisInteriorSystem.Arrival arrival = TardisInteriorSystem.arrival(interior, tardisId);

        BlockPos origin = TardisManager.get(server).getInteriorOrigin(tardisId);
        if (origin == null) origin = TardisInteriorSystem.ORIGIN;
        Vec3i size = server.getStructureTemplateManager()
                .get(TardisInteriorSystem.getInteriorDefinition(server, tardisId).structure())
                .map(StructureTemplate::getSize).orElse(FALLBACK_SIZE);
        BlockPos boxMin = origin;
        BlockPos boxMax = origin.offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1);
        int floorY = BlockPos.containing(arrival.pos()).getY();

        String fadeType = exterior.getFadeType().id();
        int duration = Math.max(1, exterior.getFadeType().timing().fadeInTicks());
        int elapsed = (int) Math.max(0L, level.getGameTime() - startTick);

        for (ServerPlayer player : caught) {
            TardisInteriorSystem.markArriving(player);
            player.teleportTo(interior, arrival.pos().x, arrival.pos().y, arrival.pos().z, Set.of(),
                    arrival.yaw(), 0.0f, true);

            Services.NETWORK.sendRematCatch(player, new RematCatchPayload(player.getUUID(), fadeType, elapsed,
                    duration, true, boxMin, boxMax, floorY));
            for (ServerPlayer other : interior.players()) {
                if (other == player) continue;
                Services.NETWORK.sendRematCatch(other, new RematCatchPayload(player.getUUID(), fadeType, elapsed,
                        duration, false, boxMin, boxMax, floorY));
            }

            int left = Math.max(0, duration - elapsed) + RESEND_EXTRA_TICKS;
            BlockPos min = boxMin;
            BlockPos max = boxMax;
            GCSchedulerUtil.runLater(server, left, () -> resendInterior(player, interior, min, max));
        }
    }

    private static void resendInterior(ServerPlayer player, ServerLevel interior, BlockPos min, BlockPos max) {
        if (player.isRemoved() || player.level() != interior) return;
        for (int cx = min.getX() >> 4; cx <= max.getX() >> 4; cx++) {
            for (int cz = min.getZ() >> 4; cz <= max.getZ() >> 4; cz++) {
                LevelChunk chunk = interior.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue;
                player.connection.send(new ClientboundLevelChunkWithLightPacket(chunk, interior.getLightEngine(),
                        null, null));
            }
        }
    }
}
