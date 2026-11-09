package com.noahtnt2009.gallifreyan_chronicles.compat.flashback;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import qouteall.imm_ptl.core.network.ImmPtlNetworking;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.q_misc_util.MiscNetworking;
import qouteall.q_misc_util.dimension.DimIntIdMap;
import qouteall.q_misc_util.dimension.DimensionIntId;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.world.level.chunk.LevelChunk;
import qouteall.imm_ptl.core.ClientWorldLoader;
import qouteall.imm_ptl.core.chunk_loading.ImmPtlClientChunkMap;
import qouteall.imm_ptl.core.network.PacketRedirection;

/* a flashback snapshot saves portals as plain entities with no shape, and it
   doesn't save immersive portals' list of dimensions. so a recording started
   after a tardis was placed had no working portal. we add both to the snapshot */
final class FlashbackPortalSnapshot {
    private static Method saveMethod;
    private static int errors;

    private FlashbackPortalSnapshot() {
    }

    static void write(Consumer<Packet<? super ClientGamePacketListener>> out) {
        try {
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc.level;
            if (level == null) return;

            /* the dimension list first, so the client knows the tardis worlds.
               only possible in singleplayer, where the server is right here */
            MinecraftServer server = mc.getSingleplayerServer();
            if (server != null) out.accept(MiscNetworking.DimIdSyncPacket.createPacket(server));

            DimIntIdMap map = DimensionIntId.getClientMap();
            if (map == null || !map.containsDimId(level.dimension())) return;

            /* every other loaded world (the tardis inside, or the overworld when you
               stand inside the tardis). flashback only saves the world you stand in.
               it also takes a new snapshot when you walk through the door, and an
               export plays those, so the outside portal has to be in it too */
            for (ClientLevel other : ClientWorldLoader.getClientWorlds()) {
                if (other == level || !map.containsDimId(other.dimension())) continue;
                if (!(other.getChunkSource() instanceof ImmPtlClientChunkMap chunkMap)) continue;
                int otherId = map.toIntegerId(other.dimension());
                for (LevelChunk chunk : chunkMap.getCopiedChunkList()) {
                    var chunkPacket = new ClientboundLevelChunkWithLightPacket(
                            chunk, other.getLightEngine(), null, null);
                    out.accept(new ClientboundCustomPayloadPacket(
                            new PacketRedirection.Payload(otherId, chunkPacket)));
                }
            }

            /* the portals in every loaded world, with the world they're in */
            for (ClientLevel world : ClientWorldLoader.getClientWorlds()) {
                if (!map.containsDimId(world.dimension())) continue;
                int worldId = map.toIntegerId(world.dimension());
                for (Entity entity : world.entitiesForRendering()) {
                    if (!(entity instanceof Portal portal) || portal.getAxisW() == null) continue;
                    CompoundTag tag = new CompoundTag();
                    save(portal, tag);
                    out.accept(new ClientboundCustomPayloadPacket(new ImmPtlNetworking.PortalSyncPacket(
                            portal.getId(), portal.getUUID(), portal.getType(), worldId,
                            portal.getX(), portal.getY(), portal.getZ(), tag)));
                }
            }
        } catch (Exception e) {
            if (++errors <= 3) Constants.LOG.warn("[replay] couldn't save portals in the snapshot: {}", e.toString());
        }
    }

    /* Portal.addAdditionalSaveData(CompoundTag) is protected */
    private static void save(Portal portal, CompoundTag tag) throws Exception {
        if (saveMethod == null) {
            saveMethod = Portal.class.getDeclaredMethod("addAdditionalSaveData", CompoundTag.class);
            saveMethod.setAccessible(true);
        }
        saveMethod.invoke(portal, tag);
    }
}
