package uk.nbforge.gallifreyan_chronicles.tardis;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.compat.portal.TardisPortalBridge;
import uk.nbforge.gallifreyan_chronicles.core.GCDimensions;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.lifecycle.ServerState;
import uk.nbforge.gallifreyan_chronicles.mixin.server.MinecraftServerAccessor;
import uk.nbforge.gallifreyan_chronicles.tardis.interior.TardisInteriors;
import uk.nbforge.gallifreyan_chronicles.util.GCLog;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

public final class TardisDimensionManager {
    private static final Map<UUID, UUID> PENDING = ServerState.track(new LinkedHashMap<>());
    private static final Map<UUID, Integer> PLACE_CONTROLS = ServerState.track(new LinkedHashMap<>());
    private static final int CONTROL_PLACE_DELAY_TICKS = 5;

    private TardisDimensionManager() {}

    public static ResourceKey<Level> levelKey(UUID tardisId) {
        return ResourceKey.create(Registries.DIMENSION, GCUtil.modIdentifier("tardis/" + tardisId));
    }

    /** use {@link Tardis#interiorLevel()}, this is what it reads */
    public static @Nullable ServerLevel get(Tardis tardis) {
        return tardis.server().getLevel(levelKey(tardis.id()));
    }

    public static ServerLevel getOrCreate(Tardis tardis) {
        MinecraftServer server = tardis.server();
        ResourceKey<Level> key = levelKey(tardis.id());
        ServerLevel existing = server.getLevel(key);
        if (existing != null) return existing;

        LevelStem template =
                server.registryAccess().lookupOrThrow(Registries.LEVEL_STEM).getValue(GCDimensions.TARDIS_INTERIOR_KEY);
        if (template == null) {
            throw new IllegalStateException("missing level stem " + GCDimensions.TARDIS_INTERIOR_KEY);
        }

        MinecraftServerAccessor accessor = (MinecraftServerAccessor) server;
        var worldData = server.getWorldData();
        DerivedLevelData levelData = new DerivedLevelData(worldData, worldData.overworldData());
        long biomeZoomSeed = BiomeManager.obfuscateSeed(
                server.getWorldGenSettings().options().seed());

        ServerLevel level = new ServerLevel(
                server,
                Util.backgroundExecutor(),
                accessor.gallifreyan_chronicles$getStorageSource(),
                levelData,
                key,
                template,
                worldData.isDebugWorld(),
                biomeZoomSeed,
                List.of(),
                false);

        accessor.gallifreyan_chronicles$getLevels().put(key, level);
        level.getWorldBorder().setAbsoluteMaxSize(server.getAbsoluteMaxWorldSize());
        server.getPlayerList().addWorldborderListener(level);

        GCLog.TARDIS.info("Created TARDIS dimension {}", key.identifier());
        TardisPortalBridge.onDimensionsChanged(server);
        return level;
    }

    public static void restoreAll(MinecraftServer server) {
        PENDING.clear();
        PLACE_CONTROLS.clear();
        for (UUID tardisId : TardisManager.get(server).getInteriorIds()) {
            try {
                Tardis.get(server, tardisId).ifPresent(TardisDimensionManager::getOrCreate);
            } catch (Exception e) {
                GCLog.TARDIS.error("Couldn't restore TARDIS dimension for {}", tardisId, e);
            }
        }
    }

    public static void requestEntry(Tardis tardis, ServerPlayer player) {
        ServerLevel level = get(tardis);
        if (level != null) {
            teleportPlayerIntoInterior(tardis, player, level);
            return;
        }
        PENDING.putIfAbsent(player.getUUID(), tardis.id());
    }

    public static void processPendingEntries(MinecraftServer server) {
        processControlPlacement(server);
        if (PENDING.isEmpty()) return;

        Map<UUID, UUID> work = new LinkedHashMap<>(PENDING);
        PENDING.clear();

        for (Map.Entry<UUID, UUID> entry : work.entrySet()) {
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null) continue;
            try {
                Tardis tardis = Tardis.get(server, entry.getValue()).orElse(null);
                if (tardis == null) continue;
                ServerLevel level = getOrCreate(tardis);
                teleportPlayerIntoInterior(tardis, player, level);
            } catch (Exception e) {
                GCLog.TARDIS.error("Couldn't enter TARDIS {}", entry.getValue(), e);
            }
        }
    }

    public static void scheduleControlPlacement(Tardis tardis) {
        PLACE_CONTROLS.put(tardis.id(), CONTROL_PLACE_DELAY_TICKS);
    }

    private static void teleportPlayerIntoInterior(Tardis tardis, ServerPlayer player, ServerLevel interior) {
        boolean firstEntry = TardisInteriors.buildInteriorIfMissing(tardis, interior);
        TardisInteriors.Arrival arrival = TardisInteriors.arrival(tardis, interior);
        TardisInteriors.markArriving(player);
        player.teleportTo(
                interior, arrival.pos().x, arrival.pos().y, arrival.pos().z, Set.of(), arrival.yaw(), 0.0f, true);

        if (firstEntry) {
            scheduleControlPlacement(tardis);
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
            Tardis.get(server, tardisId)
                    .flatMap(Tardis::console)
                    .ifPresentOrElse(
                            TardisConsoleBlockEntity::respawnControls,
                            () -> GCLog.TARDIS.warn("No console to place controls on for TARDIS {}", tardisId));
        }
    }
}
