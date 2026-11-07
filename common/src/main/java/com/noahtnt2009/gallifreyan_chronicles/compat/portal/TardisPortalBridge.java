package com.noahtnt2009.gallifreyan_chronicles.compat.portal;

import com.noahtnt2009.gallifreyan_chronicles.config.GCConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TardisPortalBridge {
    public static final String PORTAL_TAG = "gallifreyan_chronicles.tardis_portal";
    private static final String PORTAL_CLASS = "qouteall.imm_ptl.core.portal.Portal";
    private static final Set<UUID> ACTIVE = ConcurrentHashMap.newKeySet();
    private static @Nullable Boolean available;

    private TardisPortalBridge() {
    }

    public static boolean isAvailable() {
        if (available == null) {
            boolean found;
            try {
                Class.forName(PORTAL_CLASS, false, TardisPortalBridge.class.getClassLoader());
                found = true;
            } catch (ClassNotFoundException | LinkageError e) {
                found = false;
            }
            available = found;
        }
        return available;
    }

    public static boolean isActive(@Nullable UUID tardisId) {
        return tardisId != null && ACTIVE.contains(tardisId);
    }

    static void setActive(UUID tardisId, boolean active) {
        if (active) {
            ACTIVE.add(tardisId);
        } else {
            ACTIVE.remove(tardisId);
        }
    }

    public static boolean isViewingThroughPortalNear(Vec3 pos, double range) {
        return isAvailable() && TardisPortalClient.isViewingThroughPortalNear(pos, range);
    }

    public static boolean isRenderingTardisPortal() {
        return isAvailable() && TardisPortalClient.isRenderingTardisPortal();
    }

    public static boolean isNearTardisPortal(double range) {
        return isAvailable() && TardisPortalClient.isNearTardisPortal(range);
    }

    public static boolean isInteriorDoorAtInAnyTardisLevel(net.minecraft.core.BlockPos pos) {
        return isAvailable() && TardisPortalClient.isInteriorDoorAtInAnyTardisLevel(pos);
    }
    public static boolean isInDoorFootprintOfAnyTardisPortal(net.minecraft.core.BlockPos pos) {
        return isAvailable() && TardisPortalClient.isInDoorFootprintOfAnyTardisPortal(pos);
    }

    public static boolean isDoorBackBlocked(Object portal) {
        return isAvailable() && TardisPortalClient.isDoorBackBlocked(portal);
    }

    public static net.minecraft.core.@Nullable BlockPos exteriorBaseOf(Object portal) {
        return isAvailable() ? TardisPortalClient.exteriorBaseOf(portal) : null;
    }

    public static @Nullable Level clientLevel(ResourceKey<Level> dimension) {
        return isAvailable() ? TardisPortalClient.clientLevel(dimension) : null;
    }

    public static boolean shouldSkipPortalRendering(Object portal) {
        return isAvailable() && TardisPortalClient.shouldSkipRendering(portal);
    }

    public static void tintPortalForPowerDarkness(Object portal) {
        if (!isAvailable()) return;
        TardisPortalClient.tintPortalForPowerDarkness(portal);
    }

    public static void tick(MinecraftServer server) {
        if (!isAvailable()) return;
        if (!GCConfig.immersivePortals()) {
            TardisPortals.disableAll();
            return;
        }
        TardisPortals.tick(server);
    }

    public static void onDoorsChanged(MinecraftServer server, UUID tardisId) {
        if (!isAvailable() || !GCConfig.immersivePortals()) return;
        TardisPortals.onDoorsChanged(server, tardisId);
    }

    public static void onExteriorPlaced(MinecraftServer server, UUID tardisId) {
        if (!isAvailable() || !GCConfig.immersivePortals()) return;
        TardisPortals.onExteriorPlaced(server, tardisId);
    }

    public static void onDimensionsChanged(MinecraftServer server) {
        if (!isAvailable()) return;
        TardisPortals.syncDimensionIds(server);
    }

    public static void removeTardis(MinecraftServer server, UUID tardisId) {
        ACTIVE.remove(tardisId);
        if (!isAvailable()) return;
        TardisPortals.removeTardis(tardisId);
    }

    public static void beforeDimensionRemoved(ServerLevel level) {
        if (!isAvailable()) return;
        TardisPortals.beforeDimensionRemoved(level);
    }

    public static @Nullable ServerLevel redirectedActionLevel() {
        return isAvailable() ? TardisPortals.redirectedActionLevel() : null;
    }

    public static void reset() {
        ACTIVE.clear();
        if (isAvailable()) {
            TardisPortals.reset();
        }
    }
}