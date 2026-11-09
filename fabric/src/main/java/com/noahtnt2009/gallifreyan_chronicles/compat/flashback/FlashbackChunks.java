package com.noahtnt2009.gallifreyan_chronicles.compat.flashback;

import net.fabricmc.loader.api.FabricLoader;
import org.joml.Vector3d;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.player.Player;

/* immersive portals turns off vanilla chunk tracking and sending,
   and flashback needs both to show the recorded world.
   so with both installed we hand each replay chunk to the viewer ourselves.
   this class never touches flashback classes, so it is safe without flashback. */
public final class FlashbackChunks {
    private static final boolean ACTIVE = FabricLoader.getInstance().isModLoaded("flashback")
            && FabricLoader.getInstance().isModLoaded("seamlessportals");
    /* replay features that threw once. each is logged once and the game carries on,
       a broken replay feature should never kick you out of the replay */
    private static final Set<String> FAILED = ConcurrentHashMap.newKeySet();

    private FlashbackChunks() {
    }

    private static void failed(String what, Throwable t) {
        if (FAILED.add(what)) Constants.LOG.warn("[replay] {} failed, skipping it from now on: {}", what, t.toString());
    }

    /* true while a flashback replay is running */
    public static boolean inReplay() {
        try {
            return ACTIVE && FlashbackChunksImpl.inReplay();
        } catch (Throwable t) {
            failed("replay check", t);
            return false;
        }
    }

    /* a chunk is about to be thrown away on the client. in a replay we keep it */
    public static boolean keepChunk(String what) {
        return inReplay();
    }

    /* in a replay, a packet for a dimension the client doesn't know yet gets dropped
       instead of kicking you out of the replay */
    public static boolean dropUnknownDim(int dimId) {
        if (!inReplay()) return false;
        try {
            return !FlashbackChunksImpl.clientKnowsDim(dimId);
        } catch (Throwable t) {
            failed("unknown dimension check", t);
            return false;
        }
    }

    /* the replay server is about to unwrap a packet meant for another world.
       true = we passed it on to the camera instead. see ReplayRedirectForwardMixin */
    public static boolean passOnFromReplayServer(Object listener, int dimId, Packet<?> packet) {
        if (!ACTIVE || !listener.getClass().getName().startsWith("com.moulberry.flashback.")) return false;
        try {
            return FlashbackChunksImpl.passOn(dimId, packet);
        } catch (Throwable t) {
            failed("passing other-world packets on", t);
            return false;
        }
    }

    /* flashback is writing a snapshot while recording. see FlashbackSnapshotRootMixin */
    public static void writeSnapshotFromOutside(Runnable original) {
        if (!ACTIVE || FAILED.contains("snapshot from outside")) {
            original.run();
            return;
        }
        /* if our part fails before flashback's snapshot ran, still let it run once */
        boolean[] started = {false};
        try {
            FlashbackChunksImpl.writeSnapshotFromOutside(() -> {
                started[0] = true;
                original.run();
            });
        } catch (Throwable t) {
            if (started[0]) {
                if (t instanceof RuntimeException e) throw e;
                if (t instanceof Error e) throw e;
                throw new RuntimeException(t);
            }
            failed("snapshot from outside", t);
            original.run();
        }
    }

    /* immersive portals is moving the camera through a door (its argument is the portal) */
    public static void onCameraTeleport(Object portal) {
        if (!inReplay()) return;
        try {
            FlashbackChunksImpl.onCameraTeleport(portal);
        } catch (Throwable t) {
            failed("keyframes through doors", t);
        }
    }

    /* true while flashback keyframes are moving the camera */
    public static boolean keyframesMovingCamera() {
        if (!inReplay()) return false;
        try {
            return FlashbackChunksImpl.keyframesMovingCamera();
        } catch (Throwable t) {
            failed("keyframes through doors", t);
            return false;
        }
    }

    /* a keyframe camera position is about to be applied. returns {x, y, z, yaw, pitch} */
    public static double[] keyframeCamera(double x, double y, double z, float yaw, float pitch) {
        if (!inReplay() || FAILED.contains("keyframes through doors")) return new double[]{x, y, z, yaw, pitch};
        try {
            return FlashbackChunksImpl.keyframeCamera(x, y, z, yaw, pitch);
        } catch (Throwable t) {
            failed("keyframes through doors", t);
            return new double[]{x, y, z, yaw, pitch};
        }
    }

