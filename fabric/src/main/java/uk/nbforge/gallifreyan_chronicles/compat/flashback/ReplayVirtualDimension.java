package uk.nbforge.gallifreyan_chronicles.compat.flashback;

import com.moulberry.flashback.Flashback;
import com.moulberry.flashback.playback.ReplayPlayer;
import com.moulberry.flashback.playback.ReplayServer;
import com.moulberry.flashback.state.EditorState;
import com.moulberry.flashback.state.EditorStateManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundStartConfigurationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import qouteall.imm_ptl.core.ClientWorldLoader;
import qouteall.imm_ptl.core.network.ImmPtlNetworking;
import qouteall.imm_ptl.core.teleportation.ClientTeleportationManager;

/* in a replay the camera really goes through doors now: immersive portals moves it
   into the tardis world on the client like in the real game. the replay server only
   has the overworld and can't follow, so only the client moves:
   - the "i went through a portal" message and the camera's movement aren't sent while
     it's in another world, so the server keeps it parked outside and doesn't pull it back
   - packets from the replay server are for its world (the overworld), so while the
     camera is somewhere else they're applied to that world, not the one we're in
   flying back out through the door puts it back in the overworld */
final class ReplayVirtualDimension {
    private ReplayVirtualDimension() {
    }

    /* the world the replay server has the camera in */
    static @Nullable ResourceKey<Level> serverDim() {
        ReplayServer server = Flashback.getReplayServer();
        if (server == null) return null;
        for (ReplayPlayer viewer : server.getReplayViewers()) return viewer.level().dimension();
        return null;
    }

    /* true while the camera is in a different world than the replay server thinks */
    static boolean isAway() {
        Minecraft mc = Minecraft.getInstance();
        ResourceKey<Level> server = serverDim();
        return server != null && mc.player != null && !mc.player.level().dimension().equals(server);
    }

    static boolean dropOutgoing(Packet<?> packet) {
        if (packet instanceof ServerboundCustomPayloadPacket custom
                && custom.payload() instanceof ImmPtlNetworking.TeleportPacket) return true;
        return packet instanceof ServerboundMovePlayerPacket && isAway();
    }

    /* where the camera was before flashback reloaded the world (timeline jump) */
    private static @Nullable ResourceKey<Level> pendingDim;
    private static @Nullable Vec3 pendingEye;
    private static int pendingTicks;

    /* going back on the timeline reloads the world, and the camera lands in whatever
       world the replay has. put it back in the world it was in before, so only doors
       ever change which world the camera is in */
    static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (Flashback.getReplayServer() == null) {
                pendingDim = null;
                return;
            }
            if (pendingDim == null || client.player == null || --pendingTicks > 0) return;
            if (client.player.level().dimension().equals(pendingDim)) {
                pendingDim = null;
                return;
            }
            if (ClientWorldLoader.getOptionalWorld(pendingDim) != null) {
                ClientTeleportationManager.forceTeleportPlayer(pendingDim, pendingEye);
                pendingDim = null;
            } else if (pendingTicks < -100) {
                /* that world never came back, give up */
                pendingDim = null;
            }
        });
    }

    private static void rememberCamera() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || pendingDim != null) return;
        pendingDim = mc.player.level().dimension();
        pendingEye = mc.player.getEyePosition();
        pendingTicks = 20;
    }

    /* true when we handled it */
    static boolean handleIncoming(Packet<?> packet, Runnable original) {
        if (packet instanceof ClientboundCustomPayloadPacket) return false;
        if (packet instanceof ClientboundLoginPacket || packet instanceof ClientboundRespawnPacket) {
            rememberCamera();
            return false;
        }
        if (!isAway()) return false;
        if (packet instanceof ClientboundLoginPacket || packet instanceof ClientboundRespawnPacket
                || packet instanceof ClientboundStartConfigurationPacket
                || packet instanceof ClientboundFinishConfigurationPacket) {
            return false;
        }
        ResourceKey<Level> server = serverDim();
        if (packet instanceof ClientboundPlayerPositionPacket) {
            /* flashback moving the camera: put it back in its world first */
            comeBack(server);
            return false;
        }
        ClientWorldLoader.withSwitchedWorldFailSoft(server, original);
        return true;
    }

    private static void comeBack(ResourceKey<Level> server) {
        Minecraft mc = Minecraft.getInstance();
        ReplayServer replay = Flashback.getReplayServer();
        Vec3 pos = mc.player != null ? mc.player.position() : Vec3.ZERO;
        if (replay != null) {
            for (ReplayPlayer viewer : replay.getReplayViewers()) {
                pos = viewer.position();
                break;
            }
        }
        ClientTeleportationManager.forceTeleportPlayer(server, pos);
    }

    /* our sounds: the world the camera is in */
    static ClientLevel heardLevel() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.level() instanceof ClientLevel level) return level;
        return mc.level;
    }

    /* where flashback hears from: its "audio camera" when it's set, else the camera */
    static Vec3 heardPos() {
        Minecraft mc = Minecraft.getInstance();
        EditorState state = EditorStateManager.getCurrent();
        Camera audio = state != null ? state.getAudioCamera() : null;
        if (audio != null) return audio.position();
        Entity camera = mc.getCameraEntity();
        if (camera != null) return camera.getEyePosition();
        return mc.gameRenderer.mainCamera().position();
    }
}
