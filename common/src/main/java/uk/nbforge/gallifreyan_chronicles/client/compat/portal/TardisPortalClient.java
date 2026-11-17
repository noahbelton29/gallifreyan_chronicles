package uk.nbforge.gallifreyan_chronicles.client.compat.portal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import qouteall.imm_ptl.core.CHelper;
import qouteall.imm_ptl.core.ClientWorldLoader;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.render.context_management.PortalRendering;
import qouteall.imm_ptl.core.render.renderer.RendererUsingStencil;
import uk.nbforge.gallifreyan_chronicles.client.gui.hud.GCPowerDarkness;
import uk.nbforge.gallifreyan_chronicles.client.gui.hud.PowerDarknessTint;
import uk.nbforge.gallifreyan_chronicles.compat.portal.TardisPortalBridge;
import uk.nbforge.gallifreyan_chronicles.config.GCConfig;
import uk.nbforge.gallifreyan_chronicles.core.block.InteriorDoorBarrierBlock;
import uk.nbforge.gallifreyan_chronicles.core.block.InteriorDoorBlock;
import uk.nbforge.gallifreyan_chronicles.core.block.TardisBlock;
import uk.nbforge.gallifreyan_chronicles.core.block.TardisTopBlock;
import uk.nbforge.gallifreyan_chronicles.util.TardisDimensionUtil;
import uk.nbforge.gallifreyan_chronicles.client.lifecycle.ClientState;

public final class TardisPortalClient {
    private static final double DOOR_FOOTPRINT_HORIZONTAL = 2.6;
    private static final double DOOR_FOOTPRINT_VERTICAL = 3.0;

    private TardisPortalClient() {}

    public static @Nullable Level clientLevel(ResourceKey<Level> dimension) {
        return ClientWorldLoader.peekWorld(dimension);
    }

    public static boolean isViewingThroughPortalNear(Vec3 pos, double range) {
        if (!PortalRendering.isRendering()) return false;
        return PortalRendering.getRenderingPortal().getDestPos().distanceToSqr(pos) <= range * range;
    }

    public static boolean isRenderingAnyPortal() {
        return PortalRendering.isRendering();
    }

    public static boolean isRenderingTardisPortal() {
        if (!PortalRendering.isRendering()) return false;
        return TardisDimensionUtil.isInterior(
                PortalRendering.getRenderingPortal().getDestDim());
    }

    private record Scored(Portal portal, double distance) {}

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

    public static boolean shouldSkipRendering(Object object) {
        if (!(object instanceof Portal portal)) return false;
        if (!TardisPortalBridge.PORTAL_TAG.equals(portal.portalTag)) return false;
        if (!(portal.level() instanceof ClientLevel level)) return false;

        Vec3 camera = CHelper.getCurrentCameraPos();
        Ranking current = ranking;
        if (current == null
                || current.level != level
                || current.tick != level.getGameTime()
                || !current.camera.equals(camera)
                || !current.scanned.contains(portal)) {
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

    public static boolean isNearTardisPortal(double range) {
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
            if (entity instanceof Portal portal && TardisDimensionUtil.isInterior(portal.getDestDim())) {
                portals.add(portal);
            }
        }
        return portals;
    }

    public static boolean isInteriorDoorAtInAnyTardisLevel(BlockPos pos) {
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

    // Doorway centres seen so far per TARDIS interior dimension
    // Immersive Portals draws the outline of the remote hit while the client level is swapped to the destination
    // dimension where the entry portal is not listed, and the portal can disappear once the door is replaced
    // So the centres are remembered and not looked up from the current level
    private static final java.util.Map<ResourceKey<Level>, Set<Vec3>> DOOR_CENTRES = ClientState.track(new java.util.HashMap<>());

    private static void rememberDoorCentres(Level playerLevel) {
        for (Portal portal : tardisPortals((ClientLevel) playerLevel)) {
            Set<Vec3> centres = DOOR_CENTRES.computeIfAbsent(portal.getDestDim(), k -> new HashSet<>());
            if (centres.size() > 8) centres.clear();
            centres.add(portal.getDestPos());
        }
    }

    /**
     * True when the position is inside the doorway of a TARDIS interior seen through a portal, whatever block is there.
     * It keeps the outline hidden after the interior door or its barriers were replaced by another block.
     */
    public static boolean isInDoorFootprintOfAnyTardisPortal(BlockPos pos) {
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
            // Standing inside that TARDIS the door area is ordinary space that can be targeted
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

    /** True when the interior door behind the portal has a block flush against its back, unknown counts as not blocked because the door or level is not loaded. */
    public static boolean isDoorBackBlocked(Object object) {
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

    /** The base block of the exterior TARDIS this portal sits in, null when it cannot be found. */
    public static @Nullable BlockPos exteriorBaseOf(Object object) {
        if (!(object instanceof Portal portal)) return null;
        Level level = portal.level();
        Vec3 origin = portal.getOriginPos();
        BlockPos at = BlockPos.containing(origin);
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-1, -1, -1), at.offset(1, 1, 1))) {
            Block block = level.getBlockState(p).getBlock();
            BlockPos base;
            if (block instanceof TardisBlock) {
                base = p.immutable();
            } else if (block instanceof TardisTopBlock) {
                base = p.below();
            } else {
                continue;
            }
            double distance = Vec3.atCenterOf(p).distanceToSqr(origin);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = base;
            }
        }
        return best;
    }

    public static void tintPortalForPowerDarkness(Object object) {
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
