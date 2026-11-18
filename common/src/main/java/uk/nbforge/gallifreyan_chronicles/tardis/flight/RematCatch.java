package uk.nbforge.gallifreyan_chronicles.tardis.flight;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.network.tardis.RematCatchPayload;
import uk.nbforge.gallifreyan_chronicles.platform.Services;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.interior.TardisInteriors;
import uk.nbforge.gallifreyan_chronicles.util.SchedulerUtil;

public final class RematCatch {
    private static final Vec3i FALLBACK_SIZE = new Vec3i(48, 32, 48);
    private static final int RESEND_EXTRA_TICKS = 5;

    private RematCatch() {}

    public static void onRemat(TardisExteriorBlockEntity exterior, long startTick) {
        if (!(exterior.getLevel() instanceof ServerLevel level)) return;
        UUID tardisId = exterior.getTardisId();
        if (tardisId == null) return;
        BlockPos pos = exterior.getBlockPos();
        AABB footprint = new AABB(pos).expandTowards(0.0, 1.0, 0.0);
        List<ServerPlayer> caught = level.getEntitiesOfClass(
                ServerPlayer.class, footprint, player -> !player.isSpectator() && player.isAlive());
        if (caught.isEmpty()) return;

        MinecraftServer server = level.getServer();
        Tardis tardis = Tardis.get(server, tardisId).orElse(null);
        if (tardis == null) return;
        ServerLevel interior = TardisInteriors.prepareInterior(tardis);
        if (interior == null) return;
        TardisInteriors.Arrival arrival = TardisInteriors.arrival(tardis, interior);

        BlockPos origin = tardis.data().getInteriorOrigin();
        if (origin == null) origin = TardisInteriors.ORIGIN;
        Vec3i size = server.getStructureTemplateManager()
                .get(tardis.interior().structure())
                .map(StructureTemplate::getSize)
                .orElse(FALLBACK_SIZE);
        BlockPos boxMin = origin;
        BlockPos boxMax = origin.offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1);
        int floorY = BlockPos.containing(arrival.pos()).getY();

        String fadeType = exterior.travel().getFadeType().id();
        int duration = Math.max(1, exterior.travel().getFadeType().timing().fadeInTicks());
        int elapsed = (int) Math.max(0L, level.getGameTime() - startTick);

        for (ServerPlayer player : caught) {
            TardisInteriors.markArriving(player);
            player.teleportTo(
                    interior, arrival.pos().x, arrival.pos().y, arrival.pos().z, Set.of(), arrival.yaw(), 0.0f, true);

            Services.NETWORK.send(
                    player,
                    new RematCatchPayload(player.getUUID(), fadeType, elapsed, duration, true, boxMin, boxMax, floorY));
            for (ServerPlayer other : interior.players()) {
                if (other == player) continue;
                Services.NETWORK.send(
                        other,
                        new RematCatchPayload(
                                player.getUUID(), fadeType, elapsed, duration, false, boxMin, boxMax, floorY));
            }

            int left = Math.max(0, duration - elapsed) + RESEND_EXTRA_TICKS;
            BlockPos min = boxMin;
            BlockPos max = boxMax;
            SchedulerUtil.runLater(server, left, () -> resendInterior(player, interior, min, max));
        }
    }

    private static void resendInterior(ServerPlayer player, ServerLevel interior, BlockPos min, BlockPos max) {
        if (player.isRemoved() || player.level() != interior) return;
        for (int cx = min.getX() >> 4; cx <= max.getX() >> 4; cx++) {
            for (int cz = min.getZ() >> 4; cz <= max.getZ() >> 4; cz++) {
                LevelChunk chunk = interior.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue;
                player.connection.send(
                        new ClientboundLevelChunkWithLightPacket(chunk, interior.getLightEngine(), null, null));
            }
        }
    }
}
