package uk.nbforge.gallifreyan_chronicles.compat.flashback;

import com.mojang.authlib.GameProfile;
import com.moulberry.flashback.playback.ReplayServer;
import uk.nbforge.gallifreyan_chronicles.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import qouteall.imm_ptl.core.ClientWorldLoader;
import com.moulberry.flashback.Flashback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.SectionPos;
import qouteall.imm_ptl.core.chunk_loading.ImmPtlClientChunkMap;
import qouteall.imm_ptl.core.portal.Portal;

/**
   When the recorded player goes into a TARDIS the replay server does not have that world, so Flashback cannot move them.
   Instead we read their recorded movement and move a copy, the puppet, in the TARDIS world of the client.
   Immersive Portals then draws it through the door like in the real game.
   The real player in the overworld is hidden while the puppet is out.
*/
public final class FlashbackPuppet {
    private static final int PUPPET_ID = Integer.MAX_VALUE - 7;
    private static RemotePlayer puppet;
    private static volatile boolean hidden;
    private static volatile int realId = Integer.MIN_VALUE;
    private static int errors;
    private static int lastTickCount = -1;

    // The TARDIS world may not tick our copy and then its legs never move, so tick it if nothing ticked it since last time
    // Ticks until we rebuild the render of the world, like pressing F3 plus A
    private static int rebuildIn = -1;
    private static Object lastLevel;

    /**
       In a replay the renderer sometimes only built part of the world at the start or after jumping on the timeline, leaving holes.
       Rebuilding a moment later once the chunks are in fixes it.
    */
    static void scheduleRebuild() {
        rebuildIn = 40;
    }

    // On first play Flashback loads the start of the replay before the camera joined, so the TARDIS world of Immersive Portals was not ready and the door drew black
    // Jumping on the timeline loads it again with everything ready, which is why going back fixed it, so we do that one jump ourselves
    private static Object lastReplayServer;
    private static int waitTicks = -1;
    private static int step;
    private static int backTo;

    private static void tickFirstPlayReload() {
        // Never jump the timeline in the middle of an export
        if (Flashback.isExporting()) {
            waitTicks = -1;
            return;
        }
        ReplayServer server = Flashback.getReplayServer();
        if (server == null) {
            lastReplayServer = null;
            return;
        }
        if (server != lastReplayServer) {
            lastReplayServer = server;
            waitTicks = 20;
            step = 0;
        }
        if (waitTicks < 0 || --waitTicks > 0) return;
        if (step == 0) {
            // Only needed when a door leads to an empty world, each reload also restarts Voxy so do not do it for nothing
            if (!portalLeadsNowhere(Minecraft.getInstance())) {
                waitTicks = -1;
                return;
            }
            // Flashback only reloads when going backwards, so step one tick forward first and then back a few ticks later
            backTo = server.getReplayTick();
            if (backTo + 1 >= server.getTotalReplayTicks()) {
                waitTicks = -1;
                return;
            }
            server.goToReplayTick(backTo + 1);
            step = 1;
            waitTicks = 5;
        } else {
            server.goToReplayTick(backTo);
            waitTicks = -1;
        }
    }

    /** Marks every chunk section around the camera to be built again, like F3 plus A but that also restarts the renderer of Voxy and this does not. */
    private static void rebuildSections(Minecraft client) {
        ClientLevel level = client.level;
        SectionPos camera = SectionPos.of(client.gameRenderer.mainCamera().position());
        int radius = client.options.getEffectiveRenderDistance() + 1;
        client.levelExtractor.setSectionRangeDirty(
                camera.x() - radius, level.getMinSectionY(), camera.z() - radius,
                camera.x() + radius, level.getMaxSectionY(), camera.z() + radius);
    }

    /** True if a portal here leads to a world with no chunks, the black door. */
    private static boolean portalLeadsNowhere(Minecraft client) {
        if (client.level == null) return false;
        for (Entity entity : client.level.entitiesForRendering()) {
            if (!(entity instanceof Portal portal) || portal.getAxisW() == null) continue;
            ResourceKey<Level> dest = portal.getDestDim();
            if (dest == null || dest.equals(client.level.dimension())) continue;
            ClientLevel destLevel = ClientWorldLoader.peekWorld(dest);
            if (destLevel == null) return true;
            if (destLevel.getChunkSource() instanceof ImmPtlClientChunkMap map && map.getCopiedChunkList().isEmpty()) return true;
        }
        return false;
    }

