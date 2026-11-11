package com.noahtnt2009.gallifreyan_chronicles.compat.portal;

import com.noahtnt2009.gallifreyan_chronicles.util.TardisDimensions;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.block.InteriorDoorBarrierBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.InteriorDoorBlock;
import com.noahtnt2009.gallifreyan_chronicles.config.GCConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import qouteall.imm_ptl.core.CHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import com.noahtnt2009.gallifreyan_chronicles.client.gui.hud.GCPowerDarkness;
import com.noahtnt2009.gallifreyan_chronicles.client.gui.hud.PowerDarknessTint;
import qouteall.imm_ptl.core.ClientWorldLoader;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.render.context_management.PortalRendering;
import qouteall.imm_ptl.core.render.renderer.RendererUsingStencil;

final class TardisPortalClient {
    private TardisPortalClient() {
    }

    static @Nullable Level clientLevel(ResourceKey<Level> dimension) {
        return ClientWorldLoader.peekWorld(dimension);
    }

    static boolean isViewingThroughPortalNear(Vec3 pos, double range) {
        if (!PortalRendering.isRendering()) return false;
        return PortalRendering.getRenderingPortal().getDestPos().distanceToSqr(pos) <= range * range;
    }

    static boolean isRenderingAnyPortal() {
        return PortalRendering.isRendering();
    }

    static boolean isRenderingTardisPortal() {
        if (!PortalRendering.isRendering()) return false;
        return TardisDimensions.isInterior(PortalRendering.getRenderingPortal().getDestDim());
    }

    private record Scored(Portal portal, double distance) {
    }

    private static final class Ranking {
        private final Level level;
        private final Vec3 camera;
        private final long tick;
        private final Set<Portal> scanned = Collections.newSetFromMap(new IdentityHashMap<>());
        private final Set<Portal> allowed = Collections.newSetFromMap(new IdentityHashMap<>());

        private Ranking(Level level, Vec3 camera, long tick) {
            this.level = level;
            this.camera = camera;
            this.tick = tick;
        }
    }

    private static @Nullable Ranking ranking;

    static boolean shouldSkipRendering(Object object) {
        if (!(object instanceof Portal portal)) return false;
        if (!TardisPortalBridge.PORTAL_TAG.equals(portal.portalTag)) return false;
        if (!(portal.level() instanceof ClientLevel level)) return false;

        Vec3 camera = CHelper.getCurrentCameraPos();
        Ranking current = ranking;
        if (current == null || current.level != level || current.tick != level.getGameTime()
                || !current.camera.equals(camera) || !current.scanned.contains(portal)) {
            current = rank(level, camera);
            ranking = current;
        }
        return current.scanned.contains(portal) && !current.allowed.contains(portal);
    }

