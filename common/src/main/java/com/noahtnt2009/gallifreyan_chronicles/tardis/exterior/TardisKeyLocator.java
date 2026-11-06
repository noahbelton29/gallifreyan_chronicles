package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCDataComponents;
import com.noahtnt2009.gallifreyan_chronicles.item.MasterTardisKeyItem;
import com.noahtnt2009.gallifreyan_chronicles.item.TardisKeyItem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.interior.TardisInteriorRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.manager.TardisManager;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.ClientboundTrackedWaypointPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointStyleAsset;
import net.minecraft.world.waypoints.WaypointStyleAssets;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

public final class TardisKeyLocator {
    private static final int MASTER_INTERVAL = 5;
    private static final int COLOR = 0xFFFFFFFF;
    private static final ResourceKey<WaypointStyleAsset> FIRST_DOCTORS_STYLE = style("tardis");
    private static final ResourceKey<WaypointStyleAsset> SECOND_DOCTORS_STYLE = style("second_doctors_tardis");
    private static final Map<ServerPlayer, Map<UUID, Tracked>> TRACKED = new WeakHashMap<>();

    private record Tracked(UUID tardisId, BlockPos pos, ResourceKey<Level> dimension,
                           ResourceKey<WaypointStyleAsset> style) {
    }

    private TardisKeyLocator() {
    }

    public static void tick(MinecraftServer server) {
        TardisManager manager = TardisManager.get(server);
        boolean masterTick = server.getTickCount() % MASTER_INTERVAL == 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            boolean master = holdsMasterKey(player);
            Map<UUID, Tracked> previous = TRACKED.getOrDefault(player, Map.of());

            if (master && !masterTick && !previous.isEmpty()) continue;

            Map<UUID, Tracked> next = resolve(server, manager, player, master);

            for (Tracked old : previous.values()) {
                if (!next.containsKey(old.tardisId())) {
                    player.connection.send(ClientboundTrackedWaypointPacket.removeWaypoint(old.tardisId()));
                }
            }
            for (Tracked tracked : next.values()) {
                Tracked old = previous.get(tracked.tardisId());
                if (tracked.equals(old)) continue;
                send(player, tracked, old);
            }

            if (next.isEmpty()) {
                TRACKED.remove(player);
            } else {
                TRACKED.put(player, next);
            }
        }
    }

    public static void refresh(MinecraftServer server, UUID tardisId) {
        TardisManager manager = TardisManager.get(server);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Map<UUID, Tracked> current = TRACKED.get(player);
            if (current == null || !current.containsKey(tardisId)) continue;

            Tracked updated = resolveOne(server, manager, player, tardisId);
            if (updated == null || updated.equals(current.get(tardisId))) continue;

            Map<UUID, Tracked> copy = new HashMap<>(current);
            copy.put(tardisId, updated);
            TRACKED.put(player, copy);
            send(player, updated, current.get(tardisId));
        }
    }

    private static void send(ServerPlayer player, Tracked tracked, @Nullable Tracked old) {
        Vec3i position = tracked.pos();
        boolean iconChanged = old != null && !old.style().equals(tracked.style());
        if (iconChanged) {
            player.connection.send(ClientboundTrackedWaypointPacket.removeWaypoint(tracked.tardisId()));
        }
        player.connection.send(old == null || iconChanged
                ? ClientboundTrackedWaypointPacket.addWaypointPosition(tracked.tardisId(), icon(tracked.style()), position)
                : ClientboundTrackedWaypointPacket.updateWaypointPosition(tracked.tardisId(), icon(tracked.style()), position));
    }

    private static Map<UUID, Tracked> resolve(MinecraftServer server, TardisManager manager, ServerPlayer player,
                                              boolean master) {
        Set<UUID> ids = new HashSet<>();
        if (master) {
            ids.addAll(manager.getAllIds());
        } else {
            UUID main = heldKeyTardis(player.getMainHandItem());
            UUID off = heldKeyTardis(player.getOffhandItem());
            if (main != null) ids.add(main);
            if (off != null) ids.add(off);
        }

        Map<UUID, Tracked> result = new HashMap<>();
        for (UUID id : ids) {
            Tracked tracked = resolveOne(server, manager, player, id);
            if (tracked != null) result.put(id, tracked);
        }
        return result;
    }

    private static @Nullable Tracked resolveOne(MinecraftServer server, TardisManager manager, ServerPlayer player,
                                                UUID tardisId) {
        BlockPos pos = manager.resolveExteriorBlockPos(tardisId);
        if (pos == null) return null;

        ResourceKey<Level> dimension = manager.getExteriorDimension(tardisId);
        if (!player.level().dimension().equals(dimension)) return null;

        return new Tracked(tardisId, pos.immutable(), dimension, styleFor(manager, server, tardisId, dimension, pos));
    }

    private static boolean holdsMasterKey(ServerPlayer player) {
        return player.getMainHandItem().getItem() instanceof MasterTardisKeyItem
                || player.getOffhandItem().getItem() instanceof MasterTardisKeyItem;
    }

    private static @Nullable UUID heldKeyTardis(ItemStack stack) {
        if (!(stack.getItem() instanceof TardisKeyItem)) return null;
        return stack.get(GCDataComponents.TARDIS_ID);
    }

    private static ResourceKey<WaypointStyleAsset> styleFor(TardisManager manager, MinecraftServer server,
                                                            UUID tardisId, ResourceKey<Level> dimension, BlockPos pos) {
        String exteriorId = null;
        ServerLevel level = server.getLevel(dimension);
        if (level != null && level.isLoaded(pos)
                && level.getBlockEntity(pos) instanceof TardisExteriorBlockEntity exterior
                && exterior.getExterior() != null) {
            exteriorId = exterior.getExterior().id();
        }
        if (exteriorId == null) {
            exteriorId = TardisInteriorRegistry.get(manager.getInteriorId(tardisId)).resolveExteriorId();
        }
        return exteriorId != null && exteriorId.endsWith("second_doctors_exterior")
                ? SECOND_DOCTORS_STYLE
                : FIRST_DOCTORS_STYLE;
    }

    private static ResourceKey<WaypointStyleAsset> style(String name) {
        return ResourceKey.create(WaypointStyleAssets.ROOT_ID, GCUtils.modIdentifier(name));
    }

    private static Waypoint.Icon icon(ResourceKey<WaypointStyleAsset> style) {
        Waypoint.Icon icon = new Waypoint.Icon();
        icon.style = style;
        icon.color = Optional.of(COLOR);
        return icon;
    }
}