    static void registerClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tickFirstPlayReload();
            if (client.level != lastLevel) {
                lastLevel = client.level;
                if (client.level != null && FlashbackChunksImpl.inReplay()) scheduleRebuild();
            }
            if (rebuildIn > 0 && --rebuildIn == 0) {
                rebuildIn = -1;
                if (client.level != null && FlashbackChunksImpl.inReplay()) rebuildSections(client);
            }
            if (puppet == null || puppet.isRemoved()) return;
            // Copies sprinting and sneaking from the hidden real player so the copy makes the sprint particles and poses itself
            Entity real = client.level == null ? null : client.level.getEntity(realId);
            if (real != null) {
                puppet.setSprinting(real.isSprinting());
                puppet.setShiftKeyDown(real.isShiftKeyDown());
                puppet.setPose(real.getPose());
            }
            if (puppet.tickCount == lastTickCount) {
                // Same as the game ticking an entity
                puppet.commonTick();
                puppet.tick();
            }
            lastTickCount = puppet.tickCount;
        });
    }

    private FlashbackPuppet() {
    }

    /** Reads the move entities data of Flashback, the caller resets the buffer after. */
    static void scan(ReplayServer server, RegistryFriendlyByteBuf buf) {
        try {
            int localId = server.getLocalPlayerId();
            int levelCount = buf.readVarInt();
            for (int i = 0; i < levelCount; i++) {
                ResourceKey<Level> dim = buf.readResourceKey(Registries.DIMENSION);
                boolean exists = server.getLevel(dim) != null;
                int count = buf.readVarInt();
                for (int j = 0; j < count; j++) {
                    int id = buf.readVarInt();
                    double x = buf.readDouble(), y = buf.readDouble(), z = buf.readDouble();
                    float yaw = buf.readFloat(), pitch = buf.readFloat(), head = buf.readFloat();
                    boolean onGround = buf.readBoolean();
                    if (id == localId)
                        recordedPlayerMoved(server, localId, dim, exists, x, y, z, yaw, pitch, head, onGround);
                }
            }
        } catch (RuntimeException e) {
            if (++errors <= 3) Constants.LOG.warn("[replay] couldn't read recorded movement: {}", e.toString());
        }
    }

    private static void recordedPlayerMoved(ReplayServer server, int localId, ResourceKey<Level> dim, boolean exists,
                                            double x, double y, double z, float yaw, float pitch, float head, boolean onGround) {
        realId = localId;
        Entity real = null;
        for (ServerLevel level : server.getAllLevels()) {
            real = level.getEntity(localId);
            if (real != null) break;
        }
        if (!exists) {
            // Hide the real one and move the puppet
            if (real != null && !real.isInvisible()) {
                real.setInvisible(true);
                hidden = true;
            }
            if (real instanceof Player p) {
                GameProfile profile = p.getGameProfile();
                Minecraft.getInstance().execute(() -> place(dim, profile, x, y, z, yaw, pitch, head, onGround));
            }
        } else {
            if (hidden && real != null) {
                real.setInvisible(false);
                hidden = false;
            }
            if (puppet != null) Minecraft.getInstance().execute(FlashbackPuppet::remove);
        }
    }

    /** Client thread. */
    private static void place(ResourceKey<Level> dim, GameProfile profile, double x, double y, double z,
                              float yaw, float pitch, float head, boolean onGround) {
        ClientLevel level = ClientWorldLoader.getOptionalWorld(dim);
        if (level == null) return;
        if (puppet == null || puppet.isRemoved() || puppet.level() != level) {
            remove();
            puppet = new RemotePlayer(level, profile);
            puppet.setId(PUPPET_ID);
            puppet.snapTo(x, y, z, yaw, pitch);
            puppet.setYHeadRot(head);
            level.addEntity(puppet);
            return;
        }
        // Same way the game moves other players, it slides there over a few ticks and that movement swings the arms and legs
        puppet.moveOrInterpolateTo(new Vec3(x, y, z), yaw, pitch);
        puppet.lerpHeadTo(head, 3);
        puppet.setOnGround(onGround);
    }

    static boolean isHiddenReal(Entity entity) {
        return hidden && entity.getId() == realId && entity != puppet;
    }

    /** Client thread. */
    public static void remove() {
        if (puppet == null) return;
        if (!puppet.isRemoved() && puppet.level() instanceof ClientLevel level) {
            level.removeEntity(PUPPET_ID, Entity.RemovalReason.DISCARDED);
        }
        puppet = null;
    }
}