    private static Ranking rank(ClientLevel level, Vec3 camera) {
        Ranking result = new Ranking(level, camera, level.getGameTime());
        double range = GCConfig.portalRenderDistance();
        List<Scored> candidates = new ArrayList<>();
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof Portal portal) || !TardisPortalBridge.PORTAL_TAG.equals(portal.portalTag)) continue;
            result.scanned.add(portal);
            if (!portal.isVisible()) continue;
            double distance = portal.getDistanceToNearestPointInPortal(camera);
            if (distance <= range) candidates.add(new Scored(portal, distance));
        }
        candidates.sort(Comparator.comparingDouble(Scored::distance));
        int limit = Math.min(GCConfig.maxRenderedPortals(), candidates.size());
        for (int i = 0; i < limit; i++) result.allowed.add(candidates.get(i).portal());
        return result;
    }

    static boolean isNearTardisPortal(double range) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return false;
        Vec3 camera = CHelper.getCurrentCameraPos();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof Portal portal) || !TardisPortalBridge.PORTAL_TAG.equals(portal.portalTag)) continue;
            if (!portal.isVisible()) continue;
            if (portal.getDistanceToNearestPointInPortal(camera) <= range) return true;
        }
        return false;
    }

    private static List<Portal> tardisPortals(ClientLevel level) {
        List<Portal> portals = new ArrayList<>();
        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof Portal portal && TardisDimensions.isInterior(portal.getDestDim())) {
                portals.add(portal);
            }
        }
        return portals;
    }

    static boolean isInteriorDoorAtInAnyTardisLevel(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return false;
        Set<ResourceKey<Level>> checked = new HashSet<>();
        for (Portal portal : tardisPortals(mc.level)) {
            ResourceKey<Level> destDim = portal.getDestDim();
            if (!checked.add(destDim)) continue;
            Level dest = ClientWorldLoader.peekWorld(destDim);
            if (dest == null) continue;
            Block block = dest.getBlockState(pos).getBlock();
            if (block instanceof InteriorDoorBlock || block instanceof InteriorDoorBarrierBlock) return true;
        }
        return false;
    }

    // Horizontal / vertical reach (in blocks) from a portal's destination centre that still counts as the
    // interior door zone: the 2x3 door, its barrier row and the blocks right around them (a re-placed door can sit
    // a block or two off the old one). Measured to block centres.
    private static final double DOOR_FOOTPRINT_HORIZONTAL = 2.6;
    private static final double DOOR_FOOTPRINT_VERTICAL = 3.0;

    /**
     * Doorway centres seen so far, per TARDIS interior dimension. Immersive Portals draws the outline of the
     * remote hit while the client level is swapped to the destination dimension, where the entry portal is not
     * listed, and the portal itself can disappear once the door is replaced. So the centres are remembered
     * instead of being looked up from the current level.
     */
    private static final java.util.Map<ResourceKey<Level>, Set<Vec3>> DOOR_CENTRES = new java.util.HashMap<>();

    private static void rememberDoorCentres(Level playerLevel) {
        for (Portal portal : tardisPortals((ClientLevel) playerLevel)) {
            Set<Vec3> centres = DOOR_CENTRES.computeIfAbsent(portal.getDestDim(), k -> new HashSet<>());
            if (centres.size() > 8) centres.clear();
            centres.add(portal.getDestPos());
        }
    }

    /**
     * True when {@code pos} lies inside the doorway footprint of a TARDIS interior seen through a portal,
     * whatever block currently occupies it. This keeps the outline hidden after the interior door (or its
     * barriers) has been replaced by another block.
     */
    static boolean isInDoorFootprintOfAnyTardisPortal(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            DOOR_CENTRES.clear();
            return false;
        }
        Level playerLevel = mc.player.level();
        if (playerLevel instanceof ClientLevel) rememberDoorCentres(playerLevel);
        if (DOOR_CENTRES.isEmpty()) return false;

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        for (var entry : DOOR_CENTRES.entrySet()) {
            // Standing inside that TARDIS: the door area is ordinary, targetable space.
            if (entry.getKey().equals(playerLevel.dimension())) continue;
            for (Vec3 dest : entry.getValue()) {
                double dx = x - dest.x;
                double dz = z - dest.z;
                if (dx * dx + dz * dz <= DOOR_FOOTPRINT_HORIZONTAL * DOOR_FOOTPRINT_HORIZONTAL
                        && Math.abs(y - dest.y) <= DOOR_FOOTPRINT_VERTICAL) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * True when the interior door behind {@code object} (a Portal) has a block flush against its back. Unknown
     * (door or level not loaded) counts as not blocked.
     */
    static boolean isDoorBackBlocked(Object object) {
        if (!(object instanceof Portal portal)) return false;
        Level dest = ClientWorldLoader.peekWorld(portal.getDestDim());
        if (dest == null) return false;
        BlockPos center = BlockPos.containing(portal.getDestPos());
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-2, -3, -2), center.offset(2, 3, 2))) {
            var state = dest.getBlockState(p);
            if (state.getBlock() instanceof InteriorDoorBlock && InteriorDoorBlock.isMaster(state)) {
                return InteriorDoorBlock.isBackBlocked(dest, p.immutable(), state);
            }
        }
        return false;
    }

    /** Base block of the exterior TARDIS this portal sits in, or null when it cannot be found. */
    static @Nullable BlockPos exteriorBaseOf(Object object) {
        if (!(object instanceof Portal portal)) return null;
        Level level = portal.level();
        BlockPos at = BlockPos.containing(portal.getOriginPos());
        for (BlockPos p : new BlockPos[]{at, at.below(), at.above()}) {
            Block block = level.getBlockState(p).getBlock();
            if (block instanceof com.noahtnt2009.gallifreyan_chronicles.block.TardisBlock) return p;
            if (block instanceof com.noahtnt2009.gallifreyan_chronicles.block.TardisTopBlock) return p.below();
        }
        return null;
    }

    static void tintPortalForPowerDarkness(Object object) {
        if (!(object instanceof Portal portal)) return;
        if (GCPowerDarkness.shaderPackHandlesDarkness()) return;
        Level destination = ClientWorldLoader.peekWorld(portal.getDestDim());
        float power = GCPowerDarkness.powerDarknessOf(destination);
        float alarm = GCPowerDarkness.alarmOf(destination);
        if (power <= 0.0F && alarm <= 0.0F) return;
        RendererUsingStencil.setStencilLimitation(PortalRendering.getPortalLayer() + 1);
        if (alarm > power) {
            PowerDarknessTint.draw(alarm * GCPowerDarkness.ALARM_MAX_ALPHA, GCPowerDarkness.ALARM_RED);
        } else {
            PowerDarknessTint.draw(power * GCPowerDarkness.overlayMaxAlpha());
        }
    }
}