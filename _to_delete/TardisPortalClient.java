package com.noahtnt2009.gallifreyan_chronicles.compat.portal;

import qouteall.imm_ptl.core.render.context_management.PortalRendering;

final class TardisPortalClient {
    private TardisPortalClient() {
    }

    static boolean isRenderingPortal() {
        return PortalRendering.isRendering();
    }
}
