package com.noahtnt2009.gallifreyan_chronicles.tardis.interior;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.ConsoleLinkSystem;
import com.noahtnt2009.gallifreyan_chronicles.init.GCDimensions;
import com.noahtnt2009.gallifreyan_chronicles.mixin.MinecraftServerAccessor;
import com.noahtnt2009.gallifreyan_chronicles.tardis.manager.TardisManager;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class TardisDimensionManager {
    private static final Map<UUID, UUID> PENDING = new LinkedHashMap<>();
    private static final Map<UUID, Integer> PLACE_CONTROLS = new LinkedHashMap<>();
    private static final int CONTROL_PLACE_DELAY_TICKS = 5;

    private TardisDimensionManager() {
    }

    public static ResourceKey<Level> levelKey(UUID tardisId) {
        return ResourceKey.create(Registries.DIMENSION, GCUtils.of("tardis/" + tardisId));
    }

    public static @Nullable ServerLevel get(MinecraftServer server, UUID tardisId) {
        return server.getLevel(levelKey(tardisId));
    }

    public static ServerLevel getOrCreate(MinecraftServer server, UUID tardisId) {
        ResourceKey<Level> key = levelKey(tardisId);
        ServerLevel existing = server.getLevel(key);
        if (existing != null) return existing;

        LevelStem template = server.registryAccess().lookupOrThrow(Registries.LEVEL_STEM)
                .getValue(GCDimensions.TARDIS_INTERIOR_KEY);
        if (template == null) {
            throw new IllegalStateException("missing level stem " + GCDimensions.TARDIS_INTERIOR_KEY);
        }

        MinecraftServerAccessor accessor = (MinecraftServerAccessor) server;
        var worldData = server.getWorldData();
        DerivedLevelData levelData = new DerivedLevelData(worldData, worldData.overworldData());
        long biomeZoomSeed = BiomeManager.obfuscateSeed(server.getWorldGenSettings().options().seed());

        ServerLevel level = new ServerLevel(server, Util.backgroundExecutor(),
                accessor.gallifreyan_chronicles$getStorageSource(), levelData, key, template,
                worldData.isDebugWorld(), biomeZoomSeed, List.of(), false);

        accessor.gallifreyan_chronicles$getLevels().put(key, level);
        level.getWorldBorder().setAbsoluteMaxSize(server.getAbsoluteMaxWorldSize());
        server.getPlayerList().addWorldborderListener(level);

        Constants.LOG.info("created tardis dimension {}", key.identifier());
        return level;
    }

    public static void restoreAll(MinecraftServer server) {
        PENDING.clear();
        PLACE_CONTROLS.clear();
        for (UUID tardisId : TardisManager.get(server).getInteriorIds()) {
            try {
                getOrCreate(server, tardisId);
            } catch (Exception e) {
                Constants.LOG.error("failed to restore tardis dimension for {}", tardisId, e);
            }
        }
    }

    public static void request(MinecraftServer server, ServerPlayer player, UUID tardisId) {
        ServerLevel level = get(server, tardisId);
        if (level != null) {
            enterNow(player, level, tardisId);
            return;
        }
        PENDING.putIfAbsent(player.getUUID(), tardisId);
    }

    public static void processQueue(MinecraftServer server) {
        processControlPlacement(server);
        if (PENDING.isEmpty()) return;

        Map<UUID, UUID> work = new LinkedHashMap<>(PENDING);
        PENDING.clear();

        for (Map.Entry<UUID, UUID> entry : work.entrySet()) {
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null) continue;
            try {
                ServerLevel level = getOrCreate(server, entry.getValue());
                enterNow(player, level, entry.getValue());
            } catch (Exception e) {
                Constants.LOG.error("failed to enter tardis {}", entry.getValue(), e);
            }
        }
    }

    private static void enterNow(ServerPlayer player, ServerLevel interior, UUID tardisId) {
        boolean firstEntry = TardisInteriorSystem.ensureBuilt(interior, tardisId);
        Vec3 target = TardisInteriorSystem.arrivalPoint();
        player.teleportTo(interior, target.x, target.y, target.z, Set.of(),
                TardisInteriorSystem.ARRIVAL_YAW, 0.0f, true);

        if (firstEntry) {
            PLACE_CONTROLS.put(tardisId, CONTROL_PLACE_DELAY_TICKS);
        }
    }

    private static void processControlPlacement(MinecraftServer server) {
        if (PLACE_CONTROLS.isEmpty()) return;

        var iterator = PLACE_CONTROLS.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (entry.getValue() > 1) {
                entry.setValue(entry.getValue() - 1);
                continue;
            }
            iterator.remove();

            UUID tardisId = entry.getKey();
            ServerLevel level = get(server, tardisId);
            if (level == null) continue;

            ConsoleLinkSystem.resolve(level, tardisId).ifPresentOrElse(
                    TardisConsoleBlockEntity::respawnControls,
                    () -> Constants.LOG.warn("no console to place controls on for tardis {}", tardisId));
        }
    }
}