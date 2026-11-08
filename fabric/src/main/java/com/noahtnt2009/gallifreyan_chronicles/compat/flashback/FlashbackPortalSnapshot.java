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
import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
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
            int dimId = map.toIntegerId(level.dimension());

            /* the worlds the portals lead to (the tardis inside). flashback only
               saves the world you stand in, so the door led to an empty world */
            Set<ResourceKey<Level>> done = new HashSet<>();
            int chunks = 0;
            for (Entity entity : level.entitiesForRendering()) {
                if (!(entity instanceof Portal portal) || portal.getAxisW() == null) continue;
                var dest = portal.getDestDim();
                if (dest == null || dest.equals(level.dimension()) || !done.add(dest)) continue;
                if (!map.containsDimId(dest)) continue;
                ClientLevel destLevel = ClientWorldLoader.peekWorld(dest);
                if (destLevel == null || !(destLevel.getChunkSource() instanceof ImmPtlClientChunkMap chunkMap)) continue;
                int destId = map.toIntegerId(dest);
                for (LevelChunk chunk : chunkMap.getCopiedChunkList()) {
                    var chunkPacket = new ClientboundLevelChunkWithLightPacket(
                            chunk, destLevel.getLightEngine(), null, null);
                    out.accept(new ClientboundCustomPayloadPacket(
                            new PacketRedirection.Payload(destId, chunkPacket)));
                    chunks++;
                }
            }

            int count = 0;
            for (Entity entity : level.entitiesForRendering()) {
                if (!(entity instanceof Portal portal) || portal.getAxisW() == null) continue;
                CompoundTag tag = new CompoundTag();
                save(portal, tag);
                out.accept(new ClientboundCustomPayloadPacket(new ImmPtlNetworking.PortalSyncPacket(
                        portal.getId(), portal.getUUID(), portal.getType(), dimId,
                        portal.getX(), portal.getY(), portal.getZ(), tag)));
                count++;
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
