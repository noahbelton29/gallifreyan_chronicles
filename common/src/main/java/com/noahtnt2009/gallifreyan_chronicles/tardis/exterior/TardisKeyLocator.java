package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCDataComponents;
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

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;

public final class TardisKeyLocator {
    private static final int INTERVAL = 5;
    private static final int COLOR = 0xFFFFFFFF;
    private static final ResourceKey<WaypointStyleAsset> FIRST_DOCTORS_STYLE = style("tardis");
    private static final ResourceKey<WaypointStyleAsset> SECOND_DOCTORS_STYLE = style("second_doctors_tardis");
    private static final Map<ServerPlayer, Tracked> TRACKED = new WeakHashMap<>();

    private record Tracked(UUID tardisId, BlockPos pos, ResourceKey<Level> dimension,
                           ResourceKey<WaypointStyleAsset> style) {
    }

    private TardisKeyLocator() {
    }

    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % INTERVAL != 0) return;

        TardisManager manager = TardisManager.get(server);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Tracked previous = TRACKED.get(player);
            Tracked next = resolve(server, manager, player);

            if (next == null) {
                if (previous != null) {
                    player.connection.send(ClientboundTrackedWaypointPacket.removeWaypoint(previous.tardisId()));
                    TRACKED.remove(player);
                }
                continue;
            }

            if (next.equals(previous)) continue;

            if (previous != null && !previous.tardisId().equals(next.tardisId())) {
                player.connection.send(ClientboundTrackedWaypointPacket.removeWaypoint(previous.tardisId()));
                previous = null;
            }

            Vec3i position = next.pos();
            player.connection.send(previous == null
                    ? ClientboundTrackedWaypointPacket.addWaypointPosition(next.tardisId(), icon(next.style()), position)
                    : ClientboundTrackedWaypointPacket.updateWaypointPosition(next.tardisId(), icon(next.style()), position));
            TRACKED.put(player, next);
        }
    }

    private static @Nullable Tracked resolve(MinecraftServer server, TardisManager manager, ServerPlayer player) {
        UUID tardisId = heldKeyTardis(player.getMainHandItem());
        if (tardisId == null) tardisId = heldKeyTardis(player.getOffhandItem());
        if (tardisId == null) return null;

        BlockPos pos = manager.resolveExteriorBlockPos(tardisId);
        if (pos == null) return null;

        ResourceKey<Level> dimension = manager.getExteriorDimension(tardisId);
        if (!player.level().dimension().equals(dimension)) return null;

        return new Tracked(tardisId, pos.immutable(), dimension, styleFor(manager, server, tardisId, dimension, pos));
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
