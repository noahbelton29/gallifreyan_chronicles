package com.noahtnt2009.gallifreyan_chronicles.compat.flashback;

import com.moulberry.flashback.Flashback;
import com.moulberry.flashback.playback.ReplayPlayer;
import com.moulberry.flashback.playback.ReplayServer;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerLevel;
import qouteall.imm_ptl.core.network.PacketRedirection;
import qouteall.q_misc_util.dimension.DimIntIdMap;
import qouteall.q_misc_util.dimension.DimensionIntId;

import java.util.function.Consumer;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import qouteall.imm_ptl.core.network.PacketRedirectionClient;
import qouteall.imm_ptl.core.portal.Portal;

final class FlashbackChunksImpl {

    private FlashbackChunksImpl() {
    }

    static boolean isShapelessPortal(Entity entity) {
        return entity instanceof Portal portal
                && (portal.getAxisW() == null || portal.getAxisH() == null);
    }

    static boolean isRedirectedPacket() {
        return PacketRedirectionClient.getIsProcessingRedirectedMessage();
    }

    static void scanMoves(Object replayServer, RegistryFriendlyByteBuf buf) {
        FlashbackPuppet.scan((ReplayServer) replayServer, buf);
    }

    static void writePortalSnapshot(Consumer<Packet<? super ClientGamePacketListener>> out) {
        FlashbackPortalSnapshot.write(out);
    }

    static boolean isHiddenRecordedPlayer(Entity entity) {
        return FlashbackPuppet.isHiddenReal(entity);
    }

    static void afterDimSync(Object packet) {
        FlashbackDimMerge.afterSync(packet);
    }

    static void registerClient() {
        FlashbackPuppet.registerClient();
    }

    /* jumping on the timeline */
    static void clearPuppet() {
        FlashbackPuppet.remove();
        FlashbackPuppet.scheduleRebuild();
    }

    /* the client's number for this world, or MIN_VALUE if the client doesn't know it */
    private static int clientDimId(ServerLevel level) {
        DimIntIdMap map = DimensionIntId.getClientMap();
        if (!map.containsDimId(level.dimension())) return Integer.MIN_VALUE;
        return map.toIntegerId(level.dimension());
    }

    static boolean inReplay() {
        return Flashback.isInReplay();
    }

    static void forward(ServerLevel level, ClientboundLevelChunkWithLightPacket packet) {
        if (!Flashback.isInReplay()) return;
        ReplayServer server = Flashback.getReplayServer();
        if (server == null) return;
        for (ReplayPlayer viewer : server.getReplayViewers()) {
            if (viewer.level() == level) {
                /* tag the chunk with its world, using the client's numbers, so a
                   tardis chunk can't end up in the overworld */
                int dimId = clientDimId(level);
                if (dimId == Integer.MIN_VALUE) {
                    /* client doesn't know the numbers yet, send it plain */
                    viewer.connection.send(packet);
                } else {
                    viewer.connection.send(new ClientboundCustomPayloadPacket(
                            new PacketRedirection.Payload(dimId, packet)));
                }
            }
        }
    }
}
