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
import org.joml.Vector3d;
import com.noahtnt2009.gallifreyan_chronicles.client.sound.SoundListener;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import qouteall.imm_ptl.core.render.context_management.RenderStates;

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

    /* only packets for other worlds. ones for the replay's own world (the overworld
       chunks while the player was in the tardis) must go into the replay server,
       or it never has them and the world is void */
    static boolean passOn(int dimId, Packet<?> packet) {
        ReplayServer server = Flashback.getReplayServer();
        if (server == null) return false;
        ResourceKey<Level> dim = DimensionIntId.getClientMap().fromIntegerIdNullable(dimId);
        if (dim == null || dim.equals(replayDim(server))) return false;
        for (ReplayPlayer viewer : server.getReplayViewers()) viewer.connection.send(packet);
        return true;
    }

    /* the world the replay server is playing in */
    private static @Nullable ResourceKey<Level> replayDim(ReplayServer server) {
        for (ReplayPlayer viewer : server.getReplayViewers()) return viewer.level().dimension();
        for (ServerLevel level : server.getAllLevels()) return level.dimension();
        return null;
    }

    static void writeSnapshotFromOutside(Runnable original) {
        FlashbackSnapshotRoot.write(original);
    }

    static void onCameraTeleport(Object portal) {
        if (portal instanceof Portal p) ReplayKeyframePortal.onTeleport(p);
    }

    static boolean keyframesMovingCamera() {
        return ReplayKeyframePortal.keyframesMovingCamera();
    }

    static double[] keyframeCamera(double x, double y, double z, float yaw, float pitch) {
        return ReplayKeyframePortal.apply(x, y, z, yaw, pitch);
    }

    static void onKeyframeCreated(Vector3d position, float[] yawPitch) {
        ReplayKeyframePortal.onKeyframeCreated(position, yawPitch);
    }

    static boolean dropOutgoing(Packet<?> packet) {
        return ReplayVirtualDimension.dropOutgoing(packet);
    }

    static boolean handleIncoming(Packet<?> packet, Runnable original) {
        return ReplayVirtualDimension.handleIncoming(packet, original);
    }

    static void newExportFrame() {
        if (!Flashback.isInReplay()) return;
        RenderStates.portalRenderInfos = new ArrayList<>();
        RenderStates.portalsRenderedThisFrame = 0;
    }

    static boolean clientKnowsDim(int dimId) {
        return DimensionIntId.getClientMap().containsIntId(dimId);
    }

    static boolean mergeDimSync(Object packet) {
        return FlashbackDimMerge.merge(packet);
    }

    static void registerClient() {
        FlashbackPuppet.registerClient();
        FlashbackProbe.register();
        FlashbackSnapshotRoot.register();
        ReplayVirtualDimension.register();
        SoundListener.setOverride(new SoundListener.Override() {
            @Override
            public Level level() {
                return Flashback.isInReplay() ? ReplayVirtualDimension.heardLevel() : null;
            }

            @Override
            public Vec3 pos() {
                return ReplayVirtualDimension.heardPos();
            }
        });
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
                /* if the client doesn't know this world yet, skip it. sending it
                   plain put tardis chunks into the overworld (holes in the ground) */
                if (dimId == Integer.MIN_VALUE) continue;
                viewer.connection.send(new ClientboundCustomPayloadPacket(
                        new PacketRedirection.Payload(dimId, packet)));
            }
        }
    }
}