    /* a camera keyframe was just made from the camera, yawPitch = {yaw, pitch} */
    public static void onKeyframeCreated(Vector3d position, float[] yawPitch) {
        if (!inReplay()) return;
        try {
            FlashbackChunksImpl.onKeyframeCreated(position, yawPitch);
        } catch (Throwable t) {
            failed("keyframes through doors", t);
        }
    }

    /* a packet is about to go to the replay server. true = don't send it */
    public static boolean dropOutgoing(Packet<?> packet) {
        if (!inReplay()) return false;
        try {
            return FlashbackChunksImpl.dropOutgoing(packet);
        } catch (Throwable t) {
            failed("camera through doors (sending)", t);
            return false;
        }
    }

    /* a packet from the replay server is about to be handled. true = we handled it */
    public static boolean handleIncoming(Packet<?> packet, Runnable original) {
        if (!inReplay() || FAILED.contains("camera through doors (receiving)")) return false;
        try {
            return FlashbackChunksImpl.handleIncoming(packet, original);
        } catch (Throwable t) {
            failed("camera through doors (receiving)", t);
            return false;
        }
    }

    public static void newExportFrame() {
        if (!ACTIVE) return;
        try {
            FlashbackChunksImpl.newExportFrame();
        } catch (Throwable t) {
            failed("portal count reset", t);
        }
    }

    public static boolean skipForget() {
        return inReplay();
    }

    /* a portal with no shape is about to come into the replay world. it crashes
       immersive portals in lots of places, so it isn't added at all */
    public static boolean isShapelessPortalInReplay(Entity entity) {
        if (!inReplay()) return false;
        try {
            return FlashbackChunksImpl.isShapelessPortal(entity);
        } catch (Throwable t) {
            failed("shapeless portal check", t);
            return false;
        }
    }

    /* true when a player should NOT be removed: we are in a replay and
       the removal came through immersive portals' other-dimension packets */
    public static boolean keepPlayer(ClientLevel level, int id) {
        if (!inReplay()) return false;
        try {
            return FlashbackChunksImpl.isRedirectedPacket() && level.getEntity(id) instanceof Player;
        } catch (Throwable t) {
            failed("keep player", t);
            return false;
        }
    }

    /* recorded movement is about to be applied. see FlashbackPuppet */
    public static void scanMoves(Object replayServer, RegistryFriendlyByteBuf buf) {
        if (!ACTIVE) return;
        try {
            FlashbackChunksImpl.scanMoves(replayServer, buf);
        } catch (Throwable t) {
            failed("player copy in tardis", t);
        }
    }

    /* flashback is writing a snapshot, add the portals. see FlashbackPortalSnapshot */
    public static void writePortalSnapshot(Consumer<Packet<? super ClientGamePacketListener>> out) {
        if (!ACTIVE) return;
        try {
            FlashbackChunksImpl.writePortalSnapshot(out);
        } catch (Throwable t) {
            failed("portals in snapshot", t);
        }
    }

    /* the recorded player we hid while their copy is inside a tardis */
    public static boolean isHiddenRecordedPlayer(Entity entity) {
        if (!ACTIVE || !entity.level().isClientSide()) return false;
        try {
            return FlashbackChunksImpl.isHiddenRecordedPlayer(entity);
        } catch (Throwable t) {
            failed("hide recorded player", t);
            return false;
        }
    }

    /* immersive portals' dimension list just arrived.
       true when the packet was handled merged, so the original should be skipped */
    public static boolean mergeDimSync(Object packet) {
        if (!ACTIVE) return false;
        try {
            return FlashbackChunksImpl.mergeDimSync(packet);
        } catch (Throwable t) {
            failed("dimension list merge", t);
            return false;
        }
    }

    public static void registerClient() {
        if (!ACTIVE) return;
        try {
            FlashbackChunksImpl.registerClient();
        } catch (Throwable t) {
            failed("replay setup", t);
        }
    }

    /* jumping on the timeline: drop the tardis copy of the player (client thread) */
    public static void clearPuppet() {
        if (!ACTIVE) return;
        try {
            FlashbackChunksImpl.clearPuppet();
        } catch (Throwable t) {
            failed("player copy cleanup", t);
        }
    }

    public static void forward(ServerLevel level, ClientboundLevelChunkWithLightPacket packet) {
        if (!ACTIVE) return;
        try {
            FlashbackChunksImpl.forward(level, packet);
        } catch (Throwable t) {
            failed("chunk forwarding", t);
        }
    }
}
