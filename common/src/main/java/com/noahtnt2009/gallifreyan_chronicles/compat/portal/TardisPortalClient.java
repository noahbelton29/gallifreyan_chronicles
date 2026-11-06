package com.noahtnt2009.gallifreyan_chronicles.compat.portal;

import com.noahtnt2009.gallifreyan_chronicles.config.GCConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import qouteall.imm_ptl.core.CHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import com.noahtnt2009.gallifreyan_chronicles.client.gui.hud.GCPowerDarkness;
import com.noahtnt2009.gallifreyan_chronicles.client.gui.hud.PowerDarknessTint;
import qouteall.imm_ptl.core.ClientWorldLoader;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.render.renderer.RendererUsingStencil;
import qouteall.imm_ptl.core.render.context_management.PortalRendering;

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

    static void tintPortalForPowerDarkness(Object object) {
        if (!(object instanceof Portal portal)) return;
        if (GCPowerDarkness.shaderPackHandlesDarkness()) return;
        Level destination = ClientWorldLoader.peekWorld(portal.getDestDim());
        float darkness = GCPowerDarkness.darknessOf(destination);
        if (darkness <= 0.0F) return;
        RendererUsingStencil.setStencilLimitation(PortalRendering.getPortalLayer() + 1);
        PowerDarknessTint.draw(darkness * GCPowerDarkness.overlayMaxAlpha());
    }
}
