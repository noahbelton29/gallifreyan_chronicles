package com.noahtnt2009.gallifreyan_chronicles.compat.flashback;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import java.util.function.Consumer;
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

    private FlashbackChunks() {
    }

    /* true while a flashback replay is running */
    public static boolean inReplay() {
        return ACTIVE && FlashbackChunksImpl.inReplay();
    }

    /* a chunk is about to be thrown away on the client. in a replay we keep it */
    public static boolean keepChunk(String what) {
        return inReplay();
    }

    /* true when a "forget chunk" packet should be skipped (only in a replay) */
    /* in a replay, a packet for a dimension the client doesn't know yet gets dropped
       instead of kicking you out of the replay */
    public static boolean dropUnknownDim(int dimId) {
        return inReplay() && !FlashbackChunksImpl.clientKnowsDim(dimId);
    }

    public static void newExportFrame() {
        if (ACTIVE) FlashbackChunksImpl.newExportFrame();
    }

    public static boolean skipForget() {
        return inReplay();
    }

    /* a portal with no shape just came into the replay world. it crashes
       immersive portals in lots of places, so we remove it straight away */
    public static void dropShapelessPortal(Entity entity) {
        if (!inReplay() || !FlashbackChunksImpl.isShapelessPortal(entity)) return;
        entity.discard();
    }

    /* true when a player should NOT be removed: we are in a replay and
       the removal came through immersive portals' other-dimension packets */
    public static boolean keepPlayer(ClientLevel level, int id) {
        if (!inReplay() || !FlashbackChunksImpl.isRedirectedPacket()) return false;
        return level.getEntity(id) instanceof Player;
    }

    /* recorded movement is about to be applied. see FlashbackPuppet */
    public static void scanMoves(Object replayServer, RegistryFriendlyByteBuf buf) {
        if (ACTIVE) FlashbackChunksImpl.scanMoves(replayServer, buf);
    }

    /* flashback is writing a snapshot, add the portals. see FlashbackPortalSnapshot */
    public static void writePortalSnapshot(Consumer<Packet<? super ClientGamePacketListener>> out) {
        if (ACTIVE) FlashbackChunksImpl.writePortalSnapshot(out);
    }

    /* the recorded player we hid while their copy is inside a tardis */
    public static boolean isHiddenRecordedPlayer(Entity entity) {
        return ACTIVE && entity.level().isClientSide() && FlashbackChunksImpl.isHiddenRecordedPlayer(entity);
    }

    /* immersive portals' dimension list just arrived */
    /* true when the packet was handled merged, so the original should be skipped */
    public static boolean mergeDimSync(Object packet) {
        return ACTIVE && FlashbackChunksImpl.mergeDimSync(packet);
    }

    public static void registerClient() {
        if (ACTIVE) FlashbackChunksImpl.registerClient();
    }

    /* jumping on the timeline: drop the tardis copy of the player (client thread) */
    public static void clearPuppet() {
        if (ACTIVE) FlashbackChunksImpl.clearPuppet();
    }

    public static void forward(ServerLevel level, ClientboundLevelChunkWithLightPacket packet) {
        if (ACTIVE) FlashbackChunksImpl.forward(level, packet);
    }
}
