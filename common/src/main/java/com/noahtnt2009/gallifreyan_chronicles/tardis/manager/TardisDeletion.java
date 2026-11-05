package com.noahtnt2009.gallifreyan_chronicles.tardis.manager;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.compat.portal.TardisPortalBridge;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import com.noahtnt2009.gallifreyan_chronicles.mixin.MinecraftServerAccessor;
import com.noahtnt2009.gallifreyan_chronicles.tardis.interior.TardisDimensionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public final class TardisDeletion {
    private record Request(ResourceKey<Level> exitDimension, Vec3 exitPos) {
    }

    private static final Map<UUID, Request> PENDING = new LinkedHashMap<>();
    private static final int REMOVE_FLAGS = Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS;

    private TardisDeletion() {
    }

    public static void request(MinecraftServer server, UUID tardisId, ServerLevel exitLevel, BlockPos exitPos) {
        PENDING.put(tardisId, new Request(exitLevel.dimension(), Vec3.atBottomCenterOf(exitPos)));
    }

    public static boolean removeCompletely(MinecraftServer server, UUID tardisId) {
        TardisManager manager = TardisManager.get(server);
        if (manager.get(tardisId).isEmpty()) return false;
        ServerLevel exteriorLevel = server.getLevel(manager.getExteriorDimension(tardisId));
        if (exteriorLevel == null) exteriorLevel = server.overworld();
        BlockPos exteriorPos = manager.resolveExteriorBlockPos(tardisId);
        BlockPos exitPos = exteriorPos != null ? exteriorPos : exteriorLevel.getRespawnData().pos();
        if (exteriorPos != null) {
            exteriorLevel.getChunkAt(exteriorPos);
            BlockPos top = exteriorPos.above();
            if (exteriorLevel.getBlockState(top).is(GCBlocks.TARDIS_TOP)) {
                exteriorLevel.setBlock(top, Blocks.AIR.defaultBlockState(), REMOVE_FLAGS);
            }
            if (exteriorLevel.getBlockState(exteriorPos).is(GCBlocks.TARDIS)) {
                exteriorLevel.setBlock(exteriorPos, Blocks.AIR.defaultBlockState(), REMOVE_FLAGS);
            }
        }
        request(server, tardisId, exteriorLevel, exitPos);
        return true;
    }

    public static void tick(MinecraftServer server) {
        if (PENDING.isEmpty()) return;
        Map<UUID, Request> work = new LinkedHashMap<>(PENDING);
        PENDING.clear();
        work.forEach((tardisId, request) -> {
            try {
                delete(server, tardisId, request);
            } catch (Exception e) {
                Constants.LOG.error("[GC] Failed to delete TARDIS {}", tardisId, e);
            }
        });
    }

    private static void delete(MinecraftServer server, UUID tardisId, Request request) {
        TardisPortalBridge.removeTardis(server, tardisId);

        ResourceKey<Level> key = TardisDimensionManager.levelKey(tardisId);
        ServerLevel interior = server.getLevel(key);
        if (interior != null) {
            ServerLevel exit = server.getLevel(request.exitDimension());
            if (exit == null) exit = server.overworld();
            for (ServerPlayer player : List.copyOf(interior.players())) {
                player.teleportTo(exit, request.exitPos().x, request.exitPos().y, request.exitPos().z,
                        Set.of(), player.getYRot(), player.getXRot(), true);
            }
            TardisPortalBridge.beforeDimensionRemoved(interior);
            MinecraftServerAccessor accessor = (MinecraftServerAccessor) server;
            accessor.gallifreyan_chronicles$getLevels().remove(key);
            try {
                interior.close();
            } catch (IOException e) {
                Constants.LOG.error("[GC] Failed to close TARDIS dimension {}", key.identifier(), e);
            }
        }
        deleteFolder(((MinecraftServerAccessor) server).gallifreyan_chronicles$getStorageSource().getDimensionPath(key));

        TardisManager.get(server).remove(tardisId);
        Constants.LOG.info("[GC] Deleted TARDIS {} and its dimension", tardisId);
    }

    private static void deleteFolder(Path folder) {
        if (!Files.exists(folder)) return;
        try (Stream<Path> paths = Files.walk(folder)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            Constants.LOG.error("[GC] Failed to delete TARDIS dimension folder {}", folder, e);
        }
    }
}
