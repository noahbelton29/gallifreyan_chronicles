package com.noahtnt2009.gallifreyan_chronicles.compat.portal;

import net.minecraft.resources.ResourceKey;
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
