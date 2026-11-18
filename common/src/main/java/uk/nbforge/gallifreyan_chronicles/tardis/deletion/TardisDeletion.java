package uk.nbforge.gallifreyan_chronicles.tardis.deletion;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.compat.portal.TardisPortalBridge;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.TardisBlocks;
import uk.nbforge.gallifreyan_chronicles.core.lifecycle.ServerState;
import uk.nbforge.gallifreyan_chronicles.mixin.server.MinecraftServerAccessor;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisDimensionManager;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisManager;

public final class TardisDeletion {
    private record Request(ResourceKey<Level> exitDimension, Vec3 exitPos) {}

    private static final Map<UUID, Request> PENDING = ServerState.track(new LinkedHashMap<>());
    private static final int REMOVE_FLAGS = Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS;

    private static final ExecutorService DELETE_EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "GC-TardisDeletion");
        thread.setDaemon(true);
        return thread;
    });

    private TardisDeletion() {}

    public static void request(UUID tardisId, ServerLevel exitLevel, BlockPos exitPos) {
        PENDING.put(tardisId, new Request(exitLevel.dimension(), Vec3.atBottomCenterOf(exitPos)));
    }

    public static void removeCompletely(Tardis tardis) {
        ServerLevel exteriorLevel =
                tardis.exteriorLevel().orElse(tardis.server().overworld());
        BlockPos exteriorPos = tardis.exteriorPos().orElse(null);
        BlockPos exitPos = exteriorPos != null
                ? exteriorPos
                : exteriorLevel.getRespawnData().pos();
        if (exteriorPos != null) {
            exteriorLevel.getChunkAt(exteriorPos);
            BlockPos top = exteriorPos.above();
            if (exteriorLevel.getBlockState(top).is(TardisBlocks.TARDIS_TOP)) {
                exteriorLevel.setBlock(top, Blocks.AIR.defaultBlockState(), REMOVE_FLAGS);
            }
            if (exteriorLevel.getBlockState(exteriorPos).is(TardisBlocks.TARDIS)) {
                exteriorLevel.setBlock(exteriorPos, Blocks.AIR.defaultBlockState(), REMOVE_FLAGS);
            }
        }
        request(tardis.id(), exteriorLevel, exitPos);
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
        Path dimensionPath = ((MinecraftServerAccessor) server)
                .gallifreyan_chronicles$getStorageSource()
                .getDimensionPath(key);

        if (interior != null) {
            ServerLevel exit = server.getLevel(request.exitDimension());
            if (exit == null) exit = server.overworld();
            for (ServerPlayer player : List.copyOf(interior.players())) {
                player.teleportTo(
                        exit,
                        request.exitPos().x,
                        request.exitPos().y,
                        request.exitPos().z,
                        Set.of(),
                        player.getYRot(),
                        player.getXRot(),
                        true);
            }
            TardisPortalBridge.beforeDimensionRemoved(interior);
            MinecraftServerAccessor accessor = (MinecraftServerAccessor) server;
            accessor.gallifreyan_chronicles$getLevels().remove(key);

            DELETE_EXECUTOR.execute(() -> {
                try {
                    interior.close();
                } catch (IOException e) {
                    Constants.LOG.error("[GC] Failed to close TARDIS dimension {}", key.identifier(), e);
                }
                deleteFolder(dimensionPath);
            });
        } else {
            DELETE_EXECUTOR.execute(() -> deleteFolder(dimensionPath));
        }

        TardisManager.get(server).removeTardis(tardisId);
        Constants.LOG.info("[GC] Deleted TARDIS {} and its dimension", tardisId);
    }

    private static void deleteFolder(Path folder) {
        if (!Files.exists(folder)) return;
        try {
            Files.walkFileTree(folder, new SimpleFileVisitor<>() {
                @Override
                public @NonNull FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public @NonNull FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            Constants.LOG.error("[GC] Failed to delete TARDIS dimension folder {}", folder, e);
        }
    }
}
